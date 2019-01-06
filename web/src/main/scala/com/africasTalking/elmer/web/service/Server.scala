package com.africasTalking.elmer.web
package service

import io.atlabs._

import horus.core.util.{ AbstractApplicationDaemon, ATApplicationT }

class ApplicationDaemon extends AbstractApplicationDaemon {
  def application = new Application
}

object ServiceApplication extends App with ATApplicationT[ApplicationDaemon] {
  def createApplication = new ApplicationDaemon
}
