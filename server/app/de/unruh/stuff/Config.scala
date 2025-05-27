package de.unruh.stuff

import play.api.{ConfigLoader, Configuration}

import java.nio.file.Path
import javax.inject.{Inject, Singleton}

@Singleton
class Config @Inject() (configuration: Configuration) {
  private def get[A: ConfigLoader](name: String, default: A = null): A = {
    val value = configuration.get[A](name)
    if (value == null)
      if (default == null)
        throw new RuntimeException(s"Server configuration lacks key $name")
      else
        default
    else
      value
  }

  implicit private val configLoaderPath : ConfigLoader[Path] = implicitly[ConfigLoader[String]].map(Path.of(_))

//  lazy val users : Seq[String] = get[Seq[String]]("stuff.users")
  lazy val googleClientId : String =
    get[String]("stuff.google_client_id")
  lazy val dbRoot : Path =
    get[Path]("stuff.db_root")
  lazy val applicationContext: String =
    get[String]("application.context", "").stripSuffix("/")
}
