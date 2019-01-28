package com.africasTalking.elmer.core
package db.mysql.cache

import akka.actor.Props

import io.atlabs._

import horus.core.db.mysql.cache.{ MysqlDbCacheEntryT, MysqlDbCacheT }

import com.africasTalking._

import elmer.core.db.mysql.service.ElmerMysqlDbService

trait ElmerMysqlDbCacheT[EntryT <: MysqlDbCacheEntryT] extends MysqlDbCacheT[EntryT] {

  override def createMysqlDbService = context.actorOf(Props[ElmerMysqlDbService])

}
