package de.unruh.stuff

import autowire.clientCallable
import de.unruh.stuff.shared.{AjaxApi, Code, Item, Utils}
import io.kinoplan.scalajs.react.material.ui.core.MuiInput
import japgolly.scalajs.react.callback.Callback
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.util.DefaultEffects
import japgolly.scalajs.react.vdom.{TagMod, VdomElement, VdomNode, all}
import japgolly.scalajs.react.vdom.all.{autoFocus, button, className, div, h1, onChange, onClick, placeholder, value}
import japgolly.scalajs.react.vdom.Implicits._
import japgolly.scalajs.react.{AsyncCallback, BackendScope, CtorType, React, ReactFormEventFromInput, ScalaComponent}
import org.log4s
import org.scalajs.dom.{MediaTrackConstraints, console}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

// TODO: Allow to activate/deactivate camera
// TODO: Automatically deactivate camera after inactivity
object ItemSearch {
  /** How many results to load */
  val numResults = 100

  case class Props(onClick: Item.Id => Callback, onCreate: Option[Code] => Callback, visible: Boolean)
  case class State(
                  /** User input search string */
                    searchString: String = "",
                  /** User input: flashlight on? */
                  flashLight: Boolean = false,
                  /** Current search initiated by QR code reader? `None` means no, `Some(code)` means yes. */
                  searchFromCode: Option[Code] = None,
                  )

  val videoConstraints: MediaTrackConstraints = new MediaTrackConstraints {
    aspectRatio = 1
    facingMode = "environment"
  }

  def apply(props: Props): Unmounted[Props, State, Backend] = Component(props)
  def apply(onSelectItem: Item.Id => Callback, onCreate: Option[Code] => Callback, visible: Boolean): Unmounted[Props, State, Backend] =
    Component(Props(onClick=onSelectItem, onCreate = onCreate, visible = visible))

  class Backend(bs: BackendScope[Props, State]) {
    def loadAndRenderResults(searchString: String)(implicit props: Props, state: State) : AsyncCallback[VdomElement] =
      for (results <- AsyncCallback.fromFuture(AjaxApiClient[AjaxApi].search(searchString, numResults).call()))
        yield
          renderResults(results)

    def renderResults(results : Seq[Item.Id])(implicit props: Props, state: State) : VdomElement = {
      if (results.isEmpty) {
        div(
          // TODO Nicer formatting (https://mui.com/material-ui/react-alert/ ?)
          h1("Nothing found", className := "no-search-results"),
          state.searchFromCode match {
            case Some(code) =>
              div(button(s"Create new item with code $code?", onClick --> props.onCreate(Some(code))))
            case None => TagMod.empty
          })
      } else div(ItemList(results, props.onClick))
    }

    private def changed(event: ReactFormEventFromInput) =
      bs.modState(_.copy(searchString = event.target.value, searchFromCode = None))

    private def qrcode(format: Option[String], text: String) : Callback =
      for (state <- bs.state;
            _ = logger.debug(s"qrcode: $text");
            code = Code(format, text);
            codeStr = code.toString;
            needToAdd = !state.searchString.endsWith(codeStr+" ");
            newSearchString = s"${Utils.addSpaceIfNeeded(state.searchString)}code:$codeStr ";
            _ <- if (needToAdd)
              bs.setState(state.copy(searchString = newSearchString, searchFromCode = Some(code)))
            else
              DefaultEffects.Sync.empty
            )
      yield {}

    private def onError(error: Throwable): AsyncCallback[VdomElement] =
      AsyncCallback.delay {
        error.printStackTrace()
        // TODO Nicer formatting (e.g., https://mui.com/material-ui/react-alert/)
        h1("Failed to load results", className := "search-failed")
      }

    def render(implicit props: Props, state: State): VdomNode = {
      div (className := "item-search") (
        QrCode(onDetect = qrcode, constraints = videoConstraints, flashLight = state.flashLight, active = props.visible)/*.withRef(qrCodeRef)*/,
        div(button(onClick --> bs.modState(_.copy(flashLight = true)), "Flashlight"), " ",
          button(onClick --> props.onCreate(None), "New")),
        MuiInput(inputProps = js.Dynamic.literal(`type`="search"))
          (className := "item-search-input", onChange ==> changed,
          placeholder := "Search...", autoFocus := true,
          value := state.searchString),
        React.Suspense(
          // TODO: Use React 18 stuff (useTransition/startTransition) to keep the old data in view, only blurred
          fallback = h1("Loading...", className := "state-waiting"),
          asyncBody = loadAndRenderResults(state.searchString).handleError(onError)
        ),
      )
    }
  }

  private val logger = log4s.getLogger

  private val initialState = State()

  //noinspection TypeAnnotation
  val Component = ScalaComponent.builder[Props]
    .initialState(initialState)
    .renderBackend[Backend]
    .build
}
