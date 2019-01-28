package com.africasTalking.elmer.core
package test

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }

import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

import com.africasTalking._

import elmer.core.util.ElmerCoreServiceT

abstract class TestServiceT extends TestKit(ActorSystem("TestSystem"))
    with ImplicitSender
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll
    with ElmerCoreServiceT {

  override def snoopServiceName = "TestService"
  override def actorRefFactory  = system

  override def beforeAll {
    Thread.sleep(2000)
  }

  override def afterAll {
    Thread.sleep(2000)
    TestKit.shutdownActorSystem(system)
  }
}
