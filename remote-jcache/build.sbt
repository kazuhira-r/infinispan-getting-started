name := "remote-jcache"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.6"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "javax.cache" % "cache-api" % "1.0.0",
  "org.infinispan" % "infinispan-jcache-remote" % "7.2.1.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)
