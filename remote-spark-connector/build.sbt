name := "remote-spark-connector"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.4"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.infinispan" %% "infinispan-spark" % "0.2",
  "org.apache.spark" %% "spark-core" % "1.5.2",
  "org.apache.spark" %% "spark-streaming" % "1.5.2",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)
