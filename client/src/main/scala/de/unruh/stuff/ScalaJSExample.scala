package de.unruh.stuff

import de.unruh.stuff.shared.SharedMessages
import org.scalajs.dom
import org.scalajs.dom.{console, document}
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.ReactDOM
import slinky.web.html.{`type`, div, h1, input, onChange, value}

import scala.scalajs.js.annotation.JSExportTopLevel

@react class TestComponent extends Component {
  case class Props(message: String)
  case class State(text: String)

  override def initialState: State = State("")

  override def render(): ReactElement = div (
    h1(props.message + " " + state.text),
    input (
      `type` := "text",
      value := state.text,
      onChange := { e => setState(state.copy(e.target.value)); console.log(e.target.value) }
    )
  )
}

object ScalaJSExample {
  @JSExportTopLevel("test")
  def test(username: String): Unit = {
    ReactDOM.render(
      TestComponent(s"Hello ${username}!"),
      document.getElementById("react-root")
    )
  }
}
