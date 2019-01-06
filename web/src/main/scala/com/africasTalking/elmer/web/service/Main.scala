package com.africasTalking.elmer.web
package service

import akka.actor.{ ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import io.atlabs._

import horus.core.util.{ ATLogT, ApplicationLifecycle }

import com.africasTalking._

import elmer.core.config.ElmerConfig

class Application extends ApplicationLifecycle with ATLogT {

  private[this] var started: Boolean = false

  private val applicationName = "ether-web"
  implicit val system         = ActorSystem(s"$applicationName-system")

  def start() {
    log.info(s"Starting $applicationName Service")

    if (!started) {

      implicit val materializer = ActorMaterializer()
      Http().bindAndHandle(
        new ElmerWebServiceT {
          override def actorRefFactory = system
        }.route,
        ElmerConfig.webHost,
        ElmerConfig.webPort
      )
      
      started = true
    }
    
  }

  def stop() {
    log.info(s"Stopping $applicationName Service")

    if (started) {
      started = false
      system.terminate()
    }
  }

}
