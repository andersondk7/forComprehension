import Dependencies._

name := "ForComphrension"

version      := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.4"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.dka",
      version      := "0.1.0-SNAPSHOT"
    )),
    libraryDependencies += scalaTest % Test
  )
