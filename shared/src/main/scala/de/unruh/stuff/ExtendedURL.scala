package de.unruh.stuff

import de.unruh.stuff.shared.{Item, Utils}

import java.net.URI
import scala.util.matching.Regex

object ExtendedURL {
  val idRegex: Regex = "[1-9][0-9]*".r
  val fileRegex: Regex = "[A-Za-z0-9_@,][A-Za-z0-9_@,.-]*".r
  val schemaSpecificRegex: Regex = "([1-9][0-9]*)/([A-Za-z0-9_@,][A-Za-z0-9_@,.-]*)".r

  def resolve(username: String, url: URI): String = {
    if (url.getScheme == "localstuff") {
      url.getRawSchemeSpecificPart match {
        case schemaSpecificRegex(id, file) => s"/files/${Utils.encodeURIComponent(username)}/${id}/${file}"
        case _ => throw new AssertionError(url.toString)
      }
    } else
      url.toString
  }

  def forFile(id: Item.Id, filename: String): URI = {
    assert(fileRegex.matches(filename))
    new URI("localstuff", s"$id/$filename", null)
  }
}
