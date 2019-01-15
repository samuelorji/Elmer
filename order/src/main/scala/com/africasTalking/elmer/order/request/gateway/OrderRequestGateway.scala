package com.africasTalking.elmer.order
package request.gateway

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

import akka.actor.{ Actor, ActorLogging }
import akka.http.scaladsl.model._
import akka.http.scaladsl.marshalling.Marshal

import spray.json._

import io.atlabs._

import horus.core.http.client.ATHttpClientT
import horus.core.snoop.SnoopErrorPublisherT
import horus.core.util.ATCCPrinter

import com.africasTalking._

import elmer.core.config.ElmerConfig
import elmer.core.util.ElmerEnum.{ FoodName, FoodOrderStatus }

import OrderRequestGatewayMarshalling._

private[request] object OrderRequestGateway {

  case class FoodOrderGatewayRequest(
    name: FoodName.Value,
    quantity: Int
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

  implicit val system         = context.system
  private val orderRequestUrl = ElmerConfig.etherOrderRequestUrl

  import OrderRequestGateway._
  override def receive = {
    case req: FoodOrderGatewayRequest =>
      log.info("processing " + req)
      val currentSender = sender
      val sendFut = for {
        entity   <- Marshal(
          EtherFoodOrderRequest(
            name     = req.name,
            quantity = req.quantity
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
