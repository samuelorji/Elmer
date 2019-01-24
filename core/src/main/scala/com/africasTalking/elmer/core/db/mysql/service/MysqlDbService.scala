package com.africasTalking.elmer.core
package db.mysql.service

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.{ Actor, ActorLogging }
import akka.pattern.pipe

import com.github.mauricio.async.db.mysql.MySQLQueryResult

import io.atlabs._

import horus.core.db.mysql.cache.MysqlDbCacheEntryT
import horus.core.snoop.SnoopErrorPublisherT
import horus.core.util.{ ATCash, ATCCPrinter, ATSecureCCPrinter }

import com.africasTalking._

import elmer.core.db.mysql.mapper._

object ElmerMysqlDbService {

  case class UserDbEntry (
    id: Int,
    username: String,
    apikey: String
  ) extends MysqlDbCacheEntryT with ATSecureCCPrinter {
    override def getSecureFields = Set("apikey")
  }
  case object UserFetchDbQuery

}

class ElmerMysqlDbService extends Actor
    with ActorLogging
    with SnoopErrorPublisherT {

  import ElmerMysqlDbService._
  def receive = {

    case UserFetchDbQuery =>
      UserMapper.fetchAll.mapTo[List[UserDbEntry]] pipeTo sender
  }

}
