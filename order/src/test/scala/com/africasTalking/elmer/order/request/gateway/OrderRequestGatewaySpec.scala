package com.africasTalking.elmer.order
package request.gateway

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.Props
import akka.http.scaladsl.model._
import akka.testkit.TestProbe

import spray.json._

import io.atlabs._

import horus.core.db.redis.RedisDbService._
import horus.core.http.client._
import horus.core.util.ATUtil

import com.africasTalking._

import elmer.core.util.ElmerEnum._

import elmer.order.status.OrderStatusMessage._
import elmer.order.test._
import elmer.order.util.OrderJsonProtocol._

import OrderRequestGateway._
import OrderRequestGatewayMarshalling._

class OrderRequestGatewaySpec extends TestHttpStringEndpointT
    with  OrderRequestGatewayJsonSupportT {

  val redisDbServiceProbe = TestProbe()
  val gateway             = system.actorOf(Props(new OrderRequestGateway {
    override def createRedisDbService              = redisDbServiceProbe.ref
    override def sendHttpRequest(req: HttpRequest) =
      Future.successful(getStringHttpResponse(req))
  }))

  val callbackUrl = ATUtil.parseUrl("http://www.test.com/callback").get

  "The OrderRequestGateway" must {
    "process a valid JSON response from the broker" in {
      gateway ! FoodOrderGatewayRequest(
        transactionId = "SomeTxnId",
        name          = FoodName.Ugali,
        quantity      = 3,
        callbackUrl   = Some(callbackUrl)
      )

      expectMsg(FoodOrderGatewayResponse(
        status      = FoodOrderStatus.Accepted,
        description = "Order accepted for processing"
      ))

      val addElementQuery = redisDbServiceProbe.expectMsgType[AddElementQuery]
      addElementQuery should be (AddElementQuery(
        key      = "ElOrPSE_SomeGwTxnId",
        value    = FoodOrderPendingStatusElement(
          transactionId = "SomeTxnId",
          callbackUrl   = Some(callbackUrl)
        ).toJson.compactPrint,
        lifetime = Some(FiniteDuration(30, MINUTES))
      ))
      redisDbServiceProbe.reply(AddElementResult(
        query   = addElementQuery,
        success = true
      ))

      redisDbServiceProbe.expectNoMessage(100 millis)
    }
    "process a valid status code with invalid JSON from the broker" in {
      gateway ! FoodOrderGatewayRequest(
        transactionId = "SomeTxnId",
        name          = FoodName.Rice,
        quantity      = 3,
        callbackUrl   = Some(callbackUrl)
      )
      expectMsg(FoodOrderGatewayResponse(
        status      = FoodOrderStatus.Failed,
        description = "Internal error while sending request to the gateway"
      ))

      redisDbServiceProbe.expectNoMessage(100 millis)
    }
    "process an invalid status code from the broker" in {
      gateway ! FoodOrderGatewayRequest(
        transactionId = "SomeTxnId",
        name          = FoodName.BeefStew,
        quantity      = 3,
        callbackUrl   = Some(callbackUrl)
      )
      expectMsg(FoodOrderGatewayResponse(
        status      = FoodOrderStatus.Failed,
        description = "Internal error while sending request to the gateway"
      ))

      redisDbServiceProbe.expectNoMessage(100 millis)
    }

  }

  def getStringHttpResponseImpl(
    data: String,
    uri: Uri
  ) = {
    val request = data.parseJson.convertTo[EtherFoodOrderRequest]
    request.name match {
      case FoodName.Ugali =>
        ATHttpClientResponse(
          StatusCodes.OK,
          EtherFoodOrderResponse(
            transactionId = "SomeGwTxnId",
            status        = FoodOrderStatus.Accepted,
            description   = "Order accepted for processing"
          ).toJson.compactPrint
        )
      case FoodName.Rice =>
        ATHttpClientResponse(
          StatusCodes.OK,
          "Invalid JSON"
        )
      case _ =>
        ATHttpClientResponse(
          StatusCodes.BadRequest,
          "Invalid request"
        )
    }
  }
}
