package de.unruh.stuff

import de.unruh.stuff.reactsimplewysiwyg.DefaultEditor
import io.kinoplan.scalajs.react.bridge.{ReactBridgeComponent, WithProps}
import japgolly.scalajs.react.vdom.all.div
import japgolly.scalajs.react.{BackendScope, ReactEvent, ReactEventFrom, ReactEventFromInput, ScalaComponent}
import org.scalajs.dom.{Node, console}
import org.scalajs.dom.html.Div

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport




// TODO remove
object HTMLViewer {
  class Backend(bs: BackendScope[Unit, String]) {
    def render(state: String) = {
      console.log("State",state)
      div(
        DefaultEditor(value=state, onChange={ event =>
          console.log(event.target.value)
          bs.setState(event.target.value).runNow()
        })
      )
    }
  }

  val Component = ScalaComponent.builder[Unit]
    .initialState("hello")
    .renderBackend[Backend]
    .build

  def apply() = Component.apply()
}
