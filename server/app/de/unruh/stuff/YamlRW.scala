package de.unruh.stuff

import de.unruh.stuff.shared.{Code, Item, RichText}
import net.jcazevedo.moultingyaml.{DefaultYamlProtocol, PimpedAny, YamlFormat, YamlObject, YamlReader, YamlString, YamlValue, YamlWriter}

import java.io.IOException
import java.net.URI

/** YAML readers/writers for different types */
object YamlRW {

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

  private def readField[A: YamlReader](yaml: YamlValue, field: AnyRef): A =
    readFieldOption[A](yaml, field).getOrElse {
      throw new IOException(s"Invalid yaml file: field $field missing.")
    }

  private def readFieldOption[A: YamlReader](yaml: YamlValue, field: AnyRef): Option[A] = yaml match {
    case YamlObject(fields) => fields.get(YamlString(field.toString)).map(_.convertTo[A])
    case _ => throw new IOException("Invalid yaml file")
  }

  private def readFieldDefault[A: YamlReader](yaml: YamlValue, field: ItemFields.Value, default: => A): A =
    readFieldOption[A](yaml, field).getOrElse(default)

  private def writeField[A: YamlWriter](value: A, field: ItemFields.Value, condition: A => Boolean = { _: A => true }): Option[(YamlString, YamlValue)] =
    writeField[A, A](value, field, condition, identity)

  private def writeFieldOption[A: YamlWriter](value: Option[A], field: ItemFields.Value): Option[(YamlString, YamlValue)] =
    writeField[Option[A], A](value, field, _.nonEmpty, _.get)

  private def writeField[A, B: YamlWriter](value: A, field: ItemFields.Value, condition: A => Boolean, map: A => B): Option[(YamlString, YamlValue)] =
    if (condition(value))
      Some(YamlString(field.toString) -> map(value).toYaml)
    else
      None

  //noinspection SpellCheckingInspection
  object ItemFields extends Enumeration {
    val id, name, description, photos, codes, files, lastmodified, location, prevlocation = Value
  }

  implicit object itemFormat extends YamlFormat[Item] {
    override def write(item: Item): YamlObject = YamlObject(Seq(
      writeField(item.id, ItemFields.id),
      writeField(item.name, ItemFields.name),
      writeField[RichText](item.description, ItemFields.description, _.nonEmpty),
      writeField[Seq[URI]](item.photos, ItemFields.photos, _.nonEmpty),
      writeField[Seq[Code]](item.codes, ItemFields.codes, _.nonEmpty),
      writeField[Long](item.lastModified, ItemFields.lastmodified),
      writeFieldOption(item.location, ItemFields.location),
      writeFieldOption(item.previousLocation, ItemFields.prevlocation)
    ).collect { case Some(v) => v }: _*)

    override def read(yaml: YamlValue): Item = {
      assertCorrectFields(yaml, ItemFields)
      Item(id = readField[Long](yaml, ItemFields.id),
        name = readField[String](yaml, ItemFields.name),
        description = readFieldDefault(yaml, ItemFields.description, RichText.empty),
        photos = readFieldDefault[Seq[URI]](yaml, ItemFields.photos, Nil),
        codes = readFieldDefault[Seq[Code]](yaml, ItemFields.codes, Nil),
        lastModified = readField[Long](yaml, ItemFields.lastmodified),
        location = readFieldOption[Item.Id](yaml, ItemFields.location),
        previousLocation = readFieldOption[Item.Id](yaml, ItemFields.prevlocation),
      )
    }
  }

  private def assertCorrectFields(yaml: YamlValue, fields: Enumeration): Unit = {
    assert(yaml.isInstanceOf[YamlObject])
    for (key <- yaml.asYamlObject.fields.keys) {
      assert(key.isInstanceOf[YamlString])
      fields.withName(key.asInstanceOf[YamlString].value) // Throws exception if key is not a known key
    }
  }

  //noinspection TypeAnnotation
  object ConfigFields extends Enumeration {
    val users = Value
  }

  //noinspection TypeAnnotation
  object ConfigUserFields extends Enumeration {
    val password = Value
  }

  implicit object configFormat extends YamlFormat[Config] {
    override def write(config: Config): YamlObject =
      throw new UnsupportedOperationException("writing config files")

    override def read(yaml: YamlValue): Config = {
      assertCorrectFields(yaml, ConfigFields)
      Config(users = readField[Map[String, Config.User]](yaml, ConfigFields.users),
      )
    }
  }

  implicit object configUserFormat extends YamlFormat[Config.User] {
    override def write(user: Config.User): YamlObject =
      throw new UnsupportedOperationException("writing config files")

    override def read(yaml: YamlValue): Config.User = {
      assertCorrectFields(yaml, ConfigUserFields)
      Config.User(password = readField[String](yaml, ConfigUserFields.password),
      )
    }
  }

}
