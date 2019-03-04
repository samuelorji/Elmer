package com.africasTalking.elmer.core
package db.cassandra.service

import java.util.UUID

import scala.language.postfixOps

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.Success

import akka.actor.Props
import akka.pattern.ask
import akka.testkit.TestProbe

import spray.json._

import org.joda.time.DateTime

import io.atlabs._

import horus.core.db.cassandra._
import horus.core.util._

import com.africasTalking._

import elmer.core.test._
import elmer.core.util.ElmerEnum._

import FoodOrderCassandraDbService._

class FoodOrderCassandraDbServiceSpec extends TestServiceT {

  val insertionDay   = DateTime.now.toString("yyyyMMdd").toInt;
  val userId         = 1

  val transactionId1 = UUID.randomUUID.toString
  val foodName1      = FoodName.Ugali
  val quantity1      = 5
  val callbackUrl1   = ATUtil.parseUrl("http://www.callback.com/test").get

  val transactionId2 = UUID.randomUUID.toString
  val foodName2      = FoodName.BeefStew
  val quantity2      = 6
  val callbackUrl2   = ATUtil.parseUrl("http://www.callback.com/test1").get

  val status         = FoodOrderStatus.Delivered
  val description    = "The food has been delivered"

  val dbService     = system.actorOf(Props[FoodOrderCassandraDbService])

  "FoodOrderCassandraDbService" must {
    "insert records correctly into the database" in {
      dbService ! FoodOrderRequestCreateDbQuery(
        userId        = userId,
        transactionId = transactionId1,
        foodName      = foodName1,
        quantity      = quantity1,
        callbackUrl   = Some(callbackUrl1)
      )
      expectMsg(new CassandraDbQueryResult(true))

      dbService ! FoodOrderRequestCreateDbQuery(
        userId        = userId,
        transactionId = transactionId2,
        foodName      = foodName2,
        quantity      = quantity2,
        callbackUrl   = Some(callbackUrl2)
      )
      expectMsg(new CassandraDbQueryResult(true))

    }
    "fetch results in the correct order when filtered by userId" in {
      dbService ! FoodOrderRequestFetchDbQuery(
        userId = userId,
        start  = Some(0),
        limit  = 2
      )
      val result = expectMsgClass(
        5 seconds,
        classOf[Iterator[FoodOrderRequestDbEntry]]
      )
      result.hasNext should be (true)
      val dbEntry2 = result.next
      dbEntry2.insertionDay should be (insertionDay)
      dbEntry2.transactionId should be (transactionId2)
      dbEntry2.foodName should be (foodName2)
      dbEntry2.quantity should be (quantity2)
      dbEntry2.callbackUrl should be (Some(callbackUrl2))

      result.hasNext should be (true)
      val dbEntry1 = result.next
      dbEntry1.insertionDay should be (insertionDay)
      dbEntry1.transactionId should be (transactionId1)
      dbEntry1.foodName should be (foodName1)
      dbEntry1.quantity should be (quantity1)
      dbEntry1.callbackUrl should be (Some(callbackUrl1))

      result.hasNext should be (false)
    }
    "fetch the second page when filtered by userId" in {
      dbService ! FoodOrderRequestFetchDbQuery(
        userId = userId,
        start  = Some(1),
        limit  = 1
      )
      val result = expectMsgClass(
        5 seconds,
        classOf[Iterator[FoodOrderRequestDbEntry]]
      )
      result.hasNext should be (true)
      val dbEntry = result.next
      dbEntry.insertionDay should be (insertionDay)
      dbEntry.transactionId should be (transactionId1)
      dbEntry.foodName should be (foodName1)
      dbEntry.quantity should be (quantity1)
      dbEntry.callbackUrl should be (Some(callbackUrl1))

      result.hasNext should be (false)
    }
    "fetch results when filtered by userId and foodName" in {
      dbService ! FoodOrderRequestFetchDbQuery(
        userId    = userId,
        start     = Some(0),
        limit     = 1,
        foodName  = Some(foodName1)
      )
      val result = expectMsgClass(
        5 seconds,
        classOf[Iterator[FoodOrderRequestDbEntry]]
      )

      result.hasNext should be (true)
      val dbEntry = result.next
      dbEntry.transactionId should be (transactionId1)
      
      result.hasNext should be (false)
    }
    "fetch results when filtered by userId and a date range" in {
      dbService ! FoodOrderRequestFetchDbQuery(
        userId    = userId,
        start     = Some(0),
        limit     = 2,
        startDate = Some(20120101),
        endDate   = Some(20991231)
      )
      val result = expectMsgClass(
        5 seconds,
        classOf[Iterator[FoodOrderRequestDbEntry]]
      )
      val dbEntry2 = result.next
      dbEntry2.transactionId should be (transactionId2)      

      result.hasNext should be (true)
      val dbEntry1 = result.next
      dbEntry1.transactionId should be (transactionId1)

      result.hasNext should be (false)
    }
    "fetch results when filtered by userId, a date range and a foodName" in {
      dbService ! FoodOrderRequestFetchDbQuery(
        userId    = userId,
        start     = Some(0),
        limit     = 1,
        foodName  = Some(foodName1),
        startDate = Some(20120101),
        endDate   = Some(20991231)
      )
      val result = expectMsgClass(
        5 seconds,
        classOf[Iterator[FoodOrderRequestDbEntry]]
      )
      result.hasNext should be (true)
      val dbEntry = result.next
      dbEntry.transactionId should be (transactionId1)

      result.hasNext should be (false)
    }
    "return an empty list for an invalid query" in {
      dbService ! FoodOrderRequestFetchDbQuery(
        userId    = userId,
        start     = Some(0),
        limit     = 1,
        foodName  = Some(foodName1),
        startDate = Some(20120101),
        endDate   = None
      )
      val result = expectMsgClass(
        5 seconds,
        classOf[Iterator[FoodOrderRequestDbEntry]]
      )
      result.hasNext should be (false)
    }

    "create a status record" in {
      dbService ! FoodOrderStatusCreateDbQuery(
        transactionId = transactionId1,
        status        = status,
        description   = description
      )
      expectMsg(new CassandraDbQueryResult(true))
    }
    "find the status record" in {
      dbService ! FoodOrderStatusFindDbQuery(transactionId1)
      val result = expectMsgClass(
        5 seconds,
        classOf[Option[FoodOrderStatusDbEntry]]
      )

      result should be (Some(FoodOrderStatusDbEntry(
        transactionId = transactionId1,
        status        = status,
        description   = description
      )))
    }
  }
}
