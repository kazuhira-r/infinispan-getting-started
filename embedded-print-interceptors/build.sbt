name := "embedded-print-interceptors"

version := "0.0.1-SNAPSHOT"

organization := "org.littlewings"

scalaVersion := "2.11.8"

updateOptions := updateOptions.value.withCachedResolution(true)

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

javaOptions in Test += "-javaagent:" + System.getenv("BYTEMAN_HOME") + "/lib/byteman.jar=script:trace.btm"

fork in Test := true

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-core" % "8.2.2.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)
