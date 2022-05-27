package de.unruh.stuff

import autowire.clientCallable
import de.unruh.stuff.shared.{AjaxApi, Code, Item, Utils}
import org.scalajs.dom.window.alert
import org.scalajs.dom.{Event, MediaTrackConstraints, console}
import slinky.core.{Component, SyntheticEvent}
import slinky.core.annotations.react
import slinky.core.facade.{React, ReactElement, ReactRef}
import slinky.web.html.{autoFocus, button, className, div, h1, input, onChange, onClick, placeholder, value}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.{Failure, Success}

@react class ItemSearch extends Component {
  case class Props(onClick: Item => Unit = { _ => () })
  case class State(searchString: String, waiting: Boolean, error: Boolean, results: Seq[Item])
  override def initialState : State = State("", waiting = false, error = false, Nil)

  override def componentDidMount(): Unit = {
    super.componentDidMount()
    doSearch("")
  }

  private def doSearch(searchString: String): Unit = {
    setState(state.copy(searchString = searchString, waiting = true, error = false))
    val future = AjaxApiClient[AjaxApi].search(searchString).call()
    future.onComplete { result =>
      setState { state =>
        if (state.searchString == searchString) { // otherwise we got a response for an outdated search
          result match {
            case Failure(exception) =>
              console.log("Failed to load search results: ", exception)
              state.copy(waiting = false, error = true)
            case Success(results) =>
              state.copy(waiting = false, error = false, results = results)
          }
        } else
          state // do nothing if these were outdated search results
      }
    }
  }

  private def changed(event:  SyntheticEvent[input.tagType#RefType, Event]) : Unit = {
    doSearch(event.target.value)
  }

  private def qrcode(format: Option[String], text: String): Unit = {
    val code = Code(format, text)
    doSearch(s"${Utils.addSpaceIfNeeded(state.searchString)}code:$code ")
  }

  private val videoConstraints = new MediaTrackConstraints {
    aspectRatio = 1
    facingMode = "environment"
  }

  val qrCodeRef: ReactRef[QrCode] = React.createRef[QrCode]

  override def render(): ReactElement = {
    val results : ReactElement =
      if (state.error) {
        // TODO Nicer formatting (e.g., https://mui.com/material-ui/react-alert/)
        h1("Failed to load results", className := "search-failed")
      } else if (state.results.isEmpty) {
        // TODO Nicer formatting (https://mui.com/material-ui/react-alert/ ?)
        h1("Nothing found", className := "no-search-results")
      } else ItemList(state.results, props.onClick)

    div (className := Utils.joinClasses("item-search", if (state.waiting) "state-waiting" else null)) (
      QrCode(onDetect = qrcode, constraints = videoConstraints).withRef(qrCodeRef),
      button("Flashlight", onClick := { _ => qrCodeRef.current.setTorch(true) }),
      // TODO: Add an X on the right side to clear the content
      input(className := "item-search-input", onChange := changed _, placeholder := "Search...", autoFocus := true, value := state.searchString),
      results
    )
  }
}
