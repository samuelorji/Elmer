package com.africasTalking.elmer.order
package request

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.Props
import akka.testkit.TestProbe

import com.africasTalking._

import elmer.core.util.ElmerEnum._

import elmer.order.test._

import gateway._
import OrderRequestGateway._

import OrderRequestService._

class OrderRequestServiceSpec extends TestServiceT {

  val gatewayProbe        = TestProbe()
  val OrderRequestService = system.actorOf(Props(new OrderRequestService {
    override def createGateway = gatewayProbe.ref
  }))

  "The OrderRequestService" must {
    "forward a request and process a gateway response" in {
      OrderRequestService ! FoodOrderServiceRequest(
        name        = FoodName.Ugali,
        quantity    = 3,
        callbackUrl = None
      )

      val gatewayRequest = gatewayProbe.expectMsgType[FoodOrderGatewayRequest]
      val transactionId  = gatewayRequest.transactionId

      gatewayRequest should be (FoodOrderGatewayRequest(
        transactionId = transactionId,
        name          = FoodName.Ugali,
        quantity      = 3,
        callbackUrl   = None
      ))
      gatewayProbe.reply(FoodOrderGatewayResponse(
        status      = FoodOrderStatus.Accepted,
        description = "Order accepted for processing"
      ))

      expectMsg(FoodOrderServiceResponse(
        transactionId = transactionId,
        status        = FoodOrderStatus.Accepted,
        description   = "Order accepted for processing"
      ))

      gatewayProbe.expectNoMessage(100 millis)
    }
  }

}
