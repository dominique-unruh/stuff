package de.unruh.stuff.reactwebcam

import io.kinoplan.scalajs.react.bridge.{ReactBridgeComponent, WithPropsNoChildren}
import org.scalajs.dom.{DOMException, MediaTrackConstraints}

import scala.scalajs.js
import scala.scalajs.js.{UndefOr, |}
import scala.scalajs.js.annotation.JSImport

trait Webcam extends js.Object {
  def getScreenshot(dimension: UndefOr[Webcam.Dimensions] = js.undefined): String
}

/** Wrapper for https://github.com/mozmorris/react-webcam */
object Webcam extends ReactBridgeComponent {
  def apply(audio: UndefOr[Boolean] = js.undefined,
            videoConstraints: UndefOr[MediaTrackConstraints] = js.undefined,
            onUserMediaError: UndefOr[js.Function1[String | DOMException, Unit]],
            screenshotFormat: UndefOr[String] = js.undefined): WithPropsNoChildren = autoNoChildren

  @js.native
  @JSImport("react-webcam", JSImport.Namespace) // apparently the module react-webcam does not contain Webcam but *is* Webcam
  private object Webcam extends js.Object

  assert(Webcam != null && !js.isUndefined(Webcam))
  override protected lazy val componentValue: js.Any = Webcam

  trait Dimensions extends js.Object {
    val width: Int
    val height: Int
  }
}