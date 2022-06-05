package de.unruh.stuff.db

import de.unruh.stuff.Paths
import de.unruh.stuff.shared.{Code, Item, RichText}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, PimpedAny, PimpedString, YamlFormat, YamlObject, YamlReader, YamlString, YamlValue, YamlWriter}

import scala.jdk.StreamConverters._
import java.io.{File, IOException}
import java.net.URI
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.Random

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
    writeField[A, A](value, field, condition, identity _)

  private def writeFieldOption[A: YamlWriter](value: Option[A], field: Fields.Value): Option[(YamlString, YamlValue)] =
    writeField[Option[A], A](value, field, _.nonEmpty, _.get)

  private def writeField[A, B: YamlWriter](value: A, field: Fields.Value, condition: A => Boolean, map: A => B): Option[(YamlString, YamlValue)] =
    if (condition(value))
      Some(YamlString(field.toString) -> map(value).toYaml)
    else
      None

  object Fields extends Enumeration {
    val id, name, description, photos, codes, files, lastmodified, location, prevlocation = Value
  }

  implicit object itemFormat extends YamlFormat[Item] {
    override def write(item: Item): YamlObject = YamlObject(Seq(
      writeField(item.id, Fields.id),
      writeField(item.name, Fields.name),
      writeField[RichText](item.description, Fields.description, _.nonEmpty),
      writeField[Seq[URI]](item.photos, Fields.photos, _.nonEmpty),
      writeField[Seq[Code]](item.codes, Fields.codes, _.nonEmpty),
      writeField[Long](item.lastModified, Fields.lastmodified),
      writeFieldOption(item.location, Fields.location),
      writeFieldOption(item.previousLocation, Fields.prevlocation)
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
        lastModified = readField[Long](yaml, Fields.lastmodified),
        location = readFieldOption[Item.Id](yaml, Fields.location),
        previousLocation = readFieldOption[Item.Id](yaml, Fields.prevlocation),
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

  def itemExists(id: Item.Id): Boolean =
    Files.exists(Paths.itemsPath(id))

  def createItem(path: Path, item: Item): Item.Id = {
    val item2 = item.copy(id = Random.nextInt(Int.MaxValue))
    assert(!itemExists(item2.id))
    updateItemMaybeNonExisting(path, item2)
    item2.id
  }

  def updateItem(path: Path, item: Item): Unit = {
    assert(itemExists(item.id))
    updateItemMaybeNonExisting(path, item)
  }

  private def updateItemMaybeNonExisting(path: Path, item: Item): Unit = {
    val item2 = ProcessItems.processItem(item)
    assert(item2.id != Item.INVALID_ID && item2.id >= 0)
    val itemPath = Paths.itemsPath(path, item2.id)
    val yaml = item2.toYaml.prettyPrint
    Files.writeString(itemPath, yaml)
  }
}
