name := "remote-query-less-proto"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-client-hotrod" % "8.1.0.Final",
  "org.infinispan" % "infinispan-remote-query-client" % "8.1.0.Final",
  "org.infinispan" % "infinispan-query-dsl" % "8.1.0.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test"
)
