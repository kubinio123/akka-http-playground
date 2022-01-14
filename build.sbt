name := "akka-http-playground"

version := "0.1"

scalaVersion := "2.13.8"

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.7"
val Slf4jVersion = "2.0.0-alpha5"
val ScalaTest = "3.2.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion,

  "org.slf4j" % "slf4j-api" % Slf4jVersion,
  "org.slf4j" % "slf4j-simple" % Slf4jVersion,

  "org.scalactic" %% "scalactic" % ScalaTest,
  "org.scalatest" %% "scalatest" % ScalaTest % "test"
)
