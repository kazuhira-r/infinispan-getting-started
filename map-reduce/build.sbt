name := "map-reduce"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.5"

organization := "org.littlewings"

updateOptions := updateOptions.value.withCachedResolution(true)

scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature")

fork in Test := true

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-core" % "7.1.0.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)
