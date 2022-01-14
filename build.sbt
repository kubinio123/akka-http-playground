name := "akka-http-playground"

version := "0.1"

scalaVersion := "2.13.8"

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.7"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
)

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "2.0.0-alpha5",
  "org.slf4j" % "slf4j-simple" % "2.0.0-alpha5"
)
