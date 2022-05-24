package de.unruh.stuff.materialui

import org.scalajs.dom.Event
import slinky.core.facade.ReactElement
import slinky.core.{ExternalComponent, ExternalComponentNoProps, SyntheticEvent}

import scala.scalajs.js
import scala.scalajs.js.{UndefOr, |}

object List extends ExternalComponentNoProps {
  override val component: String | js.Object = MaterialUi.List
}

object ListItemButton extends ExternalComponent {
  case class Props(onClick: UndefOr[SyntheticEvent[ListItemButton.this.type#RefType, Event] => Unit])
  override val component: String | js.Object = MaterialUi.ListItemButton
}

object ListItemIcon extends ExternalComponentNoProps {
  override val component: String | js.Object = MaterialUi.ListItemIcon
}

object ListItemAvatar extends ExternalComponentNoProps {
  override val component: String | js.Object = MaterialUi.ListItemAvatar
}

object ListItemText extends ExternalComponent {
  case class Props(primary: UndefOr[ReactElement] = js.undefined, secondary: UndefOr[ReactElement] = js.undefined)
  override val component: String | js.Object = MaterialUi.ListItemText
}
