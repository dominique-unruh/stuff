package de.unruh.stuff

import de.unruh.stuff.shared.Item
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.div

import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import scala.scalajs.js.UndefOr

@react class ItemListItem extends Component {
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

    ListItemButton() (children)
  }
}

@react class ItemList extends Component {
  case class Props(items: Seq[Item])
  type State = Unit
  val initialState = ()

  override def render(): ReactElement =
    List() (props.items.map(ItemListItem.apply))
}
