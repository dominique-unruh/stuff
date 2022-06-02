package de.unruh.stuff

import io.kinoplan.scalajs.react.bridge.{ReactBridgeComponent, WithProps, WithPropsNoChildren}
import io.kinoplan.scalajs.react.material.ui.core.{Handler1, MuiDialog, ReactHandler1, ReactHandler2}
import japgolly.scalajs.react.{BackendScope, CtorType, ReactEvent, Ref, ScalaComponent}
import japgolly.scalajs.react.callback.Callback
import japgolly.scalajs.react.component.Scala.{Component, Unmounted}
import japgolly.scalajs.react.vdom.all.{h1, onClick, untypedRef}
import japgolly.scalajs.react.vdom.Implicits._
import japgolly.scalajs.react.vdom.VdomElement
import org.scalajs.dom.{console, html}

import scala.scalajs.js
import scala.scalajs.js.{Thenable, UndefOr, |}
import scala.scalajs.js.annotation.{JSGlobal, JSImport}

trait Webcam extends js.Object {
  def getScreenshot(dimension: UndefOr[Webcam.Dimensions] = js.undefined) : String
}

/** Wrapper for https://github.com/mozmorris/react-webcam */
object Webcam extends ReactBridgeComponent {
  def apply(audio: Boolean,
            screenshotFormat: String): WithPropsNoChildren = autoNoChildren
  @js.native
  @JSImport("react-webcam", JSImport.Namespace) // apparently the module react-webcam does not contain Webcam but *is* Webcam
  private object Webcam extends js.Object
  assert(Webcam != null && !js.isUndefined(Webcam))
  override protected lazy val componentValue: js.Any = Webcam
  trait Dimensions extends js.Object {
    val width : Int
    val height : Int
  }
}

object Camera {
  case class Props(onPhoto: String => Callback,
                   onClose: Callback,
                   open: Boolean)
  type State = Unit

  def apply(props: Props): Unmounted[Props, State, Backend] = Component(props)
  def apply(onPhoto: String => Callback, onClose: Callback, open: Boolean): Unmounted[Props, State, Backend] =
    apply(Props(onPhoto=onPhoto, onClose=onClose, open=open))

  class Backend(bs: BackendScope[Props, State]) {
    private val webcamRef = Ref[Webcam]

    val clickHandler : Callback =
      for (props <- bs.props;
           webcam <- webcamRef.get;
           image = webcam.get.getScreenshot();
           _ <- props.onPhoto(image))
        yield ()

    def render(props: Props): VdomElement = {
      // https://mui.com/material-ui/react-dialog/
      MuiDialog(open = props.open,
        onClose = { (e,s) => props.onClose } : ReactHandler2[ReactEvent, String]) (

        // TODO: ensure that we use the environment-camera
        Webcam(audio=false, screenshotFormat = "image/jpeg")
        (untypedRef := webcamRef.asInstanceOf[Ref.Simple[html.Element]], onClick --> clickHandler)

      )
    }
  }

  //noinspection TypeAnnotation
  val Component = ScalaComponent.builder[Props]
    .stateless
    .renderBackend[Backend]
    .build
}
