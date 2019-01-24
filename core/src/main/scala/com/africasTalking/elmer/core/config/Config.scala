package com.africasTalking.elmer.core
package config

import io.atlabs._

import horus.core.config.ATBaseConfigT
import horus.core.util.ATUtil

object ElmerConfig extends ATBaseConfigT {

  // Web Interface
  val webHost = config.getString("elmer.interface.web.host")
  val webPort = config.getInt("elmer.interface.web.port")

  // db
  // mysql
  val mysqlDbAuthenticationCacheUpdateFrequency = ATUtil.parseFiniteDuration(config.getString("elmer.db.mysql.cache.update-frequency.authentication")).get

  val mysqlDbHost  = config.getString("elmer.db.mysql.host")
  val mysqlDbPort  = config.getInt("elmer.db.mysql.port")
  val mysqlDbUser  = config.getString("elmer.db.mysql.user")
  val mysqlDbPass  = config.getString("elmer.db.mysql.pass")
  val mysqlDbName  = config.getString("elmer.db.mysql.name")

  val mysqlDbPoolMaxObjects   = config.getInt("elmer.db.mysql.pool.max-objects")
  val mysqlDbPoolMaxIdle      = config.getInt("elmer.db.mysql.pool.max-idle")
  val mysqlDbPoolMaxQueueSize = config.getInt("elmer.db.mysql.pool.max-queue-size")

  // Brokers
  val etherOrderRequestUrl = config.getString("elmer.broker.ether.order-request-url")

}
