package de.unruh.stuff

import de.unruh.stuff.shared.Item
import org.scalajs.dom.console
import slinky.core.{Component, StatelessComponent}
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.{className, div, img, key, src}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

@react class ItemListItem extends Component {
  case class Props(itemId: Item.Id, onClick: Item.Id => Unit = { _ => () })
  case class State(
                  /** Item that we have last requested from server */
                    itemId : Item.Id = Item.INVALID_ID,
                  /** Data of the item (as loaded from server or cache) */
                  item: Option[Item] = None)
  override def initialState: State = State()

  private def loadItem() : Unit = {
    if (props.itemId != state.itemId) {
      setState(_.copy(itemId = props.itemId))
      // TODO: also handle failures (user feedback)
      DbCache.getItem(props.itemId).onComplete {
        case Success(item) =>
          if (item.id == props.itemId)
            setState(_.copy(item = Some(item)))
        case Failure(exception) =>
          console.log("Error loading item", exception)
      }
    }
  }

  private def loaded() : Boolean =
    state.item.nonEmpty && state.item.get.id == props.itemId

  override def componentDidMount(): Unit =
    loadItem()

  override def componentDidUpdate(prevProps: Props, prevState: State): Unit =
    loadItem()

  def render(): ReactElement = {
    if (!loaded())
      div(slinky.web.html.onClick := { _: Any => props.onClick(props.itemId) }, className := "item-list-item")
    else {
      val item = state.item.get
      div(slinky.web.html.onClick := { _: Any => props.onClick(item.id) }, className := "item-list-item")(
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
}

@react class ItemList extends Component {
  case class Props(items: Seq[Item.Id], onClick: Item.Id => Unit = { _ => () })
  type State = Unit
  val initialState : State = ()

  override def render(): ReactElement = {
    val listItems : Seq[ReactElement] = props.items.zipWithIndex.map { case (item,idx) => ItemListItem(item,props.onClick) withKey idx.toString }
    div(className := "item-list")(listItems)
  }
}
