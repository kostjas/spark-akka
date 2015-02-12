//
// http://spark.apache.org/docs/latest/quick-start.html#a-standalone-app-in-scala
//
name         := "spark-akka"
organization := "ks"
version      := "1.0"
scalaVersion := Version.scala

libraryDependencies ++= Dependencies.sparkAkkaHadoop

releaseSettings