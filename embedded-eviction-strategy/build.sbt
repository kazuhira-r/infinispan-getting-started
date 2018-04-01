name := "embedded-eviction-strategy"

organization := "org.littlewings"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.5"

updateOptions := updateOptions.value.withCachedResolution(true)

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-unused-import"
)

fork in Test := true

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-core" % "9.2.1.Final" % Compile,
  "net.jcip" % "jcip-annotations" % "1.0" % Compile,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
