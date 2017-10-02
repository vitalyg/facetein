name := """facestein"""
organization := "com.salesforce"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"       % "3.1.0",
  "com.h2database"  %  "h2"                % "1.4.196",
  "ch.qos.logback"  %  "logback-classic"   % "1.2.3",
  "org.postgresql"  %  "postgresql"        % "42.1.4"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.salesforce.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.salesforce.binders._"
