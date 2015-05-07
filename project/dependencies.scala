import sbt._

object Dependencies {

  object Version {
    val akka = "2.3.9"
  }

  lazy  val orchestrator = common ++ metrics ++ hajp

  val common = Seq(
    "com.typesafe.akka" %% "akka-actor" % Version.akka,
    "com.typesafe.akka" %% "akka-cluster" % Version.akka
  )

  val metrics = Seq(
    "org.fusesource" % "sigar" % "1.6.4"
  )

  var hajp = Seq("com.ericsson.jenkinsci.hajp" % "hajp-common" % "1.0.10")

}
