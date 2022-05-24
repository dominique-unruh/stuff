package de.unruh.stuff

import de.unruh.stuff.materialui.{Avatar, Icon, IconsMaterial, ListItemAvatar, ListItemButton, ListItemIcon, ListItemText}
import de.unruh.stuff.shared.Item
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.div

import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import scala.scalajs.js.UndefOr

object ItemListItem {
  def apply(item: Item, onClick: Item => Unit = { _ => () }): ReactElement = {
    val children = ListBuffer[ReactElement]()
    if (item.photos.nonEmpty)
      children.append(ListItemAvatar() (Avatar(Avatar.Props(src = item.photos.head.toString, variant = Avatar.SQUARE))) withKey "pic")
    else
      children.append(ListItemIcon() (Icon(IconsMaterial.IceSkating)()) withKey "pic")

    val secondary : UndefOr[String] = if (item.description.nonEmpty)
      item.description.asHtml
    else
      js.undefined

    children.append(ListItemText(ListItemText.Props(primary = item.name : ReactElement, secondary = secondary)) withKey "text")

    ListItemButton(ListItemButton.Props(onClick = { _:Any => onClick(item) })) (children) withKey item.id.toString
  }
}

/*@react class ItemListItem extends Component {
  type Props = Item
  type State = Unit
  override def initialState: Unit = ()

  override def render(): ReactElement = {
    val children = ListBuffer[ReactElement]()
    val item = props
    if (item.photos.nonEmpty)
      children.append(ListItemAvatar() (Avatar(Avatar.Props(src = item.photos.head.toString, variant = Avatar.SQUARE))))
    else
      children.append(ListItemIcon() (Icon(IconsMaterial.IceSkating)()))

    val secondary : UndefOr[String] = if (item.description.nonEmpty)
      item.description.asHtml
    else
      js.undefined

    children.append(ListItemText(ListItemText.Props(primary = item.name : ReactElement, secondary = secondary)))

    ListItemButton() (children) withKey (item.id.toString)
  }
}*/

@react class ItemList extends Component {
  case class Props(items: Seq[Item], onClick: Item => Unit = { _ => () })
  type State = Unit
  val initialState : State = ()

  override def render(): ReactElement =
    materialui.List() (props.items.map(ItemListItem(_,props.onClick)))
}
