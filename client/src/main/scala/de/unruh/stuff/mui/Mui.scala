package de.unruh.stuff.mui

import io.kinoplan.scalajs.react.bridge.{ReactBridgeComponent, WithProps}
import japgolly.scalajs.react.{Callback, ReactEvent}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/** Facade for MUI's `Dialog` (https://mui.com/material-ui/api/dialog/) */
object MuiDialog extends ReactBridgeComponent {
  /** @param onClose called with the event and the reason (`"backdropClick"` or `"escapeKeyDown"`) */
  def apply(open: Boolean,
            onClose: js.UndefOr[(ReactEvent, String) => Callback] = js.undefined): WithProps = auto

  @JSImport("@mui/material", "Dialog")
  @js.native
  private object Dialog extends js.Object

  override protected lazy val componentValue: js.Any = Dialog
}

/** Facade for MUI's `Input` (https://mui.com/material-ui/api/input/).
 * Further attributes (`value`, `onChange`, ...) are passed as tag mods. */
object MuiInput extends ReactBridgeComponent {
  /** @param inputProps attributes applied to the underlying `input` element */
  def apply(inputProps: js.UndefOr[js.Object] = js.undefined): WithProps = auto

  @JSImport("@mui/material", "Input")
  @js.native
  private object Input extends js.Object

  override protected lazy val componentValue: js.Any = Input
}
