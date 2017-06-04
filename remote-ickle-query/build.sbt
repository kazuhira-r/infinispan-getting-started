name := "remote-ickle-query"

organization := "org.littlewings"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.2"

updateOptions := updateOptions.value.withCachedResolution(true)

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-client-hotrod" % "9.0.1.Final" % Compile,
  "org.infinispan" % "infinispan-remote-query-client" % "9.0.1.Final" % Compile,
  "org.infinispan" % "infinispan-query-dsl" % "9.0.1.Final" % Provided,
  "net.jcip" % "jcip-annotations" % "1.0" % Provided,
  "org.scalatest" %% "scalatest" % "3.0.3" % Test
)
