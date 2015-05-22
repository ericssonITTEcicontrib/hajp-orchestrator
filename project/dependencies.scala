import sbt._

object Dependencies {

  object Version {
    val akka = "2.3.9"
  }

  lazy  val orchestrator = common ++ metrics ++ hajp ++ test

  val common = Seq(
    "com.typesafe.akka" %% "akka-actor" % Version.akka,
    "com.typesafe.akka" %% "akka-cluster" % Version.akka,
    "net.codingwell" %% "scala-guice" % "4.0.0-beta5"
  )

  val metrics = Seq(
    "org.fusesource" % "sigar" % "1.6.4"
  )

  val test = Seq(
    "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
      "com.typesafe.akka" % "akka-testkit_2.11" % "2.3.9" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test"
  )

  var hajp = Seq("com.ericsson.jenkinsci.hajp" % "hajp-common" % "1.0.10")

}
