name := "remote-task"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.1"

organization := "org.littlewings"

scalacOptions ++= Seq("-Xlint", "-unchecked", "-deprecation", "-feature")

updateOptions := updateOptions.value.withCachedResolution(true)

assemblyJarName in assembly := "remote-task.jar"

assemblyMergeStrategy in assembly := {
  case "org/littlewings/infinispan/task/entity/Book.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

packageOptions in (Compile, packageBin) +=
    Package.ManifestAttributes("Dependencies" -> "org.littlewings.task.entity")

test in assembly := {}

fork in Test := true

libraryDependencies ++= Seq(
  "org.infinispan" % "infinispan-tasks-api" % "8.2.6.Final" % Provided,
  "net.jcip" % "jcip-annotations" % "1.0" % Provided,
  "org.infinispan" % "infinispan-client-hotrod" % "8.2.6.Final" % Test,
  "org.scalatest" %% "scalatest" % "3.0.1" % Test
)
