package com.africasTalking.elmer.web

import scala.concurrent.duration._

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import akka.util.ByteString

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import org.json4s.jackson.Serialization.write

import com.africasTalking._

import elmer.core.util._

import elmer.productservice._

import FoodOrderService._

class WebServiceSpec extends WordSpec 
  	with Matchers 
  	with ScalaFutures 
	with ScalatestRouteTest
    with WebServiceT 
    with ElmerJsonProtocol{

  def actorRefFactory  = system

  lazy val routes = route

  implicit val routeTestTimeout = RouteTestTimeout(FiniteDuration(20, "seconds"))

  "WebService" should {
    "be able to add food orders (POST /food/order)" in {
      val order = FoodOrder(
		    quantity = 4,
		    name     = "Ugali"
		)
      val orderJson   = order.toJson

      val orderEntity = HttpEntity(ContentTypes.`application/json`, ByteString(orderJson))

      val request     = Post("/order").withEntity(orderEntity)
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
      }
    }
  }

} 