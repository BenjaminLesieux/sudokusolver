ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "sudokusolver",
    idePackagePrefix := Some("com.efrei.team")
  )

libraryDependencies += "dev.zio" %% "zio" % "2.0.15"
libraryDependencies += "dev.zio" %% "zio-json" % "0.6.0"
libraryDependencies += "dev.zio" %% "zio-nio"  % "2.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.16" % "test"
