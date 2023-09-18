// Comment to get more information during initialization
logLevel := Level.Warn

evictionErrorLevel := Level.Warn // TODO remove, but currently needed because of https://github.com/sbt/sbt/issues/6997#issue-1332853454

addSbtPlugin("com.vmunier"               % "sbt-web-scalajs"           % "1.2.0")
addSbtPlugin("org.scala-js"              % "sbt-scalajs"               % "1.13.2")
addSbtPlugin("com.typesafe.play"         % "sbt-plugin"                % "2.8.20")
addSbtPlugin("org.portable-scala"        % "sbt-scalajs-crossproject"  % "1.3.2")
addSbtPlugin("com.typesafe.sbt"          % "sbt-gzip"                  % "1.0.2")
addSbtPlugin("com.typesafe.sbt"          % "sbt-digest"                % "1.1.4")
//addSbtPlugin("org.scalablytyped.converter" % "sbt-converter"           % "1.0.0-beta37")
addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.21.1")
//addSbtPlugin("com.heroku" % "sbt-heroku" % "2.1.4")
// Invoke via dependencyUpdates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.0")