addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")
addSbtPlugin("org.ensime" % "sbt-ensime" % "2.5.1")
credentials += Credentials(
  System.getenv("AT_RESOLVER_REALM"),
  System.getenv("AT_RESOLVER_HOST"),
  System.getenv("AT_RESOLVER_USER"),
  System.getenv("AT_RESOLVER_PASS")
)
