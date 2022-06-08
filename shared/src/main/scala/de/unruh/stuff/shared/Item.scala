package de.unruh.stuff.shared

import de.unruh.stuff.shared.Code.urlRegex
import de.unruh.stuff.shared.Item.{Id, newID}
import monocle.macros.Lenses
import org.apache.commons.text.StringEscapeUtils

import java.io.FileInputStream
import java.net.{URI, URL}
import java.nio.file.attribute.FileTime
import java.nio.file.{Files, Path}
import java.time.Instant
import scala.util.Random
import scala.util.matching.Regex

class RichText(html: String) {
  def nonEmpty: Boolean = html.nonEmpty
  def isEmpty: Boolean = html.isEmpty
  def asHtml: String = html
  override def toString: String = html
}
object RichText {
  val empty: RichText = new RichText("")
//  def plain(text: String) = new RichText(StringEscapeUtils.escapeHtml4(text))
  def html(html: String): RichText = new RichText(html)
  implicit val rw: upickle.default.ReadWriter[RichText] =
    upickle.default.readwriter[String].bimap[RichText](_.asHtml, RichText.html)
}

case class Code(format: Option[String], content: String) {
  def link: Option[URI] =
    try (format.getOrElse("UNKNOWN"), content) match {
      case ("QR_CODE",Code.urlRegex()) => Some(URI.create(content))
      case ("EAN_13",Code.isbnRegex()) => Some(URI.create(s"https://www.goodreads.com/search?query=$content"))
      case ("EAN_13" | "EAN_8" | "UPC_A" | "UPC_E", _) => Some(URI.create(s"https://www.barcodelookup.com/$content"))
          // Alternative: https://www.ean-search.org/?q=XXXXXXXXX
      case _ => None
    } catch {
      case e : Throwable => None
        // TODO: log error
    }

  /** True if the two codes match. The contents must be equal, and the formats must be equal if both are not `None`. */
  def matches(other: Code): Boolean =
    (content == other.content) &&
      (format.isEmpty || other.format.isEmpty || format == other.format)

  assert(format.getOrElse("") != "UNKNOWN")
  override def toString: String = s"${format.getOrElse("UNKNOWN")}:$content"
}

object Code {
  val urlRegex: Regex = "https?://.*".r
  val isbnRegex: Regex = "978[0-9]{10}".r

  val codeRegex: Regex = "([A-Za-z0-9_-]+):(.*)".r
  def apply(string : String): Code = {
    string match {
      case `codeRegex`(format, content) =>
        if (format == "UNKNOWN")
          Code(None, content)
        else
          Code(Some(format), content)
      case _ =>
        throw new RuntimeException(s"Invalid QR/barcode specifier: $string")
    }
  }
  implicit val rw: upickle.default.ReadWriter[Code] = upickle.default.macroRW
}

@Lenses case class Item(
                 /** Unique ID */
                 id: Id = newID(),
                 /** Short name of the item. Plain text. */
                 name: String,
                 /** Description of the item. HTML rich text */
                 description: RichText = RichText.empty,
                 /** Photos of the item. */
                 photos: Seq[URI] = Nil,
                 /** QR / barcodes */
                 codes: Seq[Code] = Nil,
                 /** Last access */
                 lastModified: Long,
                 /** Location of the item */
                 location: Option[Item.Id] = None,
                 /** Previous location of the item */
                 previousLocation: Option[Item.Id] = None,
               ) {

  /** Sets the location (and stores the previous location, if set, in [[previousLocation]]) */
  def setLocation(location: Option[Item.Id]): Item = copy(location = location,
    previousLocation = this.location match { case None => previousLocation; case Some(loc) => Some(loc) })
  def setLocation(location: Item.Id): Item = setLocation(Some(location))
  def clearLocation(): Item = setLocation(None)
}

object Item {
  private def newID(): Item.Id = Random.nextLong()

  implicit val rwUri: upickle.default.ReadWriter[URI] =
    upickle.default.readwriter[String].bimap[URI](_.toString, URI.create)

  implicit val rw: upickle.default.ReadWriter[Item] = upickle.default.macroRW

  type Id = Long

  /** Represents an id that will never occur */
  val INVALID_ID : Id = -1

  /** An item with id [[INVALID_ID]] and dummy content. */
  val invalid: Item = Item(id = INVALID_ID, name = "Invalid item: You should never see this. Please file a bug report.", lastModified = -1)

  /** Creates an empty item (for editing). */
  def create(code: Option[Code]): Item = Item(id=INVALID_ID, name = "", lastModified = 0,
    codes = code.toSeq)
}