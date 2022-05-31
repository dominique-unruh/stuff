package de.unruh.stuff.db

import de.unruh.stuff.Paths
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

  private def readField[A: YamlReader](yaml: YamlValue, field: Fields.Value): A =
    readFieldOption[A](yaml, field).getOrElse {
      throw new IOException(s"Invalid yaml file: field $field missing.")
    }

  private def readFieldOption[A: YamlReader](yaml: YamlValue, field: Fields.Value): Option[A] = yaml match {
    case YamlObject(fields) => fields.get(YamlString(field.toString)).map(_.convertTo[A])
    case _ => throw new IOException("Invalid yaml file")
  }

  private def readFieldDefault[A: YamlReader](yaml: YamlValue, field: Fields.Value, default: => A): A =
    readFieldOption[A](yaml, field).getOrElse(default)

  private def writeField[A: YamlWriter](value: A, field: Fields.Value, condition: A => Boolean = { _:A => true }): Option[(YamlString, YamlValue)] =
    if (condition(value))
      Some(YamlString(field.toString) -> value.toYaml)
    else
      None

  object Fields extends Enumeration {
    val id, name, description, photos, codes, files = Value
  }

  implicit object itemFormat extends YamlFormat[Item] {
    override def write(item: Item): YamlObject = YamlObject(Seq(
      writeField(item.id, Fields.id),
      writeField(item.name, Fields.name),
      writeField[RichText](item.description, Fields.description, _.nonEmpty),
      writeField[Seq[URI]](item.photos, Fields.photos, _.nonEmpty),
      writeField[Seq[Code]](item.codes, Fields.codes, _.nonEmpty),
    ).collect { case Some(v) => v } :_*)

    override def read(yaml: YamlValue): Item = {
      assert(yaml.isInstanceOf[YamlObject])
      for (key <- yaml.asYamlObject.fields.keys) {
        assert(key.isInstanceOf[YamlString])
        Fields.withName(key.asInstanceOf[YamlString].value) // Throws exception if key is not a known key
      }
      Item(id = readField[Long](yaml, Fields.id),
        name = readField[String](yaml, Fields.name),
        description = readFieldDefault(yaml, Fields.description, RichText.empty),
        photos = readFieldDefault[Seq[URI]](yaml, Fields.photos, Nil),
        codes = readFieldDefault[Seq[Code]](yaml, Fields.codes, Nil),
      )
    }
  }

  def parse(yaml: String): Item = yaml.parseYaml.convertTo[Item]

  def parse(path: Path): Item = parse(Files.readString(path))

  def loadDb(path: Path): Map[Long, Item] = {
    Map.from(
      for (file <- Files.list(Paths.itemsPath(path)).iterator.asScala;
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
    val itemPath = Paths.itemsPath(path).resolve(s"${item.id}.yaml")
    assert(Files.exists(itemPath))
    val yaml = item.toYaml.prettyPrint
    Files.writeString(itemPath, yaml)
  }
}
