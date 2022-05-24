package de.unruh.stuff.db

import de.unruh.stuff.shared.{Item, RichText}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, PimpedString, YamlFormat, YamlObject, YamlReader, YamlString, YamlValue, YamlWriter}

import scala.jdk.StreamConverters._
import java.io.{File, IOException}
import java.net.URI
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.IteratorHasAsScala

object Yaml {
  import DefaultYamlProtocol._

  implicit object richTextFormat extends YamlFormat[RichText] {
    override def write(obj: RichText): YamlValue = ???
    override def read(yaml: YamlValue): RichText = RichText.html(yaml.convertTo[String])
  }
  implicit object uriFormat extends YamlFormat[URI] {
    override def write(obj: URI): YamlValue = ???
    override def read(yaml: YamlValue): URI = URI.create(yaml.convertTo[String])
  }
  private def readField[A: YamlReader](yaml: YamlValue, name: String) : A =
    readFieldOption[A](yaml, name).getOrElse {
      throw new IOException(s"Invalid yaml file: field $name missing.")
    }

  private def readFieldOption[A: YamlReader](yaml: YamlValue, name: String) : Option[A] = yaml match {
    case YamlObject(fields) => fields.get(YamlString(name)).map(_.convertTo[A])
    case _ => throw new IOException("Invalid yaml file")
  }
  private def readFieldDefault[A: YamlReader](yaml: YamlValue, name: String, default: => A) : A =
    readFieldOption[A](yaml, name).getOrElse(default)

  implicit object itemFormat extends YamlFormat[Item] {
    override def write(obj: Item): YamlValue = ???
    override def read(yaml: YamlValue): Item =
      Item(id=readField[Long](yaml, "id"),
        name=readField[String](yaml, "name"),
        description = readFieldDefault(yaml, "description", RichText.empty),
        photos = readFieldDefault[Seq[URI]](yaml, "photos", Nil)
      )
  }

  def parse(yaml: String): Item = yaml.parseYaml.convertTo[Item]
  def parse(path: Path): Item = parse(Files.readString(path))

  def loadDb(path: Path) : Map[Long, Item] = {
    Map.from(
      for (file <- Files.list(path.resolve("items")).iterator.asScala) yield {
        val filename = file.getFileName.toString
        assert(filename.endsWith(".yaml"))
        val id = filename.stripSuffix(".yaml").toLong
        val item = parse(file)
        assert(item.id == id)
        id -> item
      })
  }
}

