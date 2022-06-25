package de.unruh.stuff

import de.unruh.stuff.shared.Item
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, PimpedAny, PimpedString, YamlFormat, YamlObject, YamlReader, YamlString, YamlValue, YamlWriter}
import YamlRW._
import de.unruh.stuff.Config.User

import java.nio.file.{Files, Path}

case class Config(users: Map[String, User], googleClientId: String)

object Config {
  case class User(
                 /** Password of the user (ignored) */
                   password: String
                 )

  lazy val config: Config = loadConfig(Paths.configPath)

  private def loadConfig(path: Path) = {
    assert(Files.exists(path), "Config file not found")
    Files.readString(path).parseYaml.convertTo[Config]
  }
}