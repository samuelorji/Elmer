package com.africasTalking.elmer.core
package util

import spray.json._

import ElmerEnum._

object ElmerJsonProtocol extends DefaultJsonProtocol {
  implicit def enumFormat[T <: Enumeration](implicit enu: T): RootJsonFormat[T#Value] =
    new RootJsonFormat[T#Value] {
      def write(obj: T#Value): JsValue = JsString(obj.toString)
      def read(json: JsValue): T#Value = {
        json match {
          case JsString(txt) => enu.withName(txt)
          case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
        }
      }
    }

  implicit val FoodNameFormat: RootJsonFormat[FoodName.Value]               = enumFormat(FoodName)
  implicit val FoodOrderStatusFormat: RootJsonFormat[FoodOrderStatus.Value] = enumFormat(FoodOrderStatus)

}
