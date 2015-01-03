name := "remote-query"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.4"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

fork in Test := true

val infinispanVersion = "7.0.2.Final"
libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-client-hotrod" % infinispanVersion,
  "org.infinispan" % "infinispan-remote-query-client" % infinispanVersion,
  "org.infinispan" % "infinispan-query-dsl" % infinispanVersion,
  "net.jcip" % "jcip-annotations" % "1.0" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.3" % "test"
)
