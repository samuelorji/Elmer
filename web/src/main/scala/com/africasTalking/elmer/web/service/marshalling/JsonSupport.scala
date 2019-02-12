package com.africasTalking.elmer.web
package service.marshalling

import akka.http.scaladsl.marshallers.sprayjson._

import spray.json._

import com.africasTalking._

import elmer.core.util.ElmerJsonProtocol

trait WebJsonSupportT extends DefaultJsonProtocol with SprayJsonSupport {

  import ElmerJsonProtocol._

  implicit val FoodOrderRequestFormat  = jsonFormat4(FoodOrderRequest)
  implicit val FoodOrderResponseFormat = jsonFormat3(FoodOrderResponse.apply)

  implicit val EtherFoodOrderStatusRequestFormat = jsonFormat3(EtherFoodOrderStatusRequest)

}
