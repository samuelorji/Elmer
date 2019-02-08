package com.africasTalking.elmer.order
package request

import java.net.URL
import java.util.UUID

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

import elmer.core.config.ElmerConfig
import elmer.core.util.ElmerEnum.{ FoodName, FoodOrderStatus }

import gateway.OrderRequestGateway

object OrderRequestService {

  case class FoodOrderServiceRequest(
    name: FoodName.Value,
    quantity: Int,
    callbackUrl: Option[URL]
  ) extends ATCCPrinter

  case class FoodOrderServiceResponse(
    transactionId: String,
    status: FoodOrderStatus.Value,
    description: String
  ) extends ATCCPrinter

}

class OrderRequestService extends Actor    
    with ActorLogging
    with SnoopErrorPublisherT {

  implicit val timeout = Timeout(ATConfig.httpRequestTimeout)

  private val gateway  = createGateway
  def createGateway    = context.actorOf(Props[OrderRequestGateway])

  private val clientTransactionIdPrefix = ElmerConfig.clientTransactionIdPrefix

  import OrderRequestGateway._
  import OrderRequestService._
  override def receive = {
    case req: FoodOrderServiceRequest =>
      log.info("processing " + req)
      val currentSender = sender
      val transactionId = clientTransactionIdPrefix + UUID.randomUUID.toString

      (gateway ? FoodOrderGatewayRequest(
        transactionId = transactionId,
        name          = req.name,
        quantity      = req.quantity,
        callbackUrl   = req.callbackUrl
      )).mapTo[FoodOrderGatewayResponse] onComplete {
        case Success(response) =>
          currentSender ! FoodOrderServiceResponse(
            transactionId = transactionId,
            status        = response.status,
            description   = response.description
          )
        case Failure(error)    =>
          publishError("Error while processing " + req, Some(error))
          currentSender ! FoodOrderServiceResponse(
            transactionId = transactionId,
            status        = FoodOrderStatus.Failed,
            description   = "Internal error while processing request"
          )
      }

  }

}
