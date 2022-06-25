package de.unruh.stuff

import play.api.{ConfigLoader, Configuration}

object Config {
  private def get[A: ConfigLoader](name: String)(implicit configuration: Configuration): A = {
    val value = configuration.get[A](name)
    if (value == null)
      throw new RuntimeException(s"Server configuration lacks key $name")
    value
  }

  def users(implicit configuration: Configuration) : Seq[String] =
    get[Seq[String]]("stuff.users")
  def googleClientId(implicit configuration: Configuration) : String =
    get[String]("stuff.google_client_id")
}
