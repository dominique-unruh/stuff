package de.unruh.stuff.db

import de.unruh.stuff.shared.{Code, Item, RichText}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, PimpedAny, PimpedString, YamlFormat, YamlObject, YamlReader, YamlString, YamlValue, YamlWriter}

import scala.jdk.StreamConverters._
import java.io.{File, IOException}
import java.net.URI
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.IteratorHasAsScala

object Yaml {

  import DefaultYamlProtocol._

  implicit object richTextFormat extends YamlFormat[RichText] {
    override def write(obj: RichText): YamlValue = obj.asHtml.toYaml

    override def read(yaml: YamlValue): RichText = RichText.html(yaml.convertTo[String])
  }

  implicit object uriFormat extends YamlFormat[URI] {
    override def write(obj: URI): YamlValue = obj.toString.toYaml

    override def read(yaml: YamlValue): URI = URI.create(yaml.convertTo[String])
  }

  implicit object codeFormat extends YamlFormat[Code] {
    override def write(obj: Code): YamlValue = obj.toString.toYaml

    override def read(yaml: YamlValue): Code = Code(yaml.convertTo[String])
  }

  private def readField[A: YamlReader](yaml: YamlValue, name: String): A =
    readFieldOption[A](yaml, name).getOrElse {
      throw new IOException(s"Invalid yaml file: field $name missing.")
    }

  private def readFieldOption[A: YamlReader](yaml: YamlValue, name: String): Option[A] = yaml match {
    case YamlObject(fields) => fields.get(YamlString(name)).map(_.convertTo[A])
    case _ => throw new IOException("Invalid yaml file")
  }

  private def readFieldDefault[A: YamlReader](yaml: YamlValue, name: String, default: => A): A =
    readFieldOption[A](yaml, name).getOrElse(default)

  private def writeField[A: YamlWriter](value: A, name: String, condition: A => Boolean = { _:A => true }): Option[(YamlString, YamlValue)] =
    if (condition(value))
      Some(YamlString(name) -> value.toYaml)
    else
      None

  implicit object itemFormat extends YamlFormat[Item] {
    override def write(item: Item): YamlObject = YamlObject(Seq(
      writeField(item.id, "id"),
      writeField(item.name, "name"),
      writeField[RichText](item.description, "description", _.nonEmpty),
      writeField[Seq[URI]](item.photos, "photos", _.nonEmpty),
      writeField[Seq[URI]](item.links, "links", _.nonEmpty),
      writeField[Seq[Code]](item.codes, "codes", _.nonEmpty),
    ).collect { case Some(v) => v } :_*)

    override def read(yaml: YamlValue): Item =
      Item(id = readField[Long](yaml, "id"),
        name = readField[String](yaml, "name"),
        description = readFieldDefault(yaml, "description", RichText.empty),
        photos = readFieldDefault[Seq[URI]](yaml, "photos", Nil),
        links = readFieldDefault[Seq[URI]](yaml, "links", Nil),
        codes = readFieldDefault[Seq[Code]](yaml, "codes", Nil),
      )
  }

  def parse(yaml: String): Item = yaml.parseYaml.convertTo[Item]

  def parse(path: Path): Item = parse(Files.readString(path))

  def loadDb(path: Path): Map[Long, Item] = {
    Map.from(
      for (file <- Files.list(path.resolve("items")).iterator.asScala;
           filename = file.getFileName.toString;
           if !filename.startsWith(".")
           if !filename.endsWith("~")
           ) yield {
        assert(filename.endsWith(".yaml"))
        val id = filename.stripSuffix(".yaml").toLong
        val item = parse(file)
        assert(item.id == id)
        id -> item
      })
  }

  def updateItem(path: Path, item: Item): Unit = {
    assert(item.id != Item.INVALID_ID && item.id >= 0)
    val itemPath = path.resolve("items").resolve(s"${item.id}.yaml")
    assert(Files.exists(itemPath))
    val yaml = item.toYaml.prettyPrint
    Files.writeString(itemPath, yaml)
  }
}
