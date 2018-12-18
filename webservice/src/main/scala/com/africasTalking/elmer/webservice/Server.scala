package com.africasTalking.elmer.webservice

import com.africasTalking._

import elmer.core.util.{ AbstractApplicationDaemon, ATApplicationT }

class ApplicationDaemon extends AbstractApplicationDaemon {
  def application = new Application
}

object ServiceApplication extends App with ATApplicationT[ApplicationDaemon] {
  def createApplication = new ApplicationDaemon
}
