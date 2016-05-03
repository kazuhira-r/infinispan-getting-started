name := "remote-servers-embedded"

val projectScalaVersion = "2.11.8"

scalaVersion := projectScalaVersion

parallelExecution in Test := false

lazy val commonSettings = Seq(
  version := "0.0.1-SNAPSHOT",
  organization := "org.littlewings",
  scalaVersion := projectScalaVersion,
  scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature", "-Xexperimental"),
  updateOptions := updateOptions.value.withCachedResolution(true),
  parallelExecution in Test := false,
  fork in Test := true,
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  )
)

lazy val root = (project in file("."))
  .aggregate(entity, hotrod, memcached, rest)

lazy val entity = (project in file("entity"))
  .settings(commonSettings: _*)

lazy val hotrod = (project in file("hotrod"))
  .dependsOn(entity)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.infinispan" % "infinispan-server-hotrod" % "8.2.1.Final",
      "net.jcip" % "jcip-annotations" % "1.0" % "provided",
      "org.infinispan" % "infinispan-client-hotrod" % "8.2.1.Final" % "test",
      "org.infinispan" % "infinispan-query-dsl" % "8.2.1.Final" % "provided"
    )
  )

lazy val memcached = (project in file("memcached"))
  .dependsOn(entity)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.infinispan" % "infinispan-server-memcached" % "8.2.1.Final",
      "net.jcip" % "jcip-annotations" % "1.0" % "provided",
      "net.spy" % "spymemcached" % "2.12.1" % "test"
    )
  )

lazy val rest = (project in file("rest"))
  .dependsOn(entity)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.infinispan" % "infinispan-server-rest" % "8.2.1.Final" classifier "classes" excludeAll (ExclusionRule(organization = "org.apache.logging.log4j")),
      "net.jcip" % "jcip-annotations" % "1.0" % "provided",
      "org.jboss.resteasy" % "resteasy-client" % "3.0.11.Final" % "test",
      "org.jboss.resteasy" % "resteasy-jackson2-provider" % "3.0.11.Final" % "test"
    )
  )
