package com.africasTalking.elmer.core
package db.cassandra.mapper

import scala.concurrent.Future

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.dsl.context

import io.atlabs._

import horus.core.db.cassandra.ATCassandraTableT

import com.africasTalking._

import elmer.core.db.cassandra.service.FoodOrderCassandraDbService.FoodOrderStatusDbEntry
import elmer.core.util.ElmerEnum.FoodOrderStatus

private[mapper] sealed trait FoodOrderStatusMapperT extends ATCassandraTableT[FoodOrderStatusMapper, FoodOrderStatusDbEntry] {

  object transaction_id extends StringColumn with PartitionKey  
  object status extends StringColumn
  object description extends StringColumn

}

private[cassandra] abstract class FoodOrderStatusMapper extends FoodOrderStatusMapperT {

  override val tableName = "food_order_status"

  override def fromRow(row: Row): FoodOrderStatusDbEntry = 
    FoodOrderStatusDbEntry(
      transactionId = transaction_id(row),
      status        = FoodOrderStatus.withName(status(row)),
      description   = description(row)
    )


  def findByTransactionId(transactionId: String): Future[Option[FoodOrderStatusDbEntry]] = 
    select
      .where(_.transaction_id eqs transactionId)
      .one

  def insertNewRecord (
    transactionId: String,
    status: FoodOrderStatus.Value,
    description: String
  ) : Future[ResultSet] = 
    insertRecordImpl(
      insert
        .value(_.transaction_id, transactionId)
        .value(_.status, status.toString)
        .value(_.description, description)
    )
}
