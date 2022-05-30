import sbt.Compile

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
    libraryDependencies += "com.vmunier" %% "scalajs-scripts" % "1.2.0",
    libraryDependencies += "net.jcazevedo" %% "moultingyaml" % "0.4.2",
    libraryDependencies += "com.lihaoyi" %% "ujson-play" % "2.0.0",
    libraryDependencies += "com.google.zxing" % "core" % "3.5.0",
    libraryDependencies += "com.google.zxing" % "javase" % "3.5.0",
  )
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  .dependsOn(shared.jvm)

lazy val client = project
  .settings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.2.0",
    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "2.1.1",
    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "extra" % "2.1.1",
    libraryDependencies += "io.kinoplan" %%% "scalajs-react-material-ui-core"  % "0.3.1",
    scalacOptions += "-Ymacro-annotations",
    Compile / npmDependencies  ++= Seq(
      "react" -> "17.0.2",
      "react-dom" -> "17.0.2",
      "@material-ui/core"  -> "3.9.4",
      "@zxing/library" -> "latest",
    ),
    Compile / npmExtraArgs += "--legacy-peer-deps",
    webpackBundlingMode := BundlingMode.LibraryAndApplication(),
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .dependsOn(shared.js)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    libraryDependencies += "org.apache.commons" % "commons-text" % "1.9",
    libraryDependencies += "com.lihaoyi" %%% "autowire" % "0.3.3",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "2.0.0"
  )
  .in(file("shared"))
//  .jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin))

// Doesn't work:
Compile / herokuAppName := "unruh-stuff"
Compile / herokuJdkVersion := "11"
Compile / herokuProcessTypes := Map(
  "web" -> "target/universal/stage/bin/server -Dhttp.port=$PORT -Dconfig.file=server/conf/heroku.conf",
)