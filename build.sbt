organization in ThisBuild := "com.ericsson.jenkinsci.hajp"

scalaVersion in ThisBuild := "2.11.5"

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/ericssonITTEcicontrib/hajp-orchestrator</url>
  <licenses>
      <license>
        <name>MIT License</name>
        <url>http://www.opensource.org/licenses/mit-license.php</url>
      </license>
  </licenses>
   <developers>
      <developer>
        <name>Daniel Yinanc</name>
        <organization>Ericsson</organization>
        <organizationUrl>http://www.ericsson.com</organizationUrl>
      </developer>
      <developer>
        <name>Scott Hebert</name>
        <organization>Ericsson</organization>
        <organizationUrl>http://www.ericsson.com</organizationUrl>
      </developer>
    </developers>
    <scm>
      <connection>scm:git:git@github.com:ericssonITTEcicontrib/hajp-orchestrator.git</connection>
      <developerConnection>scm:git:git@github.com:ericssonITTEcicontrib/hajp-orchestrator.git</developerConnection>
      <url>git@github.com:ericssonITTEcicontrib/hajp-orchestrator.git</url>
    </scm>)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

//sbt release plugin default settings
releaseSettings


lazy val orchestrator = (project in file("."))
  .settings(
    name := "hajp-orchestrator",
    libraryDependencies ++= Dependencies.orchestrator,
    javaOptions in run ++= Seq(
      "-Djava.library.path=./sigar",
      "-Xms128m", "-Xmx1024m"),
    // this enables custom javaOptions
    fork in run := true
  )

//
// Scala Compiler Options
// If this project is only a subproject, add these to a common project setting.
//
scalacOptions in ThisBuild ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code"
)

/**
 * Black magic to tell sbt to choose the first duplicate class when resolving dependency collisions instead of just
 * failing out. Maven does this.
 */
mergeStrategy in assembly <<= (mergeStrategy in assembly) {
  (old) => {
    case x if Assembly.isConfigFile(x) =>
      MergeStrategy.concat
    case PathList(ps@_*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
      MergeStrategy.rename
    case PathList("META-INF", xs@_*) =>
      (xs map { _.toLowerCase}) match {
        case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
          MergeStrategy.discard
        case ps@(x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
          MergeStrategy.discard
        case "plexus" :: xs =>
          MergeStrategy.discard
        case "services" :: xs =>
          MergeStrategy.filterDistinctLines
        case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
          MergeStrategy.filterDistinctLines
        case _ => MergeStrategy.deduplicate
      }
    case _ => MergeStrategy.first // overrides the default fallback MergeStrategy of deduplicate
  }
}



artifact in(Compile, assembly) := {
  val art = (artifact in(Compile, assembly)).value
  art.copy(`classifier` = Some("assembly"))
}

addArtifact(artifact in(Compile, assembly), assembly)



