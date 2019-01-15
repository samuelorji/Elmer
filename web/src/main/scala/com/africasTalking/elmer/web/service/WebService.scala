package com.africasTalking.elmer.web
package service

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.Props
import akka.event.Logging
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout

import io.atlabs._

import horus.core.config.ATConfig

import com.africasTalking._

import elmer.core.util.ElmerCoreServiceT

import elmer.order.request.OrderRequestService

import marshalling._

trait ElmerWebServiceT extends ElmerCoreServiceT with WebJsonSupportT {

  def snoopServiceName            = "elmer-web"

  implicit val timeout            = Timeout(ATConfig.httpRequestTimeout)

  private val orderRequestService = actorRefFactory.actorOf(Props[OrderRequestService])

  import OrderRequestService._
  lazy val route = {
    path("order" / "request") {
      logRequestResult("order:request", Logging.InfoLevel) {
        post {
          entity(as[FoodOrderServiceRequest]) { request =>
            complete {
              (orderRequestService ? request).mapTo[FoodOrderServiceResponse]
            }
          }
        }
      }
    }
  }
}
