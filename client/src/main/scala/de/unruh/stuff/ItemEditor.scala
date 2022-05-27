package de.unruh.stuff

import de.unruh.stuff.shared.Item
import org.scalajs.dom.console
import slinky.core.{Component, WithAttrs}
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.{a, className, div, h1, h2, href, img, key, li, p, src, style, width}

import java.net.URI
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

@react class ItemEditor extends Component {
  case class Props(itemId: Item.Id)
  case class State(itemId: Item.Id, item: Option[Item] = None)
  override def initialState: State = State(itemId = props.itemId)

  // TODO: also react to itemId updates, see ItemListItem
  override def componentDidMount(): Unit =
  // TODO: also handle failures (user feedback)
    DbCache.getItem(state.itemId).onComplete
      { case Success(item) => setState(_.copy(item = Some(item)))
      case Failure(exception) => console.warn(exception) }

  override def render(): ReactElement = {
    def url(url: URI) = ExtendedURL.resolve(JSVariables.username, url)

    state.item match {
      case Some(item) =>

        val children = ListBuffer[ReactElement]()

        children.append(div(item.name, className := "item-name", key := "name"))

        if (item.photos.nonEmpty) {
          val images = for ((photo, i) <- item.photos.zipWithIndex)
            yield (img(src := url(photo), key := i.toString): ReactElement)
          children.append(div(className := "item-photos", key := "photos")(images))
        }

        if (item.description.nonEmpty) {
          children.append(div(item.description.asHtml, className := "item-description", key := "description"))
        }

        if (item.links.nonEmpty) {
          val links = for ((link, i) <- item.links.zipWithIndex)
            yield li(a(href := link.toString)(link.toString), key := i.toString)
          children.append(div(className := "item-links", key := "links")(links))
        }

        if (item.codes.nonEmpty) {
          val codes = for ((link, i) <- item.codes.zipWithIndex)
            yield li(link.toString, key := i.toString)
          children.append(div(className := "item-codes", key := "codes")(codes))
        }

        children.append(div(className := "item-id", key := "id")(item.id.toString))

        div(className := "item-editor")(children)
      case None =>
        // TODO prettier
        div(className := "item-editor state-waiting", h2("Loading..."))
    }
  }
}
