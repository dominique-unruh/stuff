import sbt.Compile

/** Local experiments in sbt:
 * project server
 * run -Dhttps.port=9443"
 * */

/* TODO: Try updating Play version (and other version).
The following two issues are hopefully fixed then:
https://github.com/sbt/sbt/issues/6997#issue-1332853454
https://github.com/akka/akka/issues/29922
*/

// Upon error involving 'ERR_OSSL_EVP_UNSUPPORTED' and webpage, run sbt via:
// NODE_OPTIONS=--openssl-legacy-provider sbt test

ThisBuild / organization := "de.unruh"
ThisBuild / scalaVersion := "2.13.12"
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
    libraryDependencies += "com.vmunier" %% "scalajs-scripts" % "1.2.0",
    libraryDependencies += "net.jcazevedo" %% "moultingyaml" % "0.4.2",
    libraryDependencies += "com.lihaoyi" %% "ujson-play" % "3.1.3",
    libraryDependencies += "com.google.zxing" % "core" % "3.5.2",
    libraryDependencies += "com.google.zxing" % "javase" % "3.5.2",
    libraryDependencies += "io.lemonlabs" %% "scala-uri" % "4.0.3",
    libraryDependencies += "org.apache.tika" % "tika-core" % "2.9.0",
    libraryDependencies += "com.google.api-client" % "google-api-client" % "2.2.0",
    // To make Intelli/J happy (otherwise compiling classes from client project may fail)
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % "test",
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
    libraryDependencies += "io.kinoplan" %%% "scalajs-react-material-ui-core"  % "0.3.1",
    scalacOptions += "-Ymacro-annotations",
    Compile / npmDependencies  ++= Seq(
      "react" -> "17.0.2",
      "react-dom" -> "17.0.2",
      "@material-ui/core"  -> "3.9.4",
      "@zxing/library" -> "latest",
      "react-simple-wysiwyg" -> "latest",
      "react-webcam" -> "latest",
      "notistack" -> "0.4.1",
    ),
    Compile / npmExtraArgs += "--legacy-peer-deps",
    webpackBundlingMode := BundlingMode.LibraryAndApplication(),
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .dependsOn(shared.js)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies += "org.apache.commons" % "commons-text" % "1.10.0",
    libraryDependencies += "com.lihaoyi" %%% "autowire" % "0.3.3",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "3.1.3",
    libraryDependencies += "dev.optics" %%% "monocle-core"  % "3.2.0",
    libraryDependencies += "dev.optics" %%% "monocle-macro" % "3.2.0",
    libraryDependencies += "org.log4s" %%% "log4s" % "1.10.0",
//    libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.36", // Newer version incompatible with Logback version loaded by Play
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.11",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.17" % "test",
  )
  .in(file("shared"))
//  .jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin))

/*// Doesn't work:
Compile / herokuAppName := "unruh-stuff"
Compile / herokuJdkVersion := "11"
Compile / herokuProcessTypes := Map(
  "web" -> "target/universal/stage/bin/server -Dhttp.port=$PORT -Dconfig.file=server/conf/heroku.conf",
)*/