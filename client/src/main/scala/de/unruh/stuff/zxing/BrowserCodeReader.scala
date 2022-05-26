package de.unruh.stuff.zxing

import org.scalajs.dom.{HTMLVideoElement, MediaStream, MediaStreamConstraints}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.{UndefOr, |}

@js.native @JSImport("@zxing/library")
class BrowserCodeReader() extends js.Object {

  /**
   * The stream output from camera.
   *
   * Note: The original TypeScript code marks this as a `protected var`. We change it to a public `val` to make it possible to modify the stream settings.
   */
  val stream: MediaStream = js.native;

  /**
   * Continuously tries to decode the barcode from the device specified by device while showing the video in the specified video element.
   *
   * @param {string|null} [deviceId] the id of one of the devices obtained after calling getVideoInputDevices. Can be undefined, in this case it will decode from one of the available devices, preffering the main camera (environment facing) if available.
   * @param {string|HTMLVideoElement|null} [video] the video element in page where to show the video while decoding. Can be either an element id or directly an HTMLVideoElement. Can be undefined, in which case no video will be shown.
   * @returns {Promise<void>}
   * @memberOf BrowserCodeReader
   */
  def decodeFromVideoDevice(deviceId: UndefOr[String], videoSource: UndefOr[String | HTMLVideoElement], callbackFn: DecodeContinuouslyCallback): js.Promise[Unit] = js.native

  /**
   * Continuously tries to decode the barcode from a stream obtained from the given constraints while showing the video in the specified video element.
   *
   * @param {MediaStream} [constraints] the media stream constraints to get s valid media stream to decode from
   * @param {string|HTMLVideoElement} [video] the video element in page where to show the video while decoding. Can be either an element id or directly an HTMLVideoElement. Can be undefined, in which case no video will be shown.
   * @returns {Promise<Result>} The decoding result.
   *
   * @memberOf BrowserCodeReader
   */
  def decodeFromConstraints(constraints: MediaStreamConstraints, videoSource: UndefOr[String | HTMLVideoElement], callbackFn: DecodeContinuouslyCallback): js.Promise[Unit] = js.native

  /**
   * Resets the code reader to the initial state. Cancels any ongoing barcode scanning from video or camera.
   *
   * @memberOf BrowserCodeReader
   */
  def reset(): Unit = js.native
}
