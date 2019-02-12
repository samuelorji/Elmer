package com.africasTalking.elmer.core
package db.redis

import io.atlabs._

import horus.core.db.redis.ATRedisDbT

import com.africasTalking._

import elmer.core.config.ElmerConfig

object ElmerRedisDb extends ATRedisDbT  {  
  val host       = ElmerConfig.elmerRedisDbHost
  val port       = ElmerConfig.elmerRedisDbPort
  val numWorkers = ElmerConfig.elmerRedisDbNumWorkers
}
