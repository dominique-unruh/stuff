package de.unruh.stuff

import de.unruh.stuff.materialui.{Avatar, Icon, IconsMaterial, ListItemAvatar, ListItemButton, ListItemIcon, ListItemText}
import de.unruh.stuff.shared.Item
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.{className, div, img, key, src}

import scala.collection.mutable.ListBuffer
import scala.scalajs.js
import scala.scalajs.js.UndefOr

object ItemListItem {
  def apply(item: Item, onClick: Item => Unit = { _ => () }): ReactElement = {
    div(slinky.web.html.onClick := { _:Any => onClick(item) }, className := "item-list-item")(
      // Photo (if exists)
      if (item.photos.nonEmpty)
        img(src := ExtendedURL.resolve(JSVariables.username, item.photos.head), key := "photo", className := "item-photo")
      else
        div(className := "item-photo-none", key := "photo"),

        // Name
        div(className := "item-name", key := "name")(item.name),

        // Description (if exists)
        if (item.description.nonEmpty)
          div(className := "item-description", key := "description")(item.description.asHtml)
        else
          null,
    )
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
    div(className := "item-list")(props.items.map(ItemListItem(_,props.onClick)))
}
