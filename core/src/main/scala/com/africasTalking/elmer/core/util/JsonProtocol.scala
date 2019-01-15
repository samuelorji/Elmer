package com.africasTalking.elmer.core
package util

import spray.json._

import io.atlabs._

import horus.core.util.ATUtil

import ElmerEnum._

object ElmerJsonProtocol extends DefaultJsonProtocol {
  implicit val FoodNameFormat: RootJsonFormat[FoodName.Value]               = ATUtil.enumJsonFormat(FoodName)
  implicit val FoodOrderStatusFormat: RootJsonFormat[FoodOrderStatus.Value] = ATUtil.enumJsonFormat(FoodOrderStatus)
}
