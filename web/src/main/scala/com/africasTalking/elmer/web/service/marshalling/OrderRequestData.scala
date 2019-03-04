package com.africasTalking.elmer.web
package service.marshalling

import io.atlabs._

import horus.core.util.{ ATCCPrinter, ATUtil }

import com.africasTalking._

import elmer.core.util.ElmerEnum.FoodName

import elmer.order.request.OrderRequestService._

private[service] case class FoodOrderRequest(
  username: String,
  foodName: String,
  quantity: Int,
  callbackUrl: Option[String]
) extends ATCCPrinter {
  require(quantity > 0, "Quantity must be greater than zero")
  require(FoodName.fromString(foodName) != None, "Invalid foodName provided")
  callbackUrl match {
    case Some(x) => require(ATUtil.parseUrl(x) != None, "Invalid callback url provided")
    case None    =>
  }
  def getServiceRequest(userId: Int) = FoodOrderServiceRequest(
    userId      = userId,
    name        = FoodName.withName(foodName),
    quantity    = quantity,
    callbackUrl = callbackUrl match {
      case None    => None
      case Some(x) => ATUtil.parseUrl(x)
    }
  )
}

private[service] case class FoodOrderResponse(
  transactionId: String,
  status: String,
  description: String
) extends ATCCPrinter

object FoodOrderResponse {
  def fromServiceResponse(response: FoodOrderServiceResponse) = FoodOrderResponse(
    transactionId = response.transactionId,
    status        = response.status.toString,
    description   = response.description
  )
}
