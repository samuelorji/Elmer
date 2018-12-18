package com.africasTalking.elmer.webservice

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.io.IO
import akka.stream.ActorMaterializer


import com.africasTalking._

import elmer.core.util.{ ApplicationLifecycle }

import elmer.core.config.ElmerConfig

class Application extends ApplicationLifecycle {

  private[this] var started: Boolean = false

  private val applicationName = "elmer"

  implicit val actorSystem    = ActorSystem(s"$applicationName-system")

  lazy val log = Logging(actorSystem, classOf[Application])

  def start() {
    log.info(s"Starting $applicationName Service")
 
    if (!started) {

      implicit val materializer = ActorMaterializer()
      Http().bindAndHandle(
        new WebServiceT {
          override def actorRefFactory = actorSystem
        }.route,
        ElmerConfig.apiInterface,
        ElmerConfig.apiPort
      )

      started = true
    }
  }

  def stop() {
    log.info(s"Stopping $applicationName Service")

    if (started) {
      started = false
      actorSystem.terminate()
    }
  }

}