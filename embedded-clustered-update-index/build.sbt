name := "embedded-clustered-update-index"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.7"

organization := "org.littleiwngs"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

fork in Test := true

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-query" % "7.2.3.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided",
  "org.hibernate" % "hibernate-search-engine" % "5.2.0.Final",
  "org.apache.lucene" % "lucene-analyzers-kuromoji" % "4.10.4",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test"
)
