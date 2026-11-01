import sbt.Compile

/** Local experiments in sbt:
 * project server
 * run -Dhttps.port=9443"
 * */

// Upon error involving 'ERR_OSSL_EVP_UNSUPPORTED' and webpage, run sbt via:
// NODE_OPTIONS=--openssl-legacy-provider sbt test

ThisBuild / organization := "de.unruh"
ThisBuild / scalaVersion := "2.13.18"
ThisBuild / version      := "0.1.0-SNAPSHOT"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .settings(
    name := "stuff"
  )
  .aggregate(server, client, shared.jvm, shared.js)

lazy val server = project
  .settings(
    // To make Intelli/J happy (otherwise compiling classes from client project may fail)
    scalacOptions += "-Ymacro-annotations",
    scalaJSProjects := Seq(client),
    Assets / pipelineStages  := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
    libraryDependencies += guice,
    libraryDependencies += "com.vmunier" %% "scalajs-scripts" % "1.3.0",
    libraryDependencies += "net.jcazevedo" %% "moultingyaml" % "0.4.2",
    // moultingyaml pulls in snakeyaml 1.26
    libraryDependencies += "org.yaml" % "snakeyaml" % "2.7",
    // ujson-play depends on com.typesafe.play:play-json, Play 3 provides the same classes as org.playframework:play-json
    libraryDependencies += ("com.lihaoyi" %% "ujson-play" % "4.4.3").exclude("com.typesafe.play", "play-json_2.13"),
    libraryDependencies += "com.google.zxing" % "core" % "3.5.4",
    libraryDependencies += "com.google.zxing" % "javase" % "3.5.4",
    libraryDependencies += "io.lemonlabs" %% "scala-uri" % "4.0.3",
    libraryDependencies += "org.apache.tika" % "tika-core" % "4.2.0",
    libraryDependencies += "com.google.api-client" % "google-api-client" % "2.9.2",
    // To make Intelli/J happy (otherwise compiling classes from client project may fail)
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.20" % "test",
    // Play 3.0 pins Jackson 2.14 and an older Guava, both with known CVEs
    dependencyOverrides ++= {
      val jacksonVersion = "2.18.12"
      Seq(
        "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
        "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
        "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
        "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % jacksonVersion,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % jacksonVersion,
        "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion,
        "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % jacksonVersion,
        "com.google.guava" % "guava" % "33.7.2-android",
      )
    },
    maintainer := "dominique@unruh.de",
  )
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  .disablePlugins(PlayLogback)
  .dependsOn(shared.jvm)

lazy val client = project
  .settings(
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.6.0",
    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "2.1.1",
    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "extra" % "2.1.1",
    // Only the bridge, the MUI facades are in de.unruh.stuff.mui
    libraryDependencies += "io.kinoplan" %%% "scalajs-react-bridge"  % "0.3.1",
    scalacOptions += "-Ymacro-annotations",
    Compile / npmDependencies  ++= Seq(
      "react" -> "17.0.2",
      "react-dom" -> "17.0.2",
      "@mui/material"  -> "9.5.0",
      "@emotion/react"  -> "11.14.0",
      "@emotion/styled"  -> "11.14.1",
      "@zxing/library" -> "0.20.0",
      "react-simple-wysiwyg" -> "2.2.5",
      "react-webcam" -> "7.1.1",
      "notistack" -> "3.0.2",
    ),
    Compile / npmExtraArgs += "--legacy-peer-deps",
    // scalajs-bundler defaults are old versions with known vulnerabilities (build-time only)
    webpack / version := "5.112.2",
    webpackCliVersion := "5.1.4",
    startWebpackDevServer / version := "6.1.0",
    webpackBundlingMode := BundlingMode.LibraryAndApplication(),
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .dependsOn(shared.js)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies += "org.apache.commons" % "commons-text" % "1.15.0",
    libraryDependencies += "com.lihaoyi" %%% "autowire" % "0.3.3",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "4.4.3",
    libraryDependencies += "dev.optics" %%% "monocle-core"  % "3.2.0",
    libraryDependencies += "dev.optics" %%% "monocle-macro" % "3.2.0",
    libraryDependencies += "org.log4s" %%% "log4s" % "1.10.0",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.38",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.20" % "test",
  )
  .in(file("shared"))
//  .jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin))

/*// Doesn't work:
Compile / herokuAppName := "unruh-stuff"
Compile / herokuJdkVersion := "11"
Compile / herokuProcessTypes := Map(
  "web" -> "target/universal/stage/bin/server -Dhttp.port=$PORT -Dconfig.file=server/conf/heroku.conf",
)*/