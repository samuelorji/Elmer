package com.africasTalking.elmer.order
package status

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }
import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.ask
import akka.util.Timeout

import spray.json._

import io.atlabs._

import horus.core.config.ATConfig
import horus.core.db.redis.RedisDbService
import horus.core.snoop.SnoopErrorPublisherT
import horus.core.util.ATCCPrinter

import com.africasTalking._

import elmer.core.config.ElmerConfig
import elmer.core.db.redis.ElmerRedisDb
import elmer.core.util.ElmerEnum.FoodOrderStatus

import elmer.order.callback.ClientCallbackService
import elmer.order.util.OrderJsonProtocol

import OrderStatusMessage.FoodOrderPendingStatusElement

object OrderStatusService {

  case class FoodOrderStatusServiceRequest(
    brokerTransactionId: String,
    status: FoodOrderStatus.Value,
    description: String
  ) extends ATCCPrinter
}

class OrderStatusService extends Actor
    with ActorLogging
    with SnoopErrorPublisherT {

  implicit val timeout              = Timeout(ATConfig.httpRequestTimeout)

  private lazy val redisDbService   = createRedisDbService
  def createRedisDbService          = ElmerRedisDb.getInstance

  private val clientCallbackService = createClientCallbackService
  def createClientCallbackService   = context.actorOf(Props[ClientCallbackService])

  private val statusElementRedisPrefix = ElmerConfig.orderPendingStatusElementRedisPrefix

  import RedisDbService._
  import ClientCallbackService._
  import OrderStatusService._
  import OrderJsonProtocol._
  override def receive = {

    case req: FoodOrderStatusServiceRequest =>
      log.info("processing " + req)

      (redisDbService ? FetchElementQuery(
        key = statusElementRedisPrefix + req.brokerTransactionId
      )).mapTo[FetchElementResult] onComplete {
        case Failure(error)  =>
          publishError("Error while fetching pending status element from redis for " + req, Some(error))
        case Success(result) =>
          result.value match {
            case None          =>
              publishError("Could not find pending status element in redis for " + req)
            case Some(payload) =>
              try {
                val pendingStatusElement = payload.parseJson.convertTo[FoodOrderPendingStatusElement]
                pendingStatusElement.callbackUrl match {
                  case None      =>
                  case Some(url) =>
                    clientCallbackService ! ClientCallbackServiceRequest(
                      transactionId = pendingStatusElement.transactionId,
                      status        = req.status,
                      description   = req.description,
                      callbackUrl   = url
                    )
                }
              } catch {
                case ex: JsonParser.ParsingException =>
                  publishError(s"Error while parsing stored status element $result for request $req", Some(ex))
              }
          }

      }

  }
  
}
