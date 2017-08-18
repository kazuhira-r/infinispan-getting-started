name := "embedded-clustered-counter"

version := "0.0.1-SNAPSHOT"

organization := "org.littlewings"

scalaVersion := "2.12.3"

scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

fork in Test := true

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-clustered-counter" % "9.1.0.Final" % Compile,
  "net.jcip" % "jcip-annotations" % "1.0" % Provided,
  "org.scalatest" %% "scalatest" % "3.0.3" % Test
)
