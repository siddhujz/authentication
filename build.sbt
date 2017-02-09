name := """authentication"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.8"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-q", "-a")

libraryDependencies += "dom4j" % "dom4j" % "1.6.1" intransitive()
libraryDependencies ++= Seq(
  javaJdbc,
  javaJpa,
  "org.mockito" % "mockito-core" % "2.6.8",
  javaWs % "test",
  "org.hibernate" % "hibernate-core" % "5.2.6.Final",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.typesafe.play" %% "play-mailer" % "5.0.0",
  cache,
  javaWs
)

fork in run := true
