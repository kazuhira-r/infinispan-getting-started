name := "embedded-clustering-starter"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.4"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

incOptions := incOptions.value.withNameHashing(true)

updateOptions := updateOptions.value.withCachedResolution(true)

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-core" % "7.0.2.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided"
)
