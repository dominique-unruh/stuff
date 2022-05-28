package de.unruh.stuff

import de.unruh.stuff.ItemSearch.{Backend, Props, State, initialState}
import de.unruh.stuff.shared.Item
import japgolly.scalajs.react.callback.AsyncCallback
import japgolly.scalajs.react.{Callback, React, ScalaComponent}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.all.{className, div, h1, img, onClick}
import japgolly.scalajs.react.vdom.{TagMod, VdomElement, VdomNode, all}
import org.scalajs.dom.console
//import slinky.core.{Component, StatelessComponent}
//import slinky.core.annotations.react
//import slinky.core.facade.ReactElement
//import slinky.web.html.{className, div, img, key, src}

import scala.util.{Failure, Success}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import japgolly.scalajs.react.vdom.Implicits._
import slinky.scalajsreact.Converters._

object ItemListItem {
  case class Props(itemId: Item.Id, onClick: Item.Id => Unit = { _ => () })
  /*case class State(
                  /** Item that we have last requested from server */
                    itemId : Item.Id = Item.INVALID_ID,
                  /** Data of the item (as loaded from server or cache) */
                  item: Option[Item] = None)

  class Backend {}

  private val initialState = State()*/

  //noinspection TypeAnnotation
  val Component = ScalaComponent.builder[Props]
//    .initialState(initialState)
    .render_P(render(_))
    .build

  def apply(props: Props): Unmounted[Props, Unit, Unit] = Component(props)
  def apply(itemId: Item.Id, onClick: Item.Id => Unit = { _ => () }): Unmounted[Props, Unit, Unit] = apply(Props(itemId=itemId, onClick=onClick))

  private def onClickHandler(implicit props: Props) = Callback {
    props.onClick(props.itemId)
  }

  private def renderBody(item: Item)(implicit props: Props): VdomElement = {
    div(onClick --> onClickHandler, className := "item-list-item")(
      // Photo (if exists)
      if (item.photos.nonEmpty)
        img(all.src := ExtendedURL.resolve(JSVariables.username, item.photos.head), className := "item-photo")
      else
        div(className := "item-photo-none"),

      // Name
      div(className := "item-name")(item.name),

      // Description (if exists)
      if (item.description.nonEmpty)
        div(className := "item-description")(item.description.asHtml)
      else
        TagMod.empty
    )
  }

  def loadAndRender(implicit props: Props): AsyncCallback[VdomElement] = {
    for (item <- AsyncCallback.fromFuture(DbCache.getItem(props.itemId)))
      yield renderBody(item)
  }

  private def onError(error: Throwable): AsyncCallback[VdomElement] =
    AsyncCallback.delay {
      error.printStackTrace()
      // TODO Nicer formatting
      h1("Failed to load item", className := "search-failed")
    }

  def render(implicit props: Props): VdomElement = {
    React.Suspense(
      fallback = div(onClick --> onClickHandler, all.className := "item-list-item", "LOADING"), // TODO
      asyncBody = loadAndRender.handleError(onError))
  }
}

object ItemList {
  case class Props(items: Seq[Item.Id], onClick: Item.Id => Unit = { _ => () })

  def apply(items: Seq[Item.Id], onClick: Item.Id => Unit = { _ => () }): Unmounted[Props, Unit, Unit] =
    apply(Props(items=items, onClick=onClick))
  def apply(props: Props): Unmounted[Props, Unit, Unit] = Component(props)

  def render(props: Props) : VdomNode = {
    val listItems: Seq[VdomNode] = props.items.zipWithIndex.map {
      case (item, idx) => ItemListItem(item, props.onClick) }
    all.div(all.className := "item-list")(listItems :_*)
  }

  //noinspection TypeAnnotation
  val Component = ScalaComponent.builder[Props]
    .render_P(render)
    .build
}
