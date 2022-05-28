package de.unruh.stuff

import autowire.clientCallable
import de.unruh.stuff.shared.{AjaxApi, Item, SharedMessages}
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode}
import org.scalajs.dom
import org.scalajs.dom.{Event, HTMLInputElement, console, document}
import slinky.core.{Component, CustomAttribute, CustomTag, ExternalComponent, ExternalComponentNoProps, ExternalComponentWithAttributes, ExternalComponentWithRefType, SyntheticEvent, WithAttrs}
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.ReactDOM
import slinky.web.html.{`type`, div, h1, input, onChange, value}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSImport}
import scala.scalajs.js.{UndefOr, |}
import slinky.scalajsreact.Converters._



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
//    val call = AjaxApiClient[AjaxApi].search("").call()
//    call.onComplete(result => console.log(result.toString))
    val root = document.getElementById("react-root")
    val itemSearch : VdomElement = ItemSearch.Component(ItemSearch.Props(onClick = {
      item:Item.Id =>
        ReactDOM.render(ItemEditor(item), root)
    }))

    itemSearch.renderIntoDOM(root)
/*
    ReactDOM.render(
      ItemSearch(onClick = {
        item:Item.Id =>
          ReactDOM.render(ItemEditor(item), root)
      }),
      root)
*/
  }
}
