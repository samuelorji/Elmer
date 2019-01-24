package com.africasTalking.elmer.core
package util

import io.atlabs._

import horus.core.util.HorusCoreServiceT

import com.africasTalking._

import elmer.core.db.mysql.cache._

trait ElmerCoreServiceT extends HorusCoreServiceT {

  AuthenticationDbCache.initialize(actorRefFactory.actorOf(
    AuthenticationDbCache.props
  ))

}
