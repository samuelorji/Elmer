package com.africasTalking.elmer.core
package db.cassandra

import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.dsl._

import mapper._

private[cassandra] object ElmerCassandraDb extends ElmerCassandraDb(ElmerCassandraDbConnector.connector)

private[cassandra] sealed class ElmerCassandraDb(override val connector: CassandraConnection) extends Database[ElmerCassandraDb](connector) {

  object FoodOrderRequestMapper extends FoodOrderRequestMapper with connector.Connector
  object FoodOrderStatusMapper extends FoodOrderStatusMapper with connector.Connector
}
