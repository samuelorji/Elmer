package com.africasTalking.elmer.core
package db.mysql.service

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.Props

import io.atlabs._

import horus.core.util.ATUtil

import com.africasTalking._

import elmer.core.db.mysql._
import elmer.core.test._

import ElmerMysqlDbService._

class ElmerMysqlDbServiceSpec extends TestServiceT {

  val dbService = system.actorOf(Props[ElmerMysqlDbService])

  "The ElmerMysqlDbService" must {
    "fetch all users correctly" in {
      dbService ! UserFetchDbQuery
      expectMsg(
        5 seconds,
        List(
          UserDbEntry(
            id       = 1,
            username = "testuser1",
            apikey   = ATUtil.sha256Hash("testpass1")
          ),
          UserDbEntry(
            id       = 2,
            username = "testuser2",
            apikey   = ATUtil.sha256Hash("testpass2")
          )
        )
      )
    }
  }
}

