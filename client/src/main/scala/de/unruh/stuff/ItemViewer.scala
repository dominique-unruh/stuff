package de.unruh.stuff

import de.unruh.stuff.editor.ItemEditor
import de.unruh.stuff.shared.{Code, Item, RichText}
import japgolly.scalajs.react.{BackendScope, Callback, CtorType, React, ScalaComponent}
import japgolly.scalajs.react.callback.AsyncCallback
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.component.builder.Lifecycle
import japgolly.scalajs.react.vdom.{HtmlAttrs, TagMod, VdomElement, all}
import japgolly.scalajs.react.vdom.all.{a, button, className, dangerouslySetInnerHtml, div, h1, h2, href, img, li, onClick, span, src}

import java.net.URI
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import japgolly.scalajs.react.vdom.Implicits._
import monocle.macros.Lenses



object ItemViewer {
  private type RS = Lifecycle.RenderScope[Props, State, Unit]
  case class Props(itemId: Item.Id,
                   /** When the user click on a link to another item */
                   onSelectItem: Item.Id => Callback)
  @Lenses case class State(editing: Boolean = false,
                           /** To enforce updates if needed */
                           modificationTime: Long = 0,
                          )

  def apply(props: Props): Unmounted[Props, State, Unit] = Component(props)
  def apply(itemId: Item.Id, onSelectItem: Item.Id => Callback): Unmounted[Props, State, Unit] =
    apply(Props(itemId=itemId, onSelectItem=onSelectItem))

  private def url(url: URI) = ExtendedURL.resolve(JSVariables.username, url)

  private def renderBodyView(item: Item)(implicit $: RS): VdomElement = {
    div(className := "item-editor",
      div(all.button("Edit", onClick --> $.modState(_.copy(editing = true)))),

      div(item.name, className := "item-name"),

      if (item.photos.nonEmpty) {
        val images = for ((photo, i) <- item.photos.zipWithIndex)
          yield span(img(src := url(photo)))
        div(className := "item-photos")(images: _*)
      } else
        TagMod.empty,

      // TODO: Add put and remove buttons
      item.location match {
        case Some(location) =>
          div(ItemListItem(location, modificationTime = 0, onClick = $.props.onSelectItem))
        case None => TagMod.empty
      },

      if (item.description.nonEmpty) {
        // TODO: sanitize!?
        div(dangerouslySetInnerHtml := item.description.asHtml, className := "item-description")
      } else
        TagMod.empty,

      if (item.codes.nonEmpty) {
        val codes = item.codes.map(CodeButton(_, link=true) : VdomElement)
        div(className := "item-codes")(codes: _*)
      } else
        TagMod.empty,

      div(className := "item-id", item.id.toString),
    )
  }

  def loadAndRender(implicit $: RS): AsyncCallback[VdomElement] = {
    for (item <- AsyncCallback.fromFuture(DbCache.getItem($.props.itemId, $.state.modificationTime)))
      yield if ($.state.editing)
        ItemEditor(item,
          onSave = { _ => $.modState(State.editing.replace(false)) },
          onCancel = $.modState(State.editing.replace(false)))
      else
        renderBodyView(item)
  }

  private def onError(error: Throwable): AsyncCallback[VdomElement] =
    AsyncCallback.delay {
      error.printStackTrace()
      // TODO Nicer formatting
      h1("Failed to load item", className := "item-load-failed")
    }

  def render(implicit $: RS): VdomElement = {
    React.Suspense(
      fallback = div(className := "item-editor state-waiting", h2("Loading...")), // TODO
      asyncBody = loadAndRender.handleError(onError))
  }

  //noinspection TypeAnnotation
  val Component = ScalaComponent.builder[Props]
    .initialState(State())
    .render(render(_))
    .build
}
