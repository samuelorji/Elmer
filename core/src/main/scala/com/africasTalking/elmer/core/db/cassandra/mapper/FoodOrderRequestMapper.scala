package com.africasTalking.elmer.core
package db.cassandra.mapper

import java.net.URL

import scala.concurrent.Future

import org.joda.time.DateTime

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.dsl.context

import io.atlabs._

import horus.core.db.cassandra.ATCassandraTableT
import horus.core.util.ATUtil

import com.africasTalking._

import elmer.core.db.cassandra.service.FoodOrderCassandraDbService.FoodOrderRequestDbEntry
import elmer.core.util.ElmerEnum.FoodName

private[mapper] sealed trait FoodOrderRequestMapperT extends ATCassandraTableT[FoodOrderRequestMapper, FoodOrderRequestDbEntry] {

  object user_id extends IntColumn with PartitionKey
  object insertion_day extends IntColumn with PrimaryKey
  object insertion_time extends DateTimeColumn
  object transaction_id extends StringColumn
  object food_name extends StringColumn with Index
  object quantity extends IntColumn
  object callback_url extends OptionalStringColumn

}

private[cassandra] abstract class FoodOrderRequestMapper extends FoodOrderRequestMapperT {

  override lazy val tableName = "food_order_request"

  override def fromRow(row: Row): FoodOrderRequestDbEntry = 
    FoodOrderRequestDbEntry(
      userId        = user_id(row),
      insertionDay  = insertion_day(row),
      insertionTime = insertion_time(row),
      transactionId = transaction_id(row),
      foodName      = FoodName.withName(food_name(row)),
      quantity      = quantity(row),
      callbackUrl   = callback_url(row) match {
        case Some(x) => ATUtil.parseUrl(x)
        case None    => None
      }
    )
 
  def fetchAll(
    userId: Int,
    start: Option[Int],
    limit: Int
  ): Future[Iterator[FoodOrderRequestDbEntry]] =
    fetchImpl(
      query = select
        .where(_.user_id eqs userId),
      start = start,
      limit = limit
    )
 
  def fetchByFoodName(
    userId: Int,
    foodName: FoodName.Value,
    start: Option[Int],
    limit: Int
  ): Future[Iterator[FoodOrderRequestDbEntry]] =
    fetchImpl(
      query = select
        .where(_.user_id eqs userId)
        .and(_.food_name eqs foodName.toString),
      start = start,
      limit = limit
    )
 
  def fetchByDateRange(
    userId: Int,
    startDate: Int,
    endDate: Int,
    start: Option[Int],
    limit: Int
  ): Future[Iterator[FoodOrderRequestDbEntry]] = 
    fetchImpl(
      query = select
        .where(_.user_id eqs userId)
        .and(_.insertion_day gte startDate)
        .and(_.insertion_day lte endDate),
      start = start,
      limit = limit
    )

  def fetchByDateRangeAndFoodName(
    userId: Int,
    startDate: Int,
    endDate: Int,
    foodName: FoodName.Value,
    start: Option[Int],
    limit: Int
  ): Future[Iterator[FoodOrderRequestDbEntry]] = 
    fetchImpl(
      query = select
        .where(_.user_id eqs userId)
        .and(_.insertion_day gte startDate)
        .and(_.insertion_day lte endDate)
        .and(_.food_name eqs foodName.toString),
      start = start,
      limit = limit
    )

  def insertNewRecord(
    userId: Int,
    transactionId: String,
    foodName: FoodName.Value,
    quantity: Int,
    callbackUrl: Option[URL],
  ) : Future[ResultSet] = {
    val insertionTime  = DateTime.now
    val insertionDay   = insertionTime.toString("yyyyMMdd").toInt;
    insertRecordImpl(
      insert
        .value(_.user_id, userId)
        .value(_.insertion_day, insertionDay)
        .value(_.insertion_time, insertionTime)
        .value(_.transaction_id, transactionId)
        .value(_.food_name, foodName.toString)
        .value(_.quantity, quantity)
        .value(_.callback_url, callbackUrl match {
          case Some(x) => Some(x.toString)
          case None    => None
        })
    )
  }
}



