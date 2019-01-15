package com.africasTalking.elmer.web
package service.marshalling

import akka.http.scaladsl.marshallers.sprayjson._

import spray.json._

import com.africasTalking._

import elmer.core.util.ElmerJsonProtocol

import elmer.order.request.OrderRequestService._

trait WebJsonSupportT extends DefaultJsonProtocol with SprayJsonSupport {

  import ElmerJsonProtocol._

  implicit val FoodOrderServiceRequestFormat  = jsonFormat2(FoodOrderServiceRequest)
  implicit val FoodOrderServiceResponseFormat = jsonFormat2(FoodOrderServiceResponse)
}
