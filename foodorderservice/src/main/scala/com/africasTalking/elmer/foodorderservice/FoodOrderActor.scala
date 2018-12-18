package com.africasTalking.elmer.foodorderservice

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Success, Failure }

import akka.actor.{ Actor, ActorSystem, ActorLogging, Props }
import akka.pattern.ask
import akka.util.Timeout
import akka.stream.ActorMaterializer
import akka.event.Logging
import akka.util.ByteString

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal


import spray.json._

import com.africasTalking._
import elmer.core.config.ElmerConfig
import elmer.core.util._

case class FoodOrder(order: String, quantity: Int)

object FoodOrderActor{
    case class PlaceOrder(order: FoodOrder)
    case class PlaceOrderStatus(status: String)
}

class FoodOrderActor extends Actor
        with ActorLogging   
        with JsonProtocolT{
            import FoodOrderActor._
            import context.dispatcher

            implicit val system = context.system

            implicit val mat = ActorMaterializer()

            override lazy val log = Logging(system, classOf[FoodOrderActor])
            
            implicit val timeout      = Timeout(5.seconds)

            val url = ElmerConfig.brokerUrl

            def receive: Receive = {

                case PlaceOrder(order) =>
                    log.info(s"Processing + ${PlaceOrder}")
                    val currentSender = sender
                    val requestBody = order.toJson.compactPrint
                    val request = HttpRequest(
                            method = HttpMethods.POST,
                            uri    = Uri(url),
                            entity = HttpEntity(ContentTypes.`application/json`, ByteString(requestBody))
                        )

                    val httpResponse = Http().singleRequest(request)

                      httpResponse.onComplete{
                            case Success(value) => 
                              val response = Unmarshal(value.entity).to[PlaceOrderStatus]
                              response onComplete {
                                  case Success(response) => 
                                    response
                                case Failure(response) => 
                                    println("Response not found!")
                            } 
                            case Failure(error) => error.printStackTrace()
            }
        }             
    }
