package de.unruh.stuff

import de.unruh.stuff.shared.Item
import japgolly.scalajs.react.callback.AsyncCallback
import japgolly.scalajs.react.{Callback, React, ScalaComponent}
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.all.{className, dangerouslySetInnerHtml, div, h1, img, onClick}
import japgolly.scalajs.react.vdom.{HtmlAttrs, TagMod, VdomElement, VdomNode, all}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import japgolly.scalajs.react.vdom.Implicits._

object ItemListItem {
  case class Props(itemId: Item.Id,
                   /** Requires that data at least as new as `modificationTime` is loaded from server. */
                   modificationTime: Long,
                   onClick: Item.Id => Callback)

  //noinspection TypeAnnotation
  val Component = ScalaComponent.builder[Props]
    .render_P(render(_))
    .build

  def apply(props: Props): Unmounted[Props, Unit, Unit] = Component(props)
  def apply(itemId: Item.Id, modificationTime: Long, onClick: Item.Id => Callback): Unmounted[Props, Unit, Unit] =
    apply(Props(itemId=itemId, modificationTime=modificationTime, onClick=onClick))

  private def renderBody(item: Item)(implicit props: Props): VdomElement = {
    div(onClick --> props.onClick(props.itemId), className := "item-list-item")(
      // Photo (if exists)
      if (item.photos.nonEmpty)
        img(all.src := ExtendedURL.resolve(JSVariables.username, item.photos.head), className := "item-photo")
      else
        div(className := "item-photo-none"),

      // Name
      div(className := "item-name")(item.name),

      // Description (if exists)
      if (item.description.nonEmpty)
      // TODO: sanitize!?
        div(dangerouslySetInnerHtml := item.description.asHtml, className := "item-description")
      else
        TagMod.empty
    )
  }

  def loadAndRender(implicit props: Props): AsyncCallback[VdomElement] = {
    for (item <- AsyncCallback.fromFuture(DbCache.getItem(props.itemId, props.modificationTime)))
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
      fallback = div(onClick --> props.onClick(props.itemId), className := "item-list-item", "LOADING"), // TODO
      asyncBody = loadAndRender.handleError(onError))
  }
}

object ItemList {
  case class Props(items: Seq[(Item.Id, Long)], onClick: Item.Id => Callback)

  def apply(items: Seq[(Item.Id, Long)], onClick: Item.Id => Callback): Unmounted[Props, Unit, Unit] =
    apply(Props(items=items, onClick=onClick))
  def apply(props: Props): Unmounted[Props, Unit, Unit] = Component(props)

  def render(props: Props) : VdomNode = {
    val listItems: Seq[VdomNode] = props.items.map { case (id,time) => ItemListItem(id, time, props.onClick) }
    all.div(all.className := "item-list")(listItems :_*)
  }

  //noinspection TypeAnnotation
  val Component = ScalaComponent.builder[Props]
    .render_P(render)
    .build
}
