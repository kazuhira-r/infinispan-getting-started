name := "embedded-query-hibernate-search-elasticsearch"

organization := "org.littlewings"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.3"

updateOptions := updateOptions.value.withCachedResolution(true)

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-value-discard"
)

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-query" % "9.1.1.Final" % Compile,
  "org.hibernate" % "hibernate-search-elasticsearch" % "5.8.0.Final" % Compile,
  "net.jcip" % "jcip-annotations" % "1.0" % Provided,
  "org.slf4j" % "slf4j-api" % "1.7.25" % Test,
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Test,
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)
