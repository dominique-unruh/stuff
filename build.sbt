ThisBuild / scalaVersion := "2.13.8"

ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """stuff""",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "org.mockito" % "mockito-core" % "4.5.1" % Test,
      "org.scalatestplus" %% "mockito-4-5" % "3.2.12.0" % Test)
  )