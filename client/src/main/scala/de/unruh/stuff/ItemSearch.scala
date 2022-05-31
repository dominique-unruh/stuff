package de.unruh.stuff

import autowire.clientCallable
import de.unruh.stuff.shared.{AjaxApi, Code, Item, Utils}
import io.kinoplan.scalajs.react.material.ui.core.MuiInput
import japgolly.scalajs.react.callback.Callback
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.vdom.{VdomElement, VdomNode, all}
import japgolly.scalajs.react.vdom.all.{autoFocus, button, className, div, h1, onChange, onClick, placeholder, value}
import japgolly.scalajs.react.vdom.Implicits._
import japgolly.scalajs.react.{AsyncCallback, BackendScope, CtorType, React, ReactFormEventFromInput, ScalaComponent}
import org.scalajs.dom.{MediaTrackConstraints, console}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

// TODO: Allow to activate/deactivate camera
// TODO: Automatically deactivate camera after inactivity
object ItemSearch {
  case class Props(onClick: Item.Id => Unit = { _ => () })
  case class State(searchString: String = "", flashLight: Boolean = false)

  def apply(props: Props): Unmounted[Props, State, Backend] = Component(props)
  def apply(onClick: Item.Id => Unit = { _ => () }): Unmounted[Props, State, Backend] =
    Component(Props(onClick=onClick))

  class Backend(bs: BackendScope[Props, State]) {
    def loadAndRenderResults(searchString: String)(implicit props: Props) : AsyncCallback[VdomElement] =
      for (results <- AsyncCallback.fromFuture(AjaxApiClient[AjaxApi].search(searchString).call()))
        yield
          renderResults(results)

    def renderResults(results : Seq[Item.Id])(implicit props: Props) : VdomElement = {
      if (results.isEmpty) {
        // TODO Nicer formatting (https://mui.com/material-ui/react-alert/ ?)
        h1("Nothing found", className := "no-search-results")
      } else div(ItemList(results, props.onClick))
    }

    private def changed(event: ReactFormEventFromInput) =
      bs.modState(_.copy(searchString = event.target.value))

    private def qrcode(format: Option[String], text: String) : Unit = {
      console.log(s"qrcode: $text")
      (for (state <- bs.state;
           codeStr = Code(format, text).toString;
           needToAdd = !state.searchString.endsWith(codeStr+" ");
           newSearchString = s"${Utils.addSpaceIfNeeded(state.searchString)}code:$codeStr ";
           _ <- if (needToAdd)
                  bs.setState(state.copy(searchString = newSearchString))
                else
                  DefaultEffects.Sync.empty
           )
        yield {})
        .runNow()
    }

    private val videoConstraints = new MediaTrackConstraints {
      aspectRatio = 1
      facingMode = "environment"
    }

    private def onError(error: Throwable): AsyncCallback[VdomElement] =
      AsyncCallback.delay {
        error.printStackTrace()
        // TODO Nicer formatting (e.g., https://mui.com/material-ui/react-alert/)
        h1("Failed to load results", className := "search-failed")
      }

    def render(implicit props: Props, state: State): VdomNode = {
      div (className := "item-search") (
        QrCode(onDetect = qrcode, constraints = videoConstraints, flashLight = state.flashLight)/*.withRef(qrCodeRef)*/,
        button(onClick --> bs.modState(_.copy(flashLight = true)), "Flashlight"),
        MuiInput(inputProps = js.Dynamic.literal(`type`="search"))
          (className := "item-search-input", onChange ==> changed,
          placeholder := "Search...", autoFocus := true,
          value := state.searchString),
        React.Suspense(
          // TODO: Use React 18 stuff (useTransition/startTransition) to keep the old data in view, only blurred
          fallback = h1("Loading...", className := "state-waiting"),
          asyncBody = loadAndRenderResults(state.searchString).handleError(onError)
        )
      )
    }
  }

  private val initialState = State()

  //noinspection TypeAnnotation
  val Component = ScalaComponent.builder[Props]
    .initialState(initialState)
    .renderBackend[Backend]
    .build
}
