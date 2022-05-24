package de.unruh.stuff.shared

import de.unruh.stuff.shared.Item.{Id, newID}
import org.apache.commons.text.StringEscapeUtils

import java.io.FileInputStream
import java.net.{URI, URL}
import java.nio.file.{Files, Path}
import scala.util.Random

class RichText(html: String) {
  def nonEmpty: Boolean = html.nonEmpty
  def isEmpty: Boolean = html.isEmpty
  def asHtml: String = html
  override def toString: String = html
}
object RichText {
  val empty: RichText = new RichText("")
  def plain(text: String) = new RichText(/*StringEscapeUtils.escapeHtml4*/(text)) // TODO: escape
  def html(html: String): RichText = new RichText(html)
  implicit val rw: upickle.default.ReadWriter[RichText] =
    upickle.default.readwriter[String].bimap[RichText](_.asHtml, RichText.html)
}

case class Item(
                 /** Unique ID */
                 val id: Id = newID(),
                 /** Short name of the item. Plain text. */
                 val name: String,
                 /** Description of the item. HTML rich text */
                 val description: RichText = RichText.empty,
                 /** Photos of the item. */
                 val photos: Seq[URI] = Nil,
               ) {
  def matches(searchTerms: String): Boolean = {
    val terms = searchTerms.toLowerCase.split("\\s")
    terms.forall(term => name.toLowerCase.contains(term) || description.asHtml.toLowerCase.contains(term))
  }
}

object Item {
  private def newID() = Random.nextLong()
  val testItems: Seq[Item] = Seq(
    Item(name="Shoe"),
    Item(name="Hat", description = RichText.html("A little test <i>with HTML</i>")),
    Item(name="Kitten", photos=List(new URI("https://api.time.com/wp-content/uploads/2019/03/kitten-report.jpg?quality=85&w=800"))),
    Item(name="Bubble gum", description=RichText.plain("Hardly used"))
  )

  implicit val rwUri: upickle.default.ReadWriter[URI] =
  upickle.default.readwriter[String].bimap[URI](_.toString, URI.create)

  implicit val rw: upickle.default.ReadWriter[Item] = upickle.default.macroRW

  type Id = Long
}