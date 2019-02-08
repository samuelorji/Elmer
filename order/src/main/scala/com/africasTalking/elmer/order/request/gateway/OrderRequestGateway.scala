package com.africasTalking.elmer.order
package request.gateway

import java.net.URL

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

import akka.actor.{ Actor, ActorLogging }
import akka.http.scaladsl.model._
import akka.http.scaladsl.marshalling.Marshal
import akka.pattern.ask
import akka.util.Timeout

import spray.json._

import io.atlabs._

import horus.core.config.ATConfig
import horus.core.db.redis.RedisDbService
import horus.core.http.client.ATHttpClientT
import horus.core.snoop.SnoopErrorPublisherT
import horus.core.util.ATCCPrinter

import com.africasTalking._

import elmer.core.config.ElmerConfig
import elmer.core.db.redis.ElmerRedisDb
import elmer.core.util.ElmerEnum.{ FoodName, FoodOrderStatus }

import elmer.order.status.OrderStatusMessage.FoodOrderPendingStatusElement
import elmer.order.util.OrderJsonProtocol

import OrderRequestGatewayMarshalling._

private[request] object OrderRequestGateway {

  case class FoodOrderGatewayRequest(
    transactionId: String,
    name: FoodName.Value,
    quantity: Int,
    callbackUrl: Option[URL]
  ) extends ATCCPrinter

  case class FoodOrderGatewayResponse(
    status: FoodOrderStatus.Value,
    description: String
  ) extends ATCCPrinter

}

private[request] class OrderRequestGateway extends Actor
    with ATHttpClientT
    with OrderRequestGatewayJsonSupportT
    with SnoopErrorPublisherT
    with ActorLogging {

  implicit val system                  = context.system

  implicit val timeout                 = Timeout(ATConfig.httpRequestTimeout)

  private lazy val redisDbService      = createRedisDbService
  def createRedisDbService             = ElmerRedisDb.getInstance

  private val orderRequestUrl          = ElmerConfig.etherOrderRequestUrl
  private val orderStatusCallbackUrl   = ElmerConfig.etherOrderStatusCallbackUrl
  private val statusElementRedisPrefix = ElmerConfig.orderPendingStatusElementRedisPrefix
  private val statusElementLifetime    = ElmerConfig.orderPendingStatusElementLifetime

  import RedisDbService._
  import OrderRequestGateway._
  import OrderJsonProtocol._
  override def receive = {
    case req: FoodOrderGatewayRequest =>
      log.info("processing " + req)
      val currentSender = sender
      val sendFut = for {
        entity   <- Marshal(
          EtherFoodOrderRequest(
            name        = req.name,
            quantity    = req.quantity,
            callbackUrl = orderStatusCallbackUrl
          )
        ).to[MessageEntity]
        response <- sendHttpRequest(
          HttpRequest(
            method = HttpMethods.POST,
            uri    = orderRequestUrl,
            entity = entity
          )
        )
      } yield response
      sendFut onComplete {
        case Success(response) =>
          response.status.isSuccess match {
            case true =>
              try {
                val brokerResponse = response.data.parseJson.convertTo[EtherFoodOrderResponse]
                currentSender ! FoodOrderGatewayResponse(
                  status      = brokerResponse.status,
                  description = brokerResponse.description
                )
                brokerResponse.status match {
                  case FoodOrderStatus.Accepted =>
                    (redisDbService ? AddElementQuery(
                      key      = statusElementRedisPrefix + brokerResponse.transactionId,
                      value    = FoodOrderPendingStatusElement(
                        transactionId = req.transactionId,
                        callbackUrl   = req.callbackUrl
                      ).toJson.compactPrint,
                      lifetime = Some(statusElementLifetime)
                    )).mapTo[AddElementResult] onComplete {
                      case Success(_)     =>
                      case Failure(error) =>
                        publishError("Error while adding pending status element to redis for " + req, Some(error))
                    }
                  case _ =>
                }
              }
              catch {
                case ex: JsonParser.ParsingException =>
                  publishError(s"Error while processing response for $req: $response", Some(ex))
                  currentSender ! FoodOrderGatewayResponse(
                    status      = FoodOrderStatus.Failed,
                    description = "Internal error while sending request to the gateway"
                  )
              }
            case false =>
              publishError(s"Received Http error response while processing $req: $response")
              currentSender ! FoodOrderGatewayResponse(
                status      = FoodOrderStatus.Failed,
                description = "Internal error while sending request to the gateway"
              )
          }
        case Failure(error)    =>
          publishError("Error while processing " + req, Some(error))
          currentSender ! FoodOrderGatewayResponse(
            status      = FoodOrderStatus.Failed,
            description = "Internal error while sending request to the gateway"
          )
      }
  }

}
