package com.africasTalking.elmer.web
package service

import scala.concurrent.duration._

import akka.http.scaladsl.model._
import headers.RawHeader
import StatusCodes._
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import akka.http.scaladsl.server._

import org.scalatest.{ Matchers, WordSpec }

import spray.json._

import marshalling._

class ElmerWebServiceSpec extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with ElmerWebServiceT {

  override def actorRefFactory  = system

  implicit val routeTestTimeout = RouteTestTimeout(FiniteDuration(30, "seconds"))

  "The ElmerWebService" should {
    "Reject a food order POST request that is missing the username" in {
      HttpRequest(
        method = HttpMethods.POST,
        uri    = "/order/request",
        entity = HttpEntity(
          MediaTypes.`application/json`,
          """{"name":"Ugali","quantity":5}"""
        )
      ) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "The request content was malformed:\nObject is missing required member 'username'"
      }
    }
    "Reject a food order POST request that is missing the quantity parameter" in {
      HttpRequest(
        method = HttpMethods.POST,
        uri    = "/order/request",
        entity = HttpEntity(
          MediaTypes.`application/json`,
          """{"username":"testuser1","foodName":"Ugali"}"""
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
          """{"username":"testuser1","foodName":"UgaliNyama","quantity":5}"""
        )
      ) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "requirement failed: Invalid foodName provided"
      }
    }
    "Reject a food order POST request that has an invalid quantity" in {
      HttpRequest(
        method = HttpMethods.POST,
        uri    = "/order/request",
        entity = HttpEntity(
          MediaTypes.`application/json`,
          """{"username":"testuser1","foodName":"Ugali","quantity":0}"""
        )
      ) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "requirement failed: Quantity must be greater than zero"
      }
    }
    "Reject a food order POST request that has an invalid callbackUrl" in {
      HttpRequest(
        method = HttpMethods.POST,
        uri    = "/order/request",
        entity = HttpEntity(
          MediaTypes.`application/json`,
          """{"username":"testuser1","foodName":"Ugali","quantity":1,"callbackUrl":"InvalidUrl"}"""
        )
      ) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "requirement failed: Invalid callback url provided"
      }
    }
 
    "Reject a food order POST request that does not have an apikey" in {
      HttpRequest(
        method = HttpMethods.POST,
        uri    = "/order/request",
        entity = HttpEntity(
          MediaTypes.`application/json`,
          """{"username":"testuser1","foodName":"Ugali","quantity":2}"""
        )
      ) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.Unauthorized
        responseAs[String] shouldEqual "The resource requires authentication, which was not supplied with the request"
      }
    }
    "Reject a food order POST request that provides the wrong authentication" in {
      HttpRequest(
        method = HttpMethods.POST,
        uri    = "/order/request",
        entity = HttpEntity(
          MediaTypes.`application/json`,
          """{"username":"testuser1","foodName":"Ugali","quantity":2}"""
        )
      ).withHeaders(
        RawHeader("apikey", "invalid")
      ) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.Unauthorized
        responseAs[String] shouldEqual "The supplied authentication is invalid"
      }
    }
    "Process a valid food order POST request" in {
      HttpRequest(
        method = HttpMethods.POST,
        uri    = "/order/request",
        entity = HttpEntity(
          MediaTypes.`application/json`,
          """{"username":"testuser1","foodName":"Ugali","quantity":2,"callbackUrl":"http://www,test.com.com/callback"}"""
        )
      ).withHeaders(
        RawHeader("apikey", "testpass1")
      ) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.Created
        val response = responseAs[String].parseJson.convertTo[FoodOrderResponse]
        response shouldEqual FoodOrderResponse(
          transactionId = response.transactionId,
          description   = "Request accepted for processing",
          status        = "Accepted"
        )
      }
    }
    "return a MethodNotAllowed error for PUT requests to the food ordering path" in {
      Put("/order/request") ~> Route.seal(route) ~> check {
        status shouldEqual MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: POST"
      }
    }
    "Process a valid food order status POST request" in {
      HttpRequest(
        method = HttpMethods.POST,
        uri    = "/order/status",
        entity = HttpEntity(
          MediaTypes.`application/json`,
          """{"transactionId":"someTxnId","status":"Delivered","description":"The food has been delivered"}"""
        )
      ) ~> Route.seal(route) ~> check {
        status shouldEqual StatusCodes.OK
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
