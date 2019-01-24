package com.africasTalking.elmer.core
package db.mysql.cache

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.Props

import akka.testkit.TestProbe

import io.atlabs._

import horus.core.util._

import com.africasTalking._

import elmer.core.db.mysql.service.ElmerMysqlDbService._
import elmer.core.test._

object TestAuthenticationDbCache extends AuthenticationDbCacheT

class AuthenticationDbCacheSpec extends TestServiceT {

  val testUser1  = UserDbEntry(
    id       = 1,
    username = "UserName1",
    apikey   = ATUtil.sha256Hash("testApiKey1")
  )
  val testUser2  = UserDbEntry(
    id       = 2,
    username = "UserName2",
    apikey   = ATUtil.sha256Hash("testApiKey2")
  )
  val mysqlProbe = TestProbe()
  "AuthenticationDbCache" must {
    "fetch entries from the database on initialization" in {
      TestAuthenticationDbCache.initialize(
        system.actorOf(Props(new AuthenticationDbCache(TestAuthenticationDbCache){
          override def createMysqlDbService = mysqlProbe.ref
        }))
      )
      mysqlProbe.expectMsg(
        5 seconds,
        UserFetchDbQuery
      )
      mysqlProbe.reply(
        List[UserDbEntry](
          testUser1,
          testUser2
        )
      )
      expectNoMessage(2 seconds)
    }
    "authentication users correctly" in {
      TestAuthenticationDbCache.authenticate(
        username = "UserName1",
        apikey   = "testApiKey1"
      ) should be (Some(testUser1))
      TestAuthenticationDbCache.authenticate(
        username = "UserName2",
        apikey   = "testApiKey2"
      ) should be (Some(testUser2))

      TestAuthenticationDbCache.authenticate(
        username = "userName1",
        apikey   = "testApiKey1"
      ) should be (Some(testUser1))
      TestAuthenticationDbCache.authenticate(
        username = "username1",
        apikey   = "testApiKey1"
      ) should be (Some(testUser1))

      TestAuthenticationDbCache.authenticate(
        username = "username1",
        apikey   = "testapikey1"
      ) should be (None)

      TestAuthenticationDbCache.authenticate(
        username = "username1",
        apikey   = "testApiKey11"
      ) should be (None)
      TestAuthenticationDbCache.authenticate(
        username = "username11",
        apikey   = "testApiKey1"
      ) should be (None)
    }
  }
}
