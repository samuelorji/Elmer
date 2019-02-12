package com.africasTalking.elmer.order
package status

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.Props
import akka.testkit.TestProbe

import spray.json._

import io.atlabs._

import horus.core.db.redis.RedisDbService._
import horus.core.util.ATUtil

import com.africasTalking._

import elmer.core.util.ElmerEnum._

import elmer.order.callback.ClientCallbackService._
import elmer.order.status.OrderStatusMessage._
import elmer.order.test._
import elmer.order.util.OrderJsonProtocol._

import OrderStatusService._

class OrderStatusServiceSpec extends TestServiceT {

  val redisDbServiceProbe        = TestProbe()
  val clientCallbackServiceProbe = TestProbe()
  val orderStatusService         = system.actorOf(Props(new OrderStatusService {
    override def createRedisDbService        = redisDbServiceProbe.ref
    override def createClientCallbackService = clientCallbackServiceProbe.ref
  }))

  "The OrderStatusservice" must {
    "process a status update correctly" in {

      orderStatusService ! FoodOrderStatusServiceRequest(
        brokerTransactionId = "SomeGwTxnId",
        status              = FoodOrderStatus.Delivered,
        description         = "The food has been delivered"
      )

      val fetchElementQuery = FetchElementQuery(
        key = "ElOrPSE_SomeGwTxnId"        
      )
      val callbackUrl       = ATUtil.parseUrl("http://www.test.com/callback").get      
      redisDbServiceProbe.expectMsg(fetchElementQuery)
      redisDbServiceProbe.reply(FetchElementResult(
        query = fetchElementQuery,
        value = Some(
          FoodOrderPendingStatusElement(
            transactionId = "SomeTxnId",
            callbackUrl   = Some(callbackUrl)
          ).toJson.compactPrint
        )
      ))

      clientCallbackServiceProbe.expectMsg(
        ClientCallbackStatusServiceRequest(
          transactionId = "SomeTxnId",
          status        = FoodOrderStatus.Delivered,
          description   = "The food has been delivered",
          callbackUrl   = callbackUrl
        )
      )

      clientCallbackServiceProbe.expectNoMessage(100 millis)
      redisDbServiceProbe.expectNoMessage(100 millis)
    }
  }

}
