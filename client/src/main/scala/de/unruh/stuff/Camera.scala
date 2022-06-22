package de.unruh.stuff

import de.unruh.stuff.reactwebcam.Webcam
import io.kinoplan.scalajs.react.bridge.{ReactBridgeComponent, WithProps, WithPropsNoChildren}
import io.kinoplan.scalajs.react.material.ui.core.{Handler1, MuiDialog, ReactHandler1, ReactHandler2}
import japgolly.scalajs.react.{BackendScope, CtorType, ReactEvent, Ref, ScalaComponent}
import japgolly.scalajs.react.callback.{AsyncCallback, Callback}
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.all.{h1, onClick, untypedRef}
import japgolly.scalajs.react.vdom.Implicits._
import japgolly.scalajs.react.vdom.VdomElement
import org.scalajs.dom.{MediaStreamConstraints, MediaTrackConstraints, console, html}

import scala.scalajs.js
import scala.scalajs.js.{Thenable, UndefOr, |}
import scala.scalajs.js.annotation.{JSGlobal, JSImport}




// TODO: If opening camera fails, report to user
object Camera {
  case class Props(onPhoto: String => AsyncCallback[Unit])
  type State = Unit

  def apply(props: Props): Unmounted[Props, State, Backend] = Component(props)
  def apply(onPhoto: String => AsyncCallback[Unit]): Unmounted[Props, State, Backend] =
    apply(Props(onPhoto=onPhoto))

  class Backend(bs: BackendScope[Props, State]) {
    private val webcamRef = Ref[Webcam]

    val clickHandler : AsyncCallback[Unit] =
      for (props <- bs.props.asAsyncCallback;
           webcam <- webcamRef.get.asAsyncCallback;
           image = webcam.get.getScreenshot();
           _ <- props.onPhoto(image))
        yield ()

    def render(props: Props): VdomElement = (
      // https://mui.com/material-ui/react-dialog/
        Webcam(audio=false, screenshotFormat = "image/jpeg",
          videoConstraints = new MediaTrackConstraints { facingMode = "environment"; aspectRatio = 1 })
        (untypedRef := webcamRef.asInstanceOf[Ref.Simple[html.Element]], // Not sure how to do this without this untrue cast...
          onClick --> clickHandler)
      )
  }

  //noinspection TypeAnnotation
  val Component = ScalaComponent.builder[Props]
    .stateless
    .renderBackend[Backend]
    .build
}
