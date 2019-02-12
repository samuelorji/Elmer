package com.africasTalking.elmer.core
package config

import io.atlabs._

import horus.core.config.ATBaseConfigT
import horus.core.util.ATUtil

object ElmerConfig extends ATBaseConfigT {

  // Web Interface
  val webHost = config.getString("elmer.interface.web.host")
  val webPort = config.getInt("elmer.interface.web.port")

  // order
  val clientTransactionIdPrefix            = config.getString("elmer.order.client-transaction-id-prefix")
  val orderPendingStatusElementLifetime    = ATUtil.parseFiniteDuration(config.getString("elmer.order.pending-status-element.lifetime")).get
  val orderPendingStatusElementRedisPrefix = config.getString("elmer.order.pending-status-element.redis-prefix")

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

  // redis
  val elmerRedisDbHost       = config.getString("elmer.db.redis.elmer.host")
  val elmerRedisDbPort       = config.getInt("elmer.db.redis.elmer.port")
  val elmerRedisDbNumWorkers = config.getInt("elmer.db.redis.elmer.num-workers")

  // Brokers
  val etherOrderRequestUrl        = config.getString("elmer.broker.ether.order-request-url")
  val etherOrderStatusCallbackUrl = config.getString("elmer.broker.ether.order-status-callback-url")

}
