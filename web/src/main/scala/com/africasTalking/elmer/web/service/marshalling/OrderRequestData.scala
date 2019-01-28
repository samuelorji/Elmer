package com.africasTalking.elmer.web
package service.marshalling

import io.atlabs._

import horus.core.util.ATCCPrinter

import com.africasTalking._

import elmer.core.util.ElmerEnum.FoodName

import elmer.order.request.OrderRequestService._

private[service] case class FoodOrderRequest(
  username: String,
  foodName: String,
  quantity: Int
) extends ATCCPrinter {
  require(quantity > 0, "Quantity must be greater than zero")
  require(FoodName.fromString(foodName) != None, "Invalid foodName provided")
  def getServiceRequest = FoodOrderServiceRequest(
    name     = FoodName.withName(foodName),
    quantity = quantity
  )
}

private[service] case class FoodOrderResponse(
  status: String,
  description: String
) extends ATCCPrinter

object FoodOrderResponse {
  def fromServiceResponse(response: FoodOrderServiceResponse) = FoodOrderResponse(
    status      = response.status.toString,
    description = response.description
  )
}
