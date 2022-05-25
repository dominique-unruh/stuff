package de.unruh.stuff

import de.unruh.stuff.shared.Item
import slinky.core.{Component, StatelessComponent}
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.{className, div, img, key, src}

@react class ItemListItem extends StatelessComponent {
  case class Props(item: Item, onClick: Item => Unit = { _ => () })
  def render(): ReactElement = {
    val item = props.item
    div(slinky.web.html.onClick := { _:Any => props.onClick(item) }, className := "item-list-item")(
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

  override def render(): ReactElement = {
    val listItems : Seq[ReactElement] = props.items.zipWithIndex.map { case (item,idx) => ItemListItem(item,props.onClick) withKey idx.toString }
    div(className := "item-list")(listItems)
  }
}
