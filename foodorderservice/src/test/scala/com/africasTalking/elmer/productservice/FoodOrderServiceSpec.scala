package com.africasTalking.elmer.productservice

import scala.concurrent.duration._

import akka.actor.Props
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.testkit.{ ImplicitSender, TestActors, TestKit }

import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

import com.africasTalking._

import FoodOrderService._

class FoodOrderServiceSpec() extends TestKit(ActorSystem("FoodOrderServiceSpec")) with ImplicitSender
    with WordSpecLike 
    with Matchers 
    with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val order = FoodOrder(
    quantity    = 4,
    name        = "Ugali"
  )

  "The FoodOrderActor" must {
    "send the food order to the broker and get a response" in {
      val foodOrderActor 		  = system.actorOf(Props(new FoodOrderActor))
      foodOrderActor ! PlaceOrder(order)
      expectMsgClass(FiniteDuration(30, "seconds"), classOf[PlaceOrderStatus])

    }

  }
}
