name := "embedded-locked-streams"

organization := "org.littlewings"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.3"

updateOptions := updateOptions.value.withCachedResolution(true)

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-core" % "9.1.0.Final" % Compile,
  "net.jcip" % "jcip-annotations" % "1.0" % Provided,
  "org.scalatest" %% "scalatest" % "3.0.3" % Test
)
