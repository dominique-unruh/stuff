package de.unruh.stuff

import de.unruh.stuff.shared.Item
import org.scalajs.dom.document
import scala.scalajs.js.annotation.JSExportTopLevel


object AppTest {
  @JSExportTopLevel("test")
  def test(username: String): Unit = {
    // TODO: use a router
    val root = document.getElementById("react-root")
    ItemSearch(onClick = {
      item:Item.Id => ItemEditor(item).renderIntoDOM(root)
    }).renderIntoDOM(root)
  }
}
