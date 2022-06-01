package de.unruh.stuff.reactsimplewysiwyg

import io.kinoplan.scalajs.react.bridge.{ReactBridgeComponent, WithProps}
import japgolly.scalajs.react.ReactEventFrom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object DefaultEditor extends ReactBridgeComponent {
  def apply(value: String,
            onChange: ReactEventFrom[NodeWithValue] => Unit): WithProps = auto

  @JSImport("react-simple-wysiwyg")
  @js.native
  private object DefaultEditor extends js.Object

  override lazy val componentValue: js.Object = DefaultEditor
}
