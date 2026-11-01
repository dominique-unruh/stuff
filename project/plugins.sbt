// Comment to get more information during initialization
logLevel := Level.Warn

addSbtPlugin("com.vmunier"               % "sbt-web-scalajs"           % "1.3.0")
addSbtPlugin("org.scala-js"              % "sbt-scalajs"               % "1.22.0")
addSbtPlugin("org.playframework"         % "sbt-plugin"                % "3.0.12")
addSbtPlugin("org.portable-scala"        % "sbt-scalajs-crossproject"  % "1.3.2")
addSbtPlugin("com.github.sbt"            % "sbt-gzip"                  % "2.0.0")
addSbtPlugin("com.github.sbt"            % "sbt-digest"                % "2.1.0")
//addSbtPlugin("org.scalablytyped.converter" % "sbt-converter"           % "1.0.0-beta37")
// Excluding the old sbt-web(-scalajs) it depends on, they clash with the com.github.sbt sbt-web used by Play 3
addSbtPlugin(("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.21.1")
  .exclude("com.vmunier", "sbt-web-scalajs")
  .exclude("com.typesafe.sbt", "sbt-web"))
//addSbtPlugin("com.heroku" % "sbt-heroku" % "2.1.4")
// Invoke via dependencyUpdates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.7.1")
