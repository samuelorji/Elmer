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
import horus.core.db.cassandra.CassandraDbQueryResult
import horus.core.snoop.SnoopErrorPublisherT
import horus.core.util.ATCCPrinter

import com.africasTalking._

import elmer.core.config.ElmerConfig
import elmer.core.db.cassandra.service.FoodOrderCassandraDbService
import elmer.core.util.ElmerEnum.{ FoodName, FoodOrderStatus }

import gateway.OrderRequestGateway

object OrderRequestService {

  case class FoodOrderServiceRequest(
    userId: Int,
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

  implicit val timeout           = Timeout(ATConfig.httpRequestTimeout)

  private val cassandraDbService = createCassandraDbService
  def createCassandraDbService   = context.actorOf(Props[FoodOrderCassandraDbService])

  private val gateway            = createGateway
  def createGateway              = context.actorOf(Props[OrderRequestGateway])

  private val clientTransactionIdPrefix = ElmerConfig.clientTransactionIdPrefix

  import FoodOrderCassandraDbService._
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
          response.status match {
            case FoodOrderStatus.Accepted =>
              (cassandraDbService ? FoodOrderRequestCreateDbQuery(
                userId        = req.userId,
                transactionId = transactionId,
                foodName      = req.name,
                quantity      = req.quantity,
                callbackUrl   = req.callbackUrl
              )).mapTo[CassandraDbQueryResult] onComplete {
                case Success(x)     =>
                  if (!x.status) {
                    publishError(s"Error while adding $req to cassandra")
                  }
                case Failure(error) =>
                  publishError(s"Error while adding $req to cassandra", Some(error))
              }
            case _                        =>
          }
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
