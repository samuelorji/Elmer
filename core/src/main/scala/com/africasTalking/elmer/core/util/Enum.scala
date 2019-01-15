package com.africasTalking.elmer.core
package util

object ElmerEnum {

  object FoodName extends Enumeration {
    val Ugali, Rice, BeefStew, BeefFry, Egusi, PepperSoup = Value
  }

  object FoodOrderStatus extends Enumeration {
    val Accepted, Delivered, Failed = Value
  }
}
