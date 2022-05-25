package de.unruh.stuff

import de.unruh.stuff.Html5Qrcode.{Html5QrcodeError, Html5QrcodeIdentifier, QrDimensionFunction, QrcodeErrorCallback, QrcodeSuccessCallback}
import de.unruh.stuff.QrCode.Box
import org.scalajs.dom
import org.scalajs.dom.{console, document}
import slinky.core.Component
import slinky.core.annotations.react
import slinky.core.facade.{React, ReactElement}
import slinky.web.html.{className, div, id}

import scala.scalajs.js
import scala.scalajs.js.{Promise, UndefOr, typeOf, |}
import scala.scalajs.js.annotation.JSImport
import scala.util.Random

@js.native
private trait CameraDevice extends js.Object {
  val id: CameraDevice.Id
  val label: String
}
private object CameraDevice {
  type Id = String
}

/** Different states of scanner */
private object Html5QrcodeScannerState extends Enumeration(0) {
  type T = Int
  // Indicates the sanning is not running or user is using file based
  // scanning.
  val NOT_STARTED: Value = Value
  // Camera scan is running.
  val SCANNING: Value = Value
  // Camera scan is paused but camera is running.
  val PAUSED: Value = Value
}


/**
 * Interface for configuring {@class Html5Qrcode} class instance.
 */
private trait Html5QrcodeConfigs extends js.Object {
  /**
   * Array of formats to support of type {@type Html5QrcodeSupportedFormats}.
   */
  val formatsToSupport: UndefOr[js.Array[Html5QrcodeSupportedFormats.T]] = js.undefined
}

/** Configuration for creating [[Html5Qrcode]]. */
private trait Html5QrcodeFullConfig extends Html5QrcodeConfigs {
  /**
   * If true, all logs would be printed to console. False by default.
   */
  val verbose: UndefOr[Boolean] = js.undefined
}

private trait QrDimensions extends js.Object {
  val width: Double
  val height: Double
}
private object QrDimensions {
  def apply(w: Double, h: Double): QrDimensions = new QrDimensions {
    override val width: Double = w
    override val height: Double = h
  }
}

private trait Html5QrcodeCameraScanConfig {
/**
 * Optional, Expected framerate of QR code scanning. example { fps: 2 } means the
 * scanning would be done every 500 ms.
 */
val fps: UndefOr[Double] = js.undefined

/**
 * Optional, edge size, dimension or calculator function for QR scanning
 * box, the value or computed value should be smaller than the width and
 * height of the full region.
 *
 * This would make the scanner look like this:
 *          ----------------------
 *          |********************|
 *          |******,,,,,,,,,*****|      <--- shaded region
 *          |******|       |*****|      <--- non shaded region would be
 *          |******|       |*****|          used for QR code scanning.
 *          |******|_______|*****|
 *          |********************|
 *          |********************|
 *          ----------------------
 *
 * Instance of {@interface QrDimensions} can be passed to construct a non
 * square rendering of scanner box. You can also pass in a function of type
 * {@type QrDimensionFunction} that takes in the width and height of the
 * video stream and return QR box size of type {@interface QrDimensions}.
 *
 * If this value is not set, no shaded QR box will be rendered and the scanner
 * will scan the entire area of video stream.
 */
val qrbox: UndefOr[Double | QrDimensions | QrDimensionFunction] = js.undefined

/**
 * Optional, Desired aspect ratio for the video feed. Ideal aspect ratios
 * are 4:3 or 16:9. Passing very wrong aspect ratio could lead to video feed
 * not showing up.
 */
val aspectRatio: UndefOr[Double] = js.undefined

/**
 * Optional, if {@code true} flipped QR Code won't be scanned. Only use this
 * if you are sure the camera cannot give mirrored feed if you are facing
 * performance constraints.
 */
val disableFlip: UndefOr[Boolean] = js.undefined

/*
 * Optional, @beta(this config is not well supported yet).
 *
 * Important: When passed this will override other parameters like
 * 'cameraIdOrConfig' or configurations like 'aspectRatio'.
 * 'videoConstraints' should be of type {@code MediaTrackConstraints} as
 * defined in
 * https://developer.mozilla.org/en-US/docs/Web/API/MediaTrackConstraints
 * and is used to specify a variety of video or camera controls like:
 * aspectRatio, facingMode, frameRate, etc.
 *
 * Note: Facade not implemented on Scala side
 */
val videoConstraints: UndefOr[Nothing /*MediaTrackConstraints*/] = js.undefined
}


@js.native
@JSImport("html5-qrcode", "Html5Qrcode")
private class Html5Qrcode(val elementId: String, config: UndefOr[Html5QrcodeFullConfig]) extends js.Object {
  /**
   * Start scanning QR codes or barcodes for a given camera.
   *
   * @param cameraIdOrConfig Identifier of the camera, it can either be the
   *  camera id retrieved from [[Html5Qrcode#getCameras()]] method or
   *  object with facing mode constraint.
   * @param configuration Extra configurations to tune the code scanner.
   * @param qrCodeSuccessCallback Callback called when an instance of a QR
   * code or any other supported bar code is found.
   * @param qrCodeErrorCallback Callback called in cases where no instance of
   * QR code or any other supported bar code is found.
   */
  def start(cameraIdOrConfig: Html5QrcodeIdentifier,
            configuration: UndefOr[Html5QrcodeCameraScanConfig] = js.undefined,
            qrCodeSuccessCallback: QrcodeSuccessCallback, // The spec says UndefOr[...], but the code fails if the callback is not specified
            qrCodeErrorCallback: UndefOr[QrcodeErrorCallback] = js.undefined,
           ): Promise[Null] = js.native


  /**
   * Pauses the ongoing scan.
   *
   * @param shouldPauseVideo (Optional, default = false) If {@code true} the
   * video will be paused.
   *
   * @throws error if method is called when scanner is not in scanning state.
   */
  def pause(shouldPauseVideo: UndefOr[Boolean] = js.undefined): Unit = js.native

  /**
   * Resumes the paused scan.
   *
   * If the video was previously paused by setting {@code shouldPauseVideo}
   * to {@code true} in {@link Html5Qrcode# pause ( shouldPauseVideo )}, calling
   * this method will resume the video.
   *
   * Note: with this caller will start getting results in success and error
   * callbacks.
   *
   * @throws error if method is called when scanner is not in paused state.
   */

  import de.unruh.stuff.Html5Qrcode

  /**
   * Resumes the paused scan.
   *
   * If the video was previously paused by setting {@code shouldPauseVideo}
   * to {@code true} in {@link Html5Qrcode# pause ( shouldPauseVideo )}, calling
   * this method will resume the video.
   *
   * Note: with this caller will start getting results in success and error
   * callbacks.
   *
   * @throws error if method is called when scanner is not in paused state.
   */
  def resume(): Unit = js.native

  /**
   * Stops streaming QR Code video and scanning.
   */
  def stop(): Promise[Unit] = js.native

  /**
   * Gets state of the camera scan.
   *
   * @return state of type {@enum ScannerState}.
   */
  def getState(): Html5QrcodeScannerState.T = js.native


  /**
   * Clears the existing canvas.
   *
   * Note: in case of ongoing web-cam based scan, it needs to be explicitly
   * closed before calling this method, else it will throw an exception.
   */
  def clear(): Unit = js.native
}



/**
 * Code formats supported by this library.
 */
private object Html5QrcodeSupportedFormats extends Enumeration(0) {
  type T = Int
  val QR_CODE, AZTEC, CODABAR, CODE_39, CODE_93, CODE_128, DATA_MATRIX, MAXICODE, ITF, EAN_13, EAN_8, PDF_417,
  RSS_14, RSS_EXPANDED, UPC_A, UPC_E, UPC_EAN_EXTENSION = Value
}

/** Format of detected code. */
@js.native
private trait QrcodeResultFormat extends js.Object {
  val format: Html5QrcodeSupportedFormats.T;
  val formatName: String;
}

/** Detailed scan result. */
@js.native
private trait QrcodeResult extends js.Object {
  /** Decoded text. */
  val text: String;

  /** Format that was successfully scanned. */
  val format: UndefOr[QrcodeResultFormat]
}

@js.native
private trait Html5QrcodeResult extends js.Object {
  val decodedText: String
  val result: QrcodeResult
}


@js.native
@JSImport("html5-qrcode", "Html5Qrcode")
private object Html5Qrcode extends js.Object {
  def getCameras(): js.Array[CameraDevice] = js.native

  /** Type for a callback for a successful code scan.
   * Arguments: decodedText, result */
  // It is crucial to use the type js.Function2 here. If we use a Scala function type, `typeof f != "function" and Html5Qrcode complains
  type QrcodeSuccessCallback = js.Function2[String, Html5QrcodeResult, Unit]

  type Html5QrcodeError = Any // Didn't find specification

  /** Type for a callback for failure during code scan.
   * Arguments: (errorMessage, error) */
  type QrcodeErrorCallback = js.Function2[String, Html5QrcodeError, Unit]

  type Html5QrcodeIdentifier = CameraDevice.Id | CameraSpec

  type QrDimensionFunction = js.Function2[Double,Double,QrDimensions]
}

private trait CameraSpec extends js.Object {
  val facingMode: UndefOr[String] = js.undefined
}
private object CameraSpec {
  val FACING_USER = "user"
  val FACING_ENVIRONMENT = "environment"
}

@react class QrCode extends Component {
  type Props = QrCode.Config
  type State = Unit
  override val initialState: Unit = ()
  private val config : Html5QrcodeFullConfig = new Html5QrcodeFullConfig {
    override val verbose: UndefOr[Boolean] = props.verbose
  }
  private val scanConfig: Html5QrcodeCameraScanConfig = new Html5QrcodeCameraScanConfig {
    override val aspectRatio: UndefOr[Double] = if (props.aspectRatio == 0) js.undefined else props.aspectRatio
    override val qrbox: UndefOr[Double | QrDimensions | QrDimensionFunction] = props.qrbox match {
      case Box.None => js.undefined
      case Box.Function(function) => { (w:Double,h:Double) => console.log("BLA"); val (w2,h2) = function(w,h); QrDimensions(w2,h2) } : js.Function2[Double,Double,QrDimensions]
      case Box.Edge(length) => length
      case Box.Size(width, height) => QrDimensions(width, height)
    }
  }
  private val camera: Html5QrcodeIdentifier = new CameraSpec {
    override val facingMode: UndefOr[String] = CameraSpec.FACING_ENVIRONMENT
  }

  private val divId = s"qrcode-scanner-${Random.nextInt()}"
  override def render(): ReactElement = div(id := divId, className := "qrcode-scanner")

  private def success(decodedText: String, decodedResult: Html5QrcodeResult) : Unit = {
//    document.write(s"Hello $decodedText ${decodedResult.decodedText} ${decodedResult.result.format}")
    props.onDetect(decodedText, decodedResult.result.format.toOption.map(_.formatName))
    console.log(s"Scan success: $decodedText", decodedResult)
  }

/*  private def error(errorMessage: String, error: Html5QrcodeError) : Unit = {
    console.log(s"Scan failure: $errorMessage", error)
  }*/

  private var scanner : Html5Qrcode = _

  override def componentDidMount(): Unit = {
    scanner = new Html5Qrcode(divId, config)
    val success : js.Function2[String, Html5QrcodeResult, Unit] = this.success
//    val error : js.Function2[String, Html5QrcodeError, Unit] = this.error
    scanner.start(camera, scanConfig, success/*, error*/)
  }
  override def componentWillUnmount(): Unit = {
    try {
      scanner.clear()
    } catch {
      case error: Throwable => console.error("Failed to clear html5QrcodeScanner. ", error);
    }
  }
}

object QrCode {
  sealed trait Box
  object Box {
    final case object None extends Box
    final case class Function(function: (Double,Double) => (Double,Double)) extends Box
    final case class Edge(length: Double) extends Box
    final case class Size(width: Double, height: Double) extends Box
  }
  final case class Config(
                           verbose: Boolean = false,
                           aspectRatio: Double = 0,
                           qrbox: Box = Box.None,
                           onDetect: (String, Option[String]) => Unit = { (_, _) => },
                         )
}