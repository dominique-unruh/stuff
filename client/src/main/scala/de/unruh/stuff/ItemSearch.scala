package de.unruh.stuff

import autowire.clientCallable
import de.unruh.stuff.materialui.TextField
import de.unruh.stuff.shared.{AjaxApi, Item}
import org.scalajs.dom.{Event, HTMLInputElement, console}
import slinky.core.{Component, SyntheticEvent, TagElement}
import slinky.core.annotations.react
import slinky.core.facade.{React, ReactElement}
import slinky.web.html.{className, div, h1, input, onChange, placeholder}

import scala.collection.mutable.ListBuffer
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

  override def render(): ReactElement = {
    val results : ReactElement =
      if (state.error) {
        // TODO Nicer formatting (e.g., https://mui.com/material-ui/react-alert/)
        h1("Failed to load results")
//      } else if (state.waiting) {
//        // TODO: User overlaid rotating circle, appearing only after 800ms (https://mui.com/material-ui/react-progress/) (Better?: https://mui.com/material-ui/react-backdrop/)
//        h1("Waiting for results")
      } else if (state.results.isEmpty) {
        // TODO Nicer formatting (https://mui.com/material-ui/react-alert/ ?)
        h1("Nothing found")
      } else ItemList(state.results, props.onClick)
    div (className := "item-search") (
      // TODO: Add an X on the right side to clear the content
      input(className := "item-search-input", onChange := changed _, placeholder := "Search..."),
//      TextField(TextField.Props(
//        fullWidth = true, placeholder = "Search", variant = TextField.FILLED,
//        autoFocus = true, onChange = changed _)),
//      s"waiting=${state.waiting}, error=${state.error}, searchString=${state.searchString}",
      results
    )
  }
}
