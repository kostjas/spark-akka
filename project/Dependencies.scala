import sbt._

object Version {
  val akka      = "2.3.9"
  val hadoop    = "2.6.0"
  val logback   = "1.1.2"
  val mockito   = "1.10.19"
  val scala     = "2.11.5"
  val scalaTest = "2.2.4"
  val slf4j     = "1.7.6"
  val spark     = "1.2.0"
}

object Library {
  val akkaActor      = "com.typesafe.akka" %% "akka-actor"      % Version.akka
  val akkaTestKit    = "com.typesafe.akka" %% "akka-testkit"    % Version.akka
  val hadoopClient   = "org.apache.hadoop" %  "hadoop-client"   % Version.hadoop
  val logbackClassic = "ch.qos.logback"    %  "logback-classic" % Version.logback
  val mockitoAll     = "org.mockito"       %  "mockito-all"     % Version.mockito
  val scalaTest      = "org.scalatest"     %% "scalatest"       % Version.scalaTest
  val slf4jApi       = "org.slf4j"         %  "slf4j-api"       % Version.slf4j
  val sparkStreaming = "org.apache.spark"  %% "spark-streaming" % Version.spark
}

object Dependencies {

  import Library._

  val sparkAkkaHadoop = Seq(
    sparkStreaming,
    akkaActor,
    akkaTestKit,
    hadoopClient,
    "org.scalaz"   %% "scalaz-core" % "7.0.6",
    "mysql" % "mysql-connector-java" % "5.1.34",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    logbackClassic % "test",
    scalaTest      % "test",
    mockitoAll     % "test",
    "org.specs2"   %% "specs2" % "2.3.12" % "test"
  )
}
