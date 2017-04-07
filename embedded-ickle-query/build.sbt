name := "embedded-ickle-query"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.1"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

fork in Test := true

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-query" % "9.0.0.Final" % Compile,
  "net.jcip" % "jcip-annotations" % "1.0" % Provided,
  "org.apache.lucene" % "lucene-analyzers-kuromoji" % "5.5.4" % Compile,
  "org.scalatest" %% "scalatest" % "3.0.1" % Test
)
