name := "embedded-listeners"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.7"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-core" % "8.1.1.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided"
)
