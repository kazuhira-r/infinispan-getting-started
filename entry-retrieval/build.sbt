name := "entry-retrieval"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.5"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

fork in Test := true

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-core" % "7.0.3.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.3" % "test"
)
