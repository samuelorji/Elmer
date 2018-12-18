package com.africasTalking.elmer.webservice

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.language.postfixOps
import scala.util.{ Failure, Success }

import akka.actor.ActorRefFactory
import akka.actor.Props
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout

import akka.event.Logging

import com.africasTalking._

import elmer.core.config.ElmerConfig

import elmer.core.util._

import elmer.foodorderservice._

import FoodOrderActor._

trait WebServiceT extends JsonProtocolT {
     implicit def actorRefFactory: ActorRefFactory

    private val foodorderActor              = actorRefFactory.actorOf(Props[FoodOrderActor])

   implicit val timeout                     = Timeout(5.seconds)

    lazy val route = {
        path("order") {
            post {
            entity(as[FoodOrder]) { order =>
                    complete((foodorderActor ? PlaceOrder(order)
                ).mapTo[PlaceOrderStatus])
                }
            }
        }
    }
} 

