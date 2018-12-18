lazy val sharedSettings = Seq(
  organization := "com.africasTalking   ",
  version      := "0.1.0",
  scalaVersion := "2.12.6",
  resolvers    ++= Seq(
    "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Confluent Maven Repository" at "http://packages.confluent.io/maven/"
  ),
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked"
  )
)

val akkaVersion      = "2.5.16"
val akkaHttpVersion  = "10.1.5"
val scalaTestVersion = "3.0.5"


lazy val elmer = (project in file("."))
  .aggregate(core, foodorderservice, webservice)

lazy val core = (project in file("core")).
  settings(
    sharedSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.akka"             %% "akka-actor"           % akkaVersion,
      "com.typesafe.akka"             %% "akka-stream"          % akkaVersion,
      "com.typesafe.akka"             %% "akka-http-spray-json" % akkaHttpVersion,

       "io.spray"                     %%  "spray-json"           % "1.3.5",

      "com.github.nscala-time"        %% "nscala-time"          % "2.20.0",
      
      "commons-daemon"                %  "commons-daemon"       % "1.1.0",

      "org.scalatest"                 %% "scalatest"            % scalaTestVersion % Test,
      "com.typesafe.akka"             %% "akka-testkit"         % akkaVersion      % Test,
      "org.lz4"                       %  "lz4-java"             % "1.4.1"          % Test,
    )
  )

lazy val foodorderservice = (project in file("foodorderservice")).
  settings(
    sharedSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion      % Test,
      "org.scalatest"     %% "scalatest"    % scalaTestVersion % Test
    )
  ).dependsOn(core)

lazy val webservice = (project in file("webservice")).
  settings(
    sharedSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-testkit"      % akkaVersion      % Test,
      "org.scalatest"     %% "scalatest"         % scalaTestVersion % Test,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion  % Test
    )
  ).dependsOn(core, foodorderservice)

  cancelable in Global := true
