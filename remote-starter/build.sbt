name := "remote-starter"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.4"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

incOptions := incOptions.value.withNameHashing(true)

fork in Test := true

parallelExecution in Test := false

libraryDependencies ++= Seq(
  // for Hot Rod
  "org.infinispan" % "infinispan-client-hotrod" % "7.0.0.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided",
  // for Memcached
  "net.spy" % "spymemcached" % "2.11.4",
  // for REST
  "net.databinder.dispatch" %% "dispatch-json4s-jackson" % "0.11.2",
  "org.scalatest" %% "scalatest" % "2.2.2" % "test"
)
