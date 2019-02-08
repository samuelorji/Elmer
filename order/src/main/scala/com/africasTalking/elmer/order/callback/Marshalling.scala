package com.africasTalking.elmer.order
package callback

import akka.http.scaladsl.marshallers.sprayjson._

import spray.json._

import io.atlabs._

import horus.core.util.ATCCPrinter

import com.africasTalking._

import elmer.core.util.{ ElmerEnum, ElmerJsonProtocol }
import ElmerEnum.FoodOrderStatus

private[callback] object ClientCallbackServiceMarshalling {

  case class ClientFoodOrderStatusRequest(
    transactionId: String,
    status: FoodOrderStatus.Value,
    description: String
  ) extends ATCCPrinter

  trait ClientCallbackServiceJsonSupportT extends SprayJsonSupport
      with DefaultJsonProtocol {
    import ElmerJsonProtocol._

    implicit val ClientFoodOrderStatusRequestFormat = jsonFormat3(ClientFoodOrderStatusRequest)

  }

}
