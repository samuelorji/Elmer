lazy val sharedSettings = Seq(
  organization := "com.africasTalking",
  version      := "0.1.6",
  scalaVersion := "2.12.7",
  resolvers ++= Seq(
    "AT Snapshots" at "https://deino.at-internal.com/repository/maven-snapshots/",
    "AT Releases"  at "https://deino.at-internal.com/repository/maven-releases/",
    "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"
  ),
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked"
  ),
  test in assembly := {}
)

lazy val elmer = (project in file("."))
  .aggregate(core, order, web)

val atLibsVersion    = "0.1.12"
val akkaVersion      = "2.5.19"
val akkaHttpVersion  = "10.1.7"
val scalaTestVersion = "3.0.5"

lazy val core = (project in file("core")).
  settings(
    sharedSettings,
    libraryDependencies ++= Seq(
      "io.atlabs"         %% "horus-core"      % atLibsVersion,
      "com.typesafe.akka" %% "akka-actor"      % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j"      % akkaVersion,
      "ch.qos.logback"    %  "logback-classic" % "1.2.3",
      "commons-daemon"    %  "commons-daemon"  % "1.1.0",
      "org.scalatest"     %% "scalatest"       % scalaTestVersion   % Test
    )
  )

lazy val order = (project in file("order")).
  settings(
    sharedSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.akka"  %% "akka-testkit" % akkaVersion      % Test,
      "org.scalatest"      %% "scalatest"    % scalaTestVersion % Test
    )
  ).dependsOn(core)

lazy val web = (project in file("web")).
  settings(
    sharedSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-testkit"      % akkaVersion      % Test,
      "org.scalatest"     %%  "scalatest"        % scalaTestVersion % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion  % Test
    )
  ).dependsOn(core, order)
