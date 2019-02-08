package com.africasTalking.elmer.core
package util

import io.atlabs._

import horus.core.util.HorusCoreServiceT

import com.africasTalking._

import elmer.core.db.mysql.cache._
import elmer.core.db.redis.ElmerRedisDb

trait ElmerCoreServiceT extends HorusCoreServiceT {

  ElmerRedisDb.initialize(actorRefFactory)

  AuthenticationDbCache.initialize(actorRefFactory.actorOf(
    AuthenticationDbCache.props
  ))

}
