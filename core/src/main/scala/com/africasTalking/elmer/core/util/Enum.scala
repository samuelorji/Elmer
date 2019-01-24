package com.africasTalking.elmer.core
package util

import scala.util.Try

object ElmerEnum {

  object FoodName extends Enumeration {
    val Ugali, Rice, BeefStew, BeefFry, Egusi, PepperSoup = Value
    def fromString(name: String): Option[Value] = Try(withName(name)).toOption
  }

  object FoodOrderStatus extends Enumeration {
    val Accepted, Delivered, Failed = Value
  }
}
