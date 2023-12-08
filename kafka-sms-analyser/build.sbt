ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "kafka-sms-analyser",
    idePackagePrefix := Some("io.github.malyszaryczlowiek"),
    assembly / assemblyJarName := s"${name.value}-${version.value}.jar",


    libraryDependencies ++= Seq(

      // Kafka Repos
      "org.apache.kafka" %% "kafka" % "3.1.0",
      "org.apache.kafka" %% "kafka-streams-scala" % "3.1.0",
      "org.apache.kafka"  % "kafka-clients"       % "3.1.0",


      // Own library
      // https://github.com/malyszaryczlowiek/kessenger-lib
      "io.github.malyszaryczlowiek" %% "kessenger-lib" % "0.3.26",


      // to parsing application.conf
      // https://github.com/lightbend/config
      "com.typesafe" % "config" % "1.4.2",


      // parsing json to object and back
      "com.lihaoyi" %% "upickle" % "3.1.3",

      // web client
      "com.softwaremill.sttp.client3" %% "core" % "3.9.1",


      // logging
      "com.typesafe.play" %% "play-logback" % "2.8.19",


      // slf4j
      //      "org.slf4j" % "slf4j-nop" % "2.0.5",
      //
      //      // log4j logger
      //      "org.apache.logging.log4j" % "log4j-api" % "2.20.0",
      //      "org.apache.logging.log4j" % "log4j-core" % "2.20.0",


      // for tests
      "org.scalactic" %% "scalactic" % "3.2.17",
      "org.scalatest" %% "scalatest" % "3.2.17" % "test",



      // For Tests with streams
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test,


     // for kafka stream tests
      "org.apache.kafka" % "kafka-streams-test-utils" % "3.1.0" % Test,

    )
  )

// for build JAR executable.
assembly / mainClass := Some("io.github.malyszaryczlowiek.SmsAnalyser")
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

Compile / run := Defaults.runTask(Compile / fullClasspath, Compile / run / mainClass, Compile / run / runner).evaluated