package de.unruh.stuff

import de.unruh.stuff.ItemListItem.{Props, loadAndRender, onError, renderBody}
import de.unruh.stuff.shared.Item
import japgolly.scalajs.react.{React, ScalaComponent}
import japgolly.scalajs.react.callback.AsyncCallback
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.{TagMod, VdomElement, all}
import japgolly.scalajs.react.vdom.all.{className, div, h1, h2, href, img, li, onClick, src}
import org.scalajs.dom.console
import slinky.core.facade.ReactElement
//import slinky.core.{Component, WithAttrs}
//import slinky.core.annotations.react
//import slinky.core.facade.ReactElement
//import slinky.web.html.{a, className, div, h1, h2, href, img, key, li, p, src, style, width}

import java.net.URI
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

import japgolly.scalajs.react.vdom.Implicits._

object ItemEditor {
  case class Props(itemId: Item.Id)

  def apply(props: Props): Unmounted[Props, Unit, Unit] = Component(props)
  def apply(itemId: Item.Id): Unmounted[Props, Unit, Unit] = apply(Props(itemId))

//  case class State(itemId: Item.Id, item: Option[Item] = None)
//  override def initialState: State = State(itemId = props.itemId)

/*  // TODO: also react to itemId updates, see ItemListItem
  override def componentDidMount(): Unit =
  // TODO: also handle failures (user feedback)
    DbCache.getItem(state.itemId).onComplete
      { case Success(item) => setState(_.copy(item = Some(item)))
      case Failure(exception) => console.warn(exception) }*/

  def renderBody(item: Item): VdomElement = {
    def url(url: URI) = ExtendedURL.resolve(JSVariables.username, url)

    div(className := "item-editor",
      div(item.name, className := "item-name"),

      if (item.photos.nonEmpty) {
        val images = for ((photo, i) <- item.photos.zipWithIndex)
          yield (img(src := url(photo)): VdomElement)
        div(className := "item-photos")(images :_*)
      } else
        TagMod.empty,

      if (item.description.nonEmpty) {
        div(item.description.asHtml, className := "item-description")
      } else
        TagMod.empty,

      if (item.links.nonEmpty) {
        val links = for (link <- item.links)
          yield li(all.a(href := link.toString)(link.toString))
        div(className := "item-links")(links :_*)
      } else
        TagMod.empty,

      if (item.codes.nonEmpty) {
        val codes = for ((link, i) <- item.codes.zipWithIndex)
          yield li(link.toString)
        div(className := "item-codes")(codes :_*)
      } else
        TagMod.empty,

      div(className := "item-id", item.id.toString),
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
      h1("Failed to load item", className := "item-load-failed")
    }

  private def render(implicit props: Props): VdomElement = {
    React.Suspense(
      fallback = div(className := "item-editor state-waiting", h2("Loading...")), // TODO
      asyncBody = loadAndRender.handleError(onError))
  }

  //noinspection TypeAnnotation
  val Component = ScalaComponent.builder[Props]
    .render_P(render(_))
    .build
}
