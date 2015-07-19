name := "embedded-jcache-cdi"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.7"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

enablePlugins(JettyPlugin)

artifactName := { (version: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  //artifact.name + "." + artifact.extension
  "javaee7-web." + artifact.extension
}

webappWebInfClasses := true

libraryDependencies ++= Seq(
  "javax" % "javaee-web-api" % "7.0" % "provided",
  "javax.cache" % "cache-api" % "1.0.0",
  "org.infinispan" % "infinispan-jcache" % "7.2.3.Final",
  "net.jcip" % "jcip-annotations" % "1.0" % "provided"
)
