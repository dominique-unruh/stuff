package de.unruh.stuff.notistack

import de.unruh.stuff.notistack.VariantType.T
import io.kinoplan.scalajs.react.bridge.{ReactBridgeComponent, WithProps}

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSImport

//noinspection TypeAnnotation
object VariantType {
  type T = String
  def default = "default"
  def error = "error"
  def success = "success"
  def warning = "warning"
  def info = "info"
}

trait OptionsObject extends js.Object {
  val variant: UndefOr[VariantType.T]
}
object OptionsObject {
  def apply(variant: UndefOr[VariantType.T]): OptionsObject = {
    val _variant = variant
    new OptionsObject {
      override val variant: UndefOr[T] = _variant
    }
  }
}

trait SnackbarProvider extends js.Object {
  def handleEnqueueSnackbar(message: String, options: UndefOr[OptionsObject]): Unit
}

object SnackbarProvider extends ReactBridgeComponent {
  def apply(): WithProps = auto

//  override protected lazy val componentNamespace: String = "notistack"
  @js.native
  @JSImport("notistack")
  private object SnackbarProvider extends js.Object

  override protected lazy val componentValue: js.Any = SnackbarProvider
}
