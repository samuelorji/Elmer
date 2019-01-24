package com.africasTalking.elmer.core
package db.mysql.cache

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.Props
import akka.pattern.{ ask, pipe }
import akka.util.Timeout

import io.atlabs._

import horus.core.config.ATConfig
import horus.core.db.mysql.cache.{ MysqlDbCacheManagerT, UpdateCacheRequestImpl }
import horus.core.util.{ ATCCPrinter, ATLogT, ATUtil }

import com.africasTalking._

import elmer.core.config.ElmerConfig
import elmer.core.db.mysql.service.ElmerMysqlDbService
import ElmerMysqlDbService._

object AuthenticationDbCache extends AuthenticationDbCacheT {

  private[cache] case class AuthKey(
    username: String,
    apikey: String
  ) extends ATCCPrinter

}

private[cache] trait AuthenticationDbCacheT extends MysqlDbCacheManagerT[UserDbEntry]
    with ATLogT {

  import AuthenticationDbCache._

  def authenticate(
    username: String,
    apikey: String
  ): Option[UserDbEntry] = authenticationMap.get(AuthKey(
    username = username.toLowerCase,
    apikey   = ATUtil.sha256Hash(apikey)
  ))

  def props = Props(classOf[AuthenticationDbCache], this)

  override def setEntries(x: List[UserDbEntry]) {
    super.setEntries(x)
    setAuthenticationMap(
      x.foldLeft(Map[AuthKey, UserDbEntry]()) {
        case (m, entry) =>
          m.updated(
            AuthKey(
              username = entry.username.toLowerCase,
              apikey   = entry.apikey
            ),
            entry
          )
      }
    )
  }

  private var authenticationMap = Map[AuthKey, UserDbEntry]()
  private def setAuthenticationMap(map: Map[AuthKey, UserDbEntry]) {
    authenticationMap = map
  }
}

private[core] class AuthenticationDbCache(
  val manager: AuthenticationDbCacheT
) extends ElmerMysqlDbCacheT[UserDbEntry] {

  implicit val timeout          = Timeout(ATConfig.mysqlDbTimeout)

  override val updateFrequency  = ElmerConfig.mysqlDbAuthenticationCacheUpdateFrequency

  import AuthenticationDbCache._
  override protected def specificReceive = {
    case UpdateCacheRequestImpl =>
      (mysqlDbService ? UserFetchDbQuery).mapTo[List[UserDbEntry]] pipeTo sender
  }
}
