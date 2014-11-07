name := "embedded-local-starter"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.4"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

incOptions := incOptions.value.withNameHashing(true)

fork in Test := true

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-core" % "7.0.0.Final",
  "net.jcip" % "jcip-annotations" % "1.0",
  "org.scalatest" %% "scalatest" % "2.2.2"
)
