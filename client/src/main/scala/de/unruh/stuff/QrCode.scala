package de.unruh.stuff

import org.scalajs.dom.{MediaStreamConstraints, MediaTrackConstraintSet, MediaTrackConstraints, console}
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.ReactElement
import slinky.web.html.{className, div, id, video}

import scala.scalajs.js
import scala.scalajs.js.{UndefOr, |}
import scala.util.Random


@react class QrCode extends Component {
  /** onDetect: (format, content) */
  // TODO class does not reinitialize when MediaTrackConstraints change. Should it?
  case class Props(onDetect: (Option[String], String) => Unit, constraints: MediaTrackConstraints,
                   flashLight: Boolean = false)
  case class State(scanner: zxing.BrowserMultiFormatReader,
                   flashLightState: Boolean = false)
  override def initialState: State = State(scanner = new zxing.BrowserMultiFormatReader())

  private val videoId = s"qrcode-scanner-${Random.nextInt()}"
  override def render(): ReactElement = div(video(id := videoId), className := "qrcode-scanner")

  private def callback(result: zxing.Result, exception: UndefOr[zxing.ZException]) : Unit = {
    if (exception != null && exception.nonEmpty) {
      val exn = exception.get
      if (exn.isInstanceOf[zxing.NotFoundException])
//        console.log("No QR code found...")
        {}
      else
        console.warn(exn, exception)
    } else {
      console.log("Scan success", result, exception)
      props.onDetect(zxing.BarcodeFormat.fromT(result.getBarcodeFormat()).map(_.toString), result.getText())
    }
  }

  /** Changes the flashLight state if necessary */
  private def updateFlashLight(): Unit = {
    if (state.scanner.stream != null && !js.isUndefined(state.scanner.stream))
      if (props.flashLight != state.flashLightState) {
        // We do this first, so that if the following fails (non-asynchronously), we don't keep trying
        setState(_.copy(flashLightState = props.flashLight))
        state.scanner.stream.getVideoTracks()(0).applyConstraints(
          new MediaTrackConstraints {
            advanced = js.Array(js.Dynamic.literal(torch = props.flashLight).asInstanceOf[MediaTrackConstraintSet])
          })
      }
  }

  override def componentDidUpdate(prevProps: Props, prevState: State): Unit = {
    updateFlashLight()
  }

  private def decodingStarted(): Unit = {
    updateFlashLight()
  }

  override def componentDidMount(): Unit = {
    state.scanner.decodeFromConstraints(
      constraints = new MediaStreamConstraints { video = props.constraints },
       videoId, callback _)
      .`then`( { _:Unit => decodingStarted() : Unit | scala.scalajs.js.Thenable[Unit] }, js.undefined)
  }

  override def componentWillUnmount(): Unit = {
    try {
      state.scanner.reset()
    } catch {
      case error: Throwable => console.error("Failed to clear ZXing QR code scanner.", error);
    }
  }
}
