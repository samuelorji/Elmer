package com.africasTalking.elmer.core
package db.cassandra.service

import java.net.URL

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.{ Actor, ActorLogging }
import akka.pattern.pipe

import org.joda.time.DateTime

import com.datastax.driver.core.ResultSet

import io.atlabs._

import horus.core.db.cassandra.CassandraDbQueryResult
import horus.core.snoop.SnoopErrorPublisherT
import horus.core.util.ATCCPrinter

import com.africasTalking._

import elmer.core.db.cassandra.ElmerCassandraDb
import elmer.core.util.ElmerEnum.{ FoodName, FoodOrderStatus }

object FoodOrderCassandraDbService {

  case class FoodOrderRequestDbEntry (
    userId: Int,
    insertionDay: Int,
    insertionTime: DateTime,
    transactionId: String,
    foodName: FoodName.Value,
    quantity: Int,
    callbackUrl: Option[URL]
  ) extends ATCCPrinter

  case class FoodOrderRequestCreateDbQuery(
    userId: Int,
    transactionId: String,
    foodName: FoodName.Value,
    quantity: Int,
    callbackUrl: Option[URL]
  ) extends ATCCPrinter

  case class FoodOrderRequestFetchDbQuery(
    userId: Int,
    start: Option[Int],
    limit: Int,
    startDate: Option[Int] = None,
    endDate: Option[Int] = None,
    foodName: Option[FoodName.Value] = None
  ) extends ATCCPrinter

  case class FoodOrderStatusDbEntry (
    transactionId: String,
    status: FoodOrderStatus.Value,
    description: String
  ) extends ATCCPrinter

  case class FoodOrderStatusCreateDbQuery(
    transactionId: String,
    status: FoodOrderStatus.Value,
    description: String
  ) extends ATCCPrinter

  case class FoodOrderStatusFindDbQuery(
    transactionId: String
  ) extends ATCCPrinter
}

class FoodOrderCassandraDbService extends Actor with ActorLogging with SnoopErrorPublisherT {

  import ElmerCassandraDb._
  import FoodOrderCassandraDbService._
  def receive = {
    case x: FoodOrderRequestCreateDbQuery =>
      log.info("processing " + x)
      val currentSender = sender
      FoodOrderRequestMapper.insertNewRecord(
        userId        = x.userId,
        transactionId = x.transactionId,
        foodName      = x.foodName,
        quantity      = x.quantity,
        callbackUrl   = x.callbackUrl
      ).mapTo[ResultSet] map { x => currentSender ! CassandraDbQueryResult(x) }

    case FoodOrderRequestFetchDbQuery(userId, start, limit, None, None, None) =>
      FoodOrderRequestMapper.fetchAll(userId, start, limit).mapTo[Iterator[FoodOrderRequestDbEntry]] pipeTo sender

    case FoodOrderRequestFetchDbQuery(userId, start, limit, Some(startDate), Some(endDate), None) =>
      FoodOrderRequestMapper.fetchByDateRange(userId, startDate, endDate, start, limit).mapTo[Iterator[FoodOrderRequestDbEntry]] pipeTo sender

    case FoodOrderRequestFetchDbQuery(userId, start, limit, Some(startDate), Some(endDate), Some(foodName)) =>
      FoodOrderRequestMapper.fetchByDateRangeAndFoodName(userId, startDate, endDate, foodName, start, limit).mapTo[Iterator[FoodOrderRequestDbEntry]] pipeTo sender

    case FoodOrderRequestFetchDbQuery(userId, start, limit, None, None, Some(foodName)) =>
      FoodOrderRequestMapper.fetchByFoodName(userId, foodName, start, limit).mapTo[Iterator[FoodOrderRequestDbEntry]] pipeTo sender

    case x: FoodOrderRequestFetchDbQuery =>
      publishError("Received invalid query: " + x)
      sender ! Iterator[FoodOrderRequestDbEntry]()

    case x: FoodOrderStatusCreateDbQuery =>
      log.info("processing " + x)
      val currentSender = sender
      FoodOrderStatusMapper.insertNewRecord(
        transactionId = x.transactionId,
        status        = x.status,
        description   = x.description
      ).mapTo[ResultSet] map { x => currentSender ! CassandraDbQueryResult(x) }

    case FoodOrderStatusFindDbQuery(transactionId) =>
      FoodOrderStatusMapper.findByTransactionId(transactionId).mapTo[Option[FoodOrderStatusDbEntry]] pipeTo sender
  }
}
