package com.africasTalking.elmer.web
package service

import scala.concurrent.duration._

import akka.http.scaladsl.model._
import StatusCodes._
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import akka.http.scaladsl.server._

import org.scalatest.{ Matchers, WordSpec }

class ElmerWebServiceSpec extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with ElmerWebServiceT {

  override def actorRefFactory  = system

  implicit val routeTestTimeout = RouteTestTimeout(FiniteDuration(10, "seconds"))

  "The ElmerWebService" should {
    "Reject a food order POST request that is missing a parameter" in {
      HttpRequest(
        method = HttpMethods.POST,
        uri    = "/order/request",
        entity = HttpEntity(
          MediaTypes.`application/json`,
          """{"name":"Ugali"}"""
        )
      ) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "The request content was malformed:\nObject is missing required member 'quantity'"
      }
    }
    "Reject a food order POST request that has an invalid food name" in {
      HttpRequest(
        method = HttpMethods.POST,
        uri    = "/order/request",
        entity = HttpEntity(
          MediaTypes.`application/json`,
          """{"name":"UgaliNyama","quantity":5}"""
        )
      ) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "The request content was malformed:\nObject is missing required member 'name'"
      }
    }
    "return a MethodNotAllowed error for PUT requests to the food ordering path" in {
      Put("/order/request") ~> Route.seal(route) ~> check {
        status shouldEqual MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: POST"
      }
    }
    "leave requests to base path unhandled" in {
      Get() ~> route ~> check {
        handled shouldEqual false
      }
    }
    "leave requests to other paths unhandled" in {
      Get("/other") ~> route ~> check {
        handled shouldEqual false
      }
    }
  }

}
