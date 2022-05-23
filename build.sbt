import play.sbt.routes.RoutesKeys

ThisBuild / organization := "de.unruh"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version      := "0.1.0-SNAPSHOT"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .aggregate(server, client, shared.jvm, shared.js)

lazy val server = project
  .settings(
    scalaJSProjects := Seq(client),
    Assets / pipelineStages  := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
    libraryDependencies += guice,
    libraryDependencies += "com.vmunier" %% "scalajs-scripts" % "1.2.0"
  )
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  .dependsOn(shared.jvm)

lazy val client = project
  .settings(
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.2.0",
      libraryDependencies += "me.shadaj" %%% "slinky-core" % "0.7.2",
      libraryDependencies += "me.shadaj" %%% "slinky-web" % "0.7.2",
//      libraryDependencies += "com.lambdaminute" %%% "slinky-wrappers-material-ui" % "0.4.1",
      scalacOptions += "-Ymacro-annotations",
      /*    stFlavour := Flavour.Slinky,
          useYarn := true,
          Compile / npmDependencies ++= Seq(
            "@material-ui/core" -> "3.9.4", // note: version 4 is not supported yet
            "@material-ui/styles" -> "3.0.0-alpha.10", // note: version 4 is not supported yet
            "@material-ui/icons" -> "3.0.2",
          )*/
      Compile / npmDependencies  ++= Seq(
        "react" -> "latest",
        "react-dom" -> "latest",
        "@mui/material" -> "latest",
        "@emotion/react" -> "latest",
        "@emotion/styled" -> "latest",
      ),
      webpackBundlingMode := BundlingMode.LibraryAndApplication(),
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .dependsOn(shared.js)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    libraryDependencies += "org.apache.commons" % "commons-text" % "1.9",
  )
  .in(file("shared"))
//  .jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin))
