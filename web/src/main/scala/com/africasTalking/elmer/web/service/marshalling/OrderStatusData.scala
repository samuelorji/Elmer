package com.africasTalking.elmer.web
package service.marshalling

import io.atlabs._

import horus.core.util.ATCCPrinter

import com.africasTalking._

import elmer.core.util.ElmerEnum.FoodOrderStatus

import elmer.order.status.OrderStatusService._

private[service] case class EtherFoodOrderStatusRequest(
  transactionId: String,
  status: FoodOrderStatus.Value,
  description: String
) extends ATCCPrinter {

  def getServiceRequest = FoodOrderStatusServiceRequest(
    brokerTransactionId = transactionId,
    status              = status,
    description         = description
  )
}
