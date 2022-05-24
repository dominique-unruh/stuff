package de.unruh.stuff.materialui

import org.scalajs.dom
import org.scalajs.dom.Event
import slinky.core.{ExternalComponentWithRefType, SyntheticEvent}

import scala.scalajs.js
import scala.scalajs.js.{UndefOr, |}

object TextField extends ExternalComponentWithRefType[dom.HTMLInputElement] {
  case class Props(fullWidth: UndefOr[Boolean] = js.undefined,
                   placeholder: UndefOr[String] = js.undefined,
                   variant: UndefOr[String] = js.undefined,
                   autoFocus: UndefOr[Boolean] = js.undefined,
                   onChange: UndefOr[SyntheticEvent[TextField.this.type#RefType, Event] => Unit] = js.undefined)

  val FILLED = "filled"
  val OUTLINED = "outlined"

  override val component: String | js.Object = MaterialUi.TextField
}
