package com.africasTalking.elmer.order
package request

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

import akka.actor.{ Actor, ActorLogging, Props }
import akka.pattern.ask
import akka.util.Timeout

import io.atlabs._

import horus.core.config.ATConfig
import horus.core.snoop.SnoopErrorPublisherT
import horus.core.util.ATCCPrinter

import com.africasTalking._

import elmer.core.util.ElmerEnum.{ FoodName, FoodOrderStatus }

import gateway.OrderRequestGateway

object OrderRequestService {

  case class FoodOrderServiceRequest(
    name: FoodName.Value,
    quantity: Int
  ) extends ATCCPrinter

  case class FoodOrderServiceResponse(
    status: FoodOrderStatus.Value,
    description: String
  ) extends ATCCPrinter

}

class OrderRequestService extends Actor
    with SnoopErrorPublisherT
    with ActorLogging {

  implicit val timeout = Timeout(ATConfig.httpRequestTimeout)

  private val gateway  = createGateway
  def createGateway    = context.actorOf(Props[OrderRequestGateway])

  import OrderRequestGateway._
  import OrderRequestService._
  override def receive = {
    case req: FoodOrderServiceRequest =>
      log.info("processing " + req)
      val currentSender = sender

      (gateway ? FoodOrderGatewayRequest(
        name     = req.name,
        quantity = req.quantity
      )).mapTo[FoodOrderGatewayResponse] onComplete {
        case Success(response) =>
          currentSender ! FoodOrderServiceResponse(
            status      = response.status,
            description = response.description
          )
        case Failure(error)    =>
          publishError(s"Error while processing " + req, Some(error))
          currentSender ! FoodOrderServiceResponse(
            status      = FoodOrderStatus.Failed,
            description = "Internal error while processing request"
          )
      }

  }

}
