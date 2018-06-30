name := "remote-transaction"

organization := "org.littlewings"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.6"

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

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-client-hotrod" % "9.3.0.Final" % Compile,
  "net.jcip" % "jcip-annotations" % "1.0" % Provided,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
