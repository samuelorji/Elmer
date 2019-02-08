package com.africasTalking.elmer.order
package status

import java.net.URL

import io.atlabs._

import horus.core.util.ATCCPrinter

import com.africasTalking._

import elmer.core.util.ElmerEnum.FoodOrderStatus

private[order] object OrderStatusMessage {

  case class FoodOrderPendingStatusElement(
    transactionId: String,
    callbackUrl: Option[URL]
  ) extends ATCCPrinter

}
