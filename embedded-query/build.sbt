name := "embedded-query"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.4"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

fork in Test := true

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-query" % "7.0.2.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided",
  "org.apache.lucene" % "lucene-analyzers-kuromoji" % "4.10.2",
  "org.scalatest" %% "scalatest" % "2.2.3" % "test"
)
