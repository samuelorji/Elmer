package com.africasTalking.elmer.core
package config

import org.scalatest.{ Matchers, WordSpecLike }

class ElmerConfigSpec extends WordSpecLike
    with Matchers {
  "The ElmerConfig" must {
    "parse values correctly" in {
      ElmerConfig.webHost should be ("127.0.0.1")
      ElmerConfig.webPort should be (8080)

      ElmerConfig.etherOrderRequestUrl should be ("https://ether.at-labs.at-internal.com/order/request")
    }
  }

}
