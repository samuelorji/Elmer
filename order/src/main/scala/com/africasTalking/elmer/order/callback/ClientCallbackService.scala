package com.africasTalking.elmer.order
package callback

import java.net.URL

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

import akka.actor.{ Actor, ActorLogging }
import akka.http.scaladsl.model._
import akka.http.scaladsl.marshalling.Marshal

import spray.json._

import io.atlabs._

import horus.core.http.client.ATHttpClientT
import horus.core.util.{ ATCCPrinter, ATUtil }

import com.africasTalking._

import elmer.core.util.ElmerEnum.FoodOrderStatus

import ClientCallbackServiceMarshalling._

private[order] object ClientCallbackService {

  case class ClientCallbackServiceRequest(
    transactionId: String,
    status: FoodOrderStatus.Value,
    description: String,
    callbackUrl: URL
  ) extends ATCCPrinter

}

private[order] class ClientCallbackService extends Actor
    with ActorLogging
    with ATHttpClientT
    with ClientCallbackServiceJsonSupportT {

  implicit val system = context.system

  import ClientCallbackService._
  override def receive = {

    case req: ClientCallbackServiceRequest =>
      log.info("processing " + req)
      val sendFut = for {
        entity   <- Marshal(
          ClientFoodOrderStatusRequest(
            transactionId = req.transactionId,
            status        = req.status,
            description   = req.description
          )
        ).to[MessageEntity]
        request   = HttpRequest(
          method = HttpMethods.POST,
          uri    = req.callbackUrl.toString,
          entity = entity
        )
        response <- sendHttpRequest(request)
      } yield (request, response)
      sendFut onComplete {
        case Success((request, response)) =>
          log.info("Processed {} => {}", request, response)
        case Failure(error)    =>
          log.warning("Failed to send callback notification for {} => {}" + req, ATUtil.getStackTrace(error))
      }
  }

}
