package com.africasTalking.elmer.core
package db.cassandra

import io.atlabs._

import horus.core.db.cassandra.ATCassandraDbConnector
import ATCassandraDbConnector.{ AuthParams, ConnectorParams }
import horus.core.util.ATEnum.ATEnvironment

import com.africasTalking._

import elmer.core.config.ElmerConfig

private[cassandra] object ElmerCassandraDbConnector extends ATCassandraDbConnector(
  params = ConnectorParams(
    port                  = ElmerConfig.cassandraPort,
    hosts                 = ElmerConfig.cassandraHosts,
    keySpace              = ElmerConfig.cassandraKeySpace,
    auth                  = ElmerConfig.getEnvironment match {
      case ATEnvironment.Development => None
      case _                         => Some(
        AuthParams(
          username = ElmerConfig.cassandraUsername,
          password = ElmerConfig.cassandraPassword
        )
      )
    }
  )
)
