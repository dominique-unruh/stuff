package de.unruh.stuff

import de.unruh.stuff.shared.{Item, Utils}

import java.net.URI
import scala.util.matching.Regex

object ExtendedURL {
  val idRegex: Regex = "[1-9][0-9]*".r
  val fileRegex: Regex = "[A-Za-z0-9_@,][A-Za-z0-9_@,.-]*".r
  val fileSchemaSpecificRegex: Regex = "([1-9][0-9]*)/([A-Za-z0-9_@,][A-Za-z0-9_@,.-]*)".r
  val itemSchemaSpecificRegex: Regex = "([1-9][0-9]*)".r

  def forFile(id: Item.Id, filename: String): URI = {
    assert(fileRegex.matches(filename))
    new URI("localstuff", s"$id/$filename", null)
  }

  def forItem(id: Item.Id): URI = {
    assert(id >= 0)
    new URI("localstuff", id.toString, null)
  }
}
