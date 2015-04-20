name := "embedded-lucene-distributed-directory"

version := "0.0.1-SNAPSHOT"

organization := "org.littlewings"

scalaVersion := "2.11.6"

scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

/*
// sbt上で繰り返しrunする場合は、これらをtrueにする
fork in run := true

connectInput := true
*/

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-lucene-directory" % "7.1.1.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided",
  "org.apache.lucene" % "lucene-queryparser" % "4.10.3",
  "org.apache.lucene" % "lucene-analyzers-kuromoji" % "4.10.3"
)
