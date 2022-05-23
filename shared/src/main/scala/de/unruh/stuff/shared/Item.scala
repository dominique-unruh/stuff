package de.unruh.stuff.shared

import de.unruh.stuff.shared.Item.newID
import org.apache.commons.text.StringEscapeUtils

import java.net.URL
import scala.util.Random

class RichText(html: String) {
  def isEmpty: Boolean = html.isEmpty
}
object RichText {
  val empty: RichText = new RichText("")
  def plain(text: String) = new RichText(StringEscapeUtils.escapeHtml4(text))
}

sealed trait Link
case class URLLink(uri: URL)
case class ServerSideLink(id: String)

case class Item(
                 /** Unique ID */
                 val id: Long = newID(),
                 /** Short name of the item. Plain text. */
                 val name: String,
                 /** Description of the item. HTML rich text */
                 val description: RichText = RichText.empty,
                 /** Photo of the item. */
                 val photo: Option[Link] = None,
               )

object Item {
  private def newID() = Random.nextLong()
  val testItems: Seq[Item] = Seq(
    Item(name="Shoe"),
    Item(name="Hat"),
    Item(name="Bubble gum", description=RichText.plain("Hardly used"))
  )
}