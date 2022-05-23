package de.unruh.stuff.shared

import de.unruh.stuff.shared.Item.newID
import org.apache.commons.text.StringEscapeUtils

import java.net.{URI, URL}
import scala.util.Random

class RichText(html: String) {
  def nonEmpty: Boolean = html.nonEmpty
  def isEmpty: Boolean = html.isEmpty
  def asHtml: String = html
}
object RichText {
  val empty: RichText = new RichText("")
  def plain(text: String) = new RichText(/*StringEscapeUtils.escapeHtml4*/(text)) // TODO: escape
  def html(html: String): RichText = new RichText(html)
}

case class Item(
                 /** Unique ID */
                 val id: Long = newID(),
                 /** Short name of the item. Plain text. */
                 val name: String,
                 /** Description of the item. HTML rich text */
                 val description: RichText = RichText.empty,
                 /** Photos of the item. */
                 val photos: Seq[URI] = Nil,
               )

object Item {
  private def newID() = Random.nextLong()
  val testItems: Seq[Item] = Seq(
    Item(name="Shoe"),
    Item(name="Hat", description = RichText.html("A little test <i>with HTML</i>")),
    Item(name="Kitten", photos=List(new URI("https://api.time.com/wp-content/uploads/2019/03/kitten-report.jpg?quality=85&w=800"))),
    Item(name="Bubble gum", description=RichText.plain("Hardly used"))
  )
}