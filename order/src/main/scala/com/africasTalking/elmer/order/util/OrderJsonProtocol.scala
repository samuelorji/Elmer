package com.africasTalking.elmer.order
package util

import spray.json._

import io.atlabs._

import horus.core.util.ATJsonProtocol

import com.africasTalking._

import elmer.order.status.OrderStatusMessage._

private[order] object OrderJsonProtocol extends DefaultJsonProtocol {

  import ATJsonProtocol._
  implicit val FoodOrderPendingStatusElementFormat = jsonFormat2(FoodOrderPendingStatusElement)

}
