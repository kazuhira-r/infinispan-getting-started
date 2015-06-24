name := "embedded-remote-compatibility-mode"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.7"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

fork in Test := true

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-core" % "7.2.3.Final",
  "org.infinispan" % "infinispan-client-hotrod" % "7.2.3.Final",
  "org.infinispan" % "infinispan-server-hotrod" % "7.2.3.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test"
)
