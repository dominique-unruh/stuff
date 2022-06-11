package de.unruh.stuff

import de.unruh.stuff.editor.ItemEditor
import de.unruh.stuff.shared.{Code, Item, RichText}
import japgolly.scalajs.react.{BackendScope, Callback, CtorType, React, ScalaComponent}
import japgolly.scalajs.react.callback.AsyncCallback
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.{HtmlAttrs, TagMod, VdomElement, all}
import japgolly.scalajs.react.vdom.all.{a, button, className, dangerouslySetInnerHtml, div, h1, h2, href, img, li, onClick, src}

import java.net.URI
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import japgolly.scalajs.react.vdom.Implicits._
import monocle.macros.Lenses



object ItemViewer {
  case class Props(itemId: Item.Id,
                   /** When the user click on a link to another item */
                   onSelectItem: Item.Id => Callback)
  @Lenses case class State(editing: Boolean = false,
/*                          /** Current editor content. Should be [[Item.invalid]] if `editing==false`
                           * and a valid item if `editing==true`. */
                           editItem: Item = Item.invalid*/)

  def apply(props: Props): Unmounted[Props, State, Backend] = Component(props)
  def apply(itemId: Item.Id, onSelectItem: Item.Id => Callback): Unmounted[Props, State, Backend] =
    apply(Props(itemId=itemId, onSelectItem=onSelectItem))

  class Backend(bs: BackendScope[Props, State]) {
    private def url(url: URI) = ExtendedURL.resolve(JSVariables.username, url)

    private def renderBodyView(item: Item)(implicit props: Props): VdomElement = {
      div(className := "item-editor",
        div(all.button("Edit", onClick --> bs.modState(_.copy(editing = true)))),

        div(item.name, className := "item-name"),

        if (item.photos.nonEmpty) {
          val images = for ((photo, i) <- item.photos.zipWithIndex)
            yield (img(src := url(photo)): VdomElement)
          div(className := "item-photos")(images: _*)
        } else
          TagMod.empty,

        // TODO: Add put and remove buttons
        item.location match {
          case Some(location) =>
            div(ItemListItem(location, onClick = props.onSelectItem))
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

    def loadAndRender(implicit props: Props, state: State): AsyncCallback[VdomElement] = {
        for (item <- AsyncCallback.fromFuture(DbCache.getItem(props.itemId)))
          yield if (state.editing)
            ItemEditor(item,
              onSave = { _ => bs.modState(State.editing.replace(false)) },
              onCancel = bs.modState(State.editing.replace(false)))
          else
            renderBodyView(item)
    }

    private def onError(error: Throwable): AsyncCallback[VdomElement] =
      AsyncCallback.delay {
        error.printStackTrace()
        // TODO Nicer formatting
        h1("Failed to load item", className := "item-load-failed")
      }

    def render(implicit props: Props, state: State): VdomElement = {
      React.Suspense(
        fallback = div(className := "item-editor state-waiting", h2("Loading...")), // TODO
        asyncBody = loadAndRender.handleError(onError))
    }
  }

  //noinspection TypeAnnotation
  val Component = ScalaComponent.builder[Props]
    .initialState(State())
    .renderBackend[Backend]
//    .render_PS(render(_,_))
    .build
}
