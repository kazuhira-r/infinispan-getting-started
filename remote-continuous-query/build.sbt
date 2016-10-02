name := "remote-continuous-query"

organization := "org.littlewings"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

updateOptions := updateOptions.value.withCachedResolution(true)

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-client-hotrod" % "8.2.4.Final" % Compile,
  "org.infinispan" % "infinispan-query-dsl" % "8.2.4.Final" % Compile,
  "org.infinispan" % "infinispan-remote-query-client" % "8.2.4.Final" % Compile,
  "org.infinispan" % "infinispan-server-hotrod" % "8.2.4.Final" % Test,
  "org.infinispan" % "infinispan-remote-query-server" % "8.2.4.Final" % Test,
  "net.jcip" % "jcip-annotations" % "1.0" % Provided,
  "org.scalatest" %% "scalatest" % "3.0.0" % Test
)
