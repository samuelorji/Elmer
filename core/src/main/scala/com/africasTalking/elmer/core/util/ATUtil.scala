package com.africasTalking.elmer.core
package util

import scala.concurrent.duration.{ Duration, FiniteDuration, MILLISECONDS }

object ATUtil {
 
  def parseFiniteDuration(str: String) : Option[FiniteDuration] = {
    try {
      Some(Duration(str)).collect { case d: FiniteDuration => d }
    } catch {
      case ex: NumberFormatException => None
    }
  }
}
