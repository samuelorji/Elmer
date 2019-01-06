package com.africasTalking.elmer.core
package config

import io.atlabs._

import horus.core.config.ATBaseConfigT

object ElmerConfig extends ATBaseConfigT {

  // Web Interface
  val webHost = config.getString("elmer.interface.web.host")
  val webPort = config.getInt("elmer.interface.web.port")

  // Broker Order Url
  val etherOrderRequestUrl = config.getString("elmer.broker.ether.order-request-url")

}
