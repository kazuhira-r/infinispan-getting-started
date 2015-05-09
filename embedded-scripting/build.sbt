name := "embedded-scripting"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.6"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

fork in Test := true

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-scripting" % "7.2.1.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.codehaus.groovy" % "groovy-all" % "2.4.3" % "test"
)
