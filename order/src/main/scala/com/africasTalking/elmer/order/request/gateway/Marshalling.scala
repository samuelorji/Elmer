package com.africasTalking.elmer.order
package request.gateway

import akka.http.scaladsl.marshallers.sprayjson._

import spray.json._

import io.atlabs._

import horus.core.util.ATCCPrinter

import com.africasTalking._

import elmer.core.util.{ ElmerEnum, ElmerJsonProtocol }
import ElmerEnum.{ FoodName, FoodOrderStatus }

private[gateway] object OrderRequestGatewayMarshalling {

  case class EtherFoodOrderRequest(
    name: FoodName.Value,
    quantity: Int
  ) extends ATCCPrinter
  
  case class EtherFoodOrderResponse(
    status: FoodOrderStatus.Value,
    description: String
  ) extends ATCCPrinter

  trait OrderRequestGatewayJsonSupportT extends SprayJsonSupport
      with DefaultJsonProtocol {
    import ElmerJsonProtocol._
    
    implicit val RequestFormat  = jsonFormat2(EtherFoodOrderRequest)
    implicit val ResponseFormat = jsonFormat2(EtherFoodOrderResponse)
  }

}
