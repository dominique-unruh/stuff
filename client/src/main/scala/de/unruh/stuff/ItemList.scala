package de.unruh.stuff

import de.unruh.stuff.shared.Item
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.{className, div, img, key, src}

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

@react class ItemList extends Component {
  case class Props(items: Seq[Item], onClick: Item => Unit = { _ => () })
  type State = Unit
  val initialState : State = ()

  override def render(): ReactElement =
    div(className := "item-list")(props.items.map(ItemListItem(_,props.onClick)))
}
