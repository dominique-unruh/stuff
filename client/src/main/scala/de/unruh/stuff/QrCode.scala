package de.unruh.stuff

import de.unruh.stuff.zxing.{BarcodeFormat}
import org.scalajs.dom.{MediaStreamConstraints, MediaTrackConstraintSet, MediaTrackConstraints, console}
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.{React, ReactElement}
import slinky.web.html.{className, div, id, video}

import scala.scalajs.js
import scala.scalajs.js.{UndefOr}
import scala.util.Random


@react class QrCode extends Component {
  /** onDetect: (format, content) */
  case class Props(onDetect: (Option[String], String) => Unit, constraints: MediaTrackConstraints)
  case class State(scanner: zxing.BrowserMultiFormatReader)
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
      props.onDetect(BarcodeFormat.fromT(result.getBarcodeFormat()).map(_.toString), result.getText())
    }
  }

  def setTorch(active: Boolean): Unit = {
    state.scanner.stream.getVideoTracks()(0).applyConstraints(
      new MediaTrackConstraints {
        advanced = js.Array(js.Dynamic.literal(torch = active).asInstanceOf[MediaTrackConstraintSet])
      })
  }


  override def componentDidMount(): Unit = {
    state.scanner.decodeFromConstraints(
      constraints = new MediaStreamConstraints { video = props.constraints },
       videoId, callback _)
  }

  override def componentWillUnmount(): Unit = {
    try {
      state.scanner.reset()
    } catch {
      case error: Throwable => console.error("Failed to clear ZXing QR code scanner.", error);
    }
  }
}
