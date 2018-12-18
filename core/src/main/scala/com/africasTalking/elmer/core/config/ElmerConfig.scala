package com.africasTalking.elmer.core
package config

import collection.JavaConversions._
import com.typesafe.config.ConfigFactory

import com.africasTalking._

import elmer.core.util.ATUtil

object ElmerConfig extends ElmerConfig

private[config] trait ElmerConfig {

  val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  
 //Timeout
  val brokerTimeout = ATUtil.parseFiniteDuration(config.getString("elmer.actor-timeout.broker")).get
  

  // Broker
  val brokerUrl   = config.getString("elmer.broker.url")
  
  //http
  val httpRequestTimeout = ATUtil.parseFiniteDuration(config.getString("elmer.http.request-timeout")).get

  // API
  val apiInterface = config.getString("elmer.interface.web.host")
  val apiPort      = config.getInt("elmer.interface.web.port")
}