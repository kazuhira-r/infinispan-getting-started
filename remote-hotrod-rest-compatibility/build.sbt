name := "remote-hotrod-rest-compatibility"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.6"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

fork in Test := true

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-client-hotrod" % "7.2.1.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided",
  "org.jboss.resteasy" % "resteasy-client" % "3.0.11.Final",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test"
)
