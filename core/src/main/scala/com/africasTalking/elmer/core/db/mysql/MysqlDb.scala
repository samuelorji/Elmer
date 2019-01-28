package com.africasTalking.elmer.core
package db.mysql

import com.github.mauricio.async.db.Configuration
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory
import com.github.mauricio.async.db.pool.{ ConnectionPool, PoolConfiguration }

import com.africasTalking._

import elmer.core.config.ElmerConfig

private[mysql] object ElmerMysqlDb {

  private val configuration = new Configuration(
    username = ElmerConfig.mysqlDbUser,
    host     = ElmerConfig.mysqlDbHost,
    port     = ElmerConfig.mysqlDbPort,
    password = Some(ElmerConfig.mysqlDbPass),
    database = Some(ElmerConfig.mysqlDbName)
  )

  private val poolConfiguration = new PoolConfiguration(
    maxObjects   = ElmerConfig.mysqlDbPoolMaxObjects,
    maxIdle      = ElmerConfig.mysqlDbPoolMaxIdle,
    maxQueueSize = ElmerConfig.mysqlDbPoolMaxQueueSize
  )


  private val factory   = new MySQLConnectionFactory(configuration)
  private lazy val pool = new ConnectionPool(factory, poolConfiguration)

}

private[mysql] trait ElmerMysqlDb {
  implicit lazy val pool = ElmerMysqlDb.pool
}
