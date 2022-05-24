package de.unruh.stuff

import de.unruh.stuff.shared.Item.testItems
import de.unruh.stuff.shared.{Item, SharedMessages}
import org.scalajs.dom
import org.scalajs.dom.{Event, HTMLInputElement, console, document}
import slinky.core.{Component, CustomAttribute, CustomTag, ExternalComponent, ExternalComponentNoProps, ExternalComponentWithAttributes, ExternalComponentWithRefType, SyntheticEvent, WithAttrs}
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.ReactDOM
import slinky.web.html.{`type`, div, h1, input, onChange, value}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSImport}
import scala.scalajs.js.{UndefOr, |}




/*@react object Card extends ExternalComponent {
  case class Props(className: UndefOr[String] = js.undefined, raised: UndefOr[Boolean] = js.undefined)
  override val component = MaterialUi.Card
}

@react object CardContent extends ExternalComponent {
  case class Props(component: UndefOr[String | js.Function] = js.undefined)
  override val component = MaterialUi.CardContent
}

@react object CardMedia extends ExternalComponent {
  case class Props(className: UndefOr[String] = js.undefined,
                   component: UndefOr[String | js.Function] = js.undefined,
                   image: UndefOr[String] = js.undefined,
                   height: UndefOr[Int] = js.undefined,
                   src: UndefOr[String] = js.undefined)
  override val component = MaterialUi.CardMedia
}*/

/*@react object Button extends ExternalComponentWithAttributes[slinky.web.html.button.tag.type] {
  case class Props(
                   onClick: UndefOr[slinky.core.SyntheticEvent[Button.this.type#RefType, org.scalajs.dom.Event] => Unit] = js.undefined,
                  )
  override val component = MaterialUi.Button
}*/







object AppTest {
  @JSExportTopLevel("test")
  def test(username: String): Unit = {
    ReactDOM.render(
      ItemSearch(testItems, onClick = { item:Item => console.log(item) }),
      document.getElementById("react-root")
    )
  }
}
