package com.africasTalking.elmer.foodorderservice

import akka.http._

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import FoodOrderActor._


trait JsonProtocolT extends SprayJsonSupport with DefaultJsonProtocol {
  import DefaultJsonProtocol._

  implicit val orderJsonFormat = jsonFormat2(FoodOrder)
  implicit val responseJsonFormat = jsonFormat1(PlaceOrderStatus)  
}
