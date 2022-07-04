package de.unruh.stuff

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter

import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import scala.util.Random

object QrSheet {
  // TODO: Add a validation function for size <= maxSize, count <= maxCount
  case class SheetOptions(template: Option[String] = None, count: Option[Int] = None, size: Option[Int] = None) {
    def templateDefault: String = template.getOrElse("XXX")
    def sizeDefault: Int = size.getOrElse(150)
    def countDefault: Int = count.getOrElse(200)
  }

//  case class ImageOptions(content: String, size: Int)

  def instantiateTemplate(template: String): String = {
    val numChars = 6
    val str = for (i <- 1 to numChars)
      yield Random.nextPrintableChar()
    template.replace("XXX", str.mkString)
  }

  def createQrCode(content: String, size: Int): Array[Byte] = {
    assert(size <= QrSheet.maxSize)
    val barcodeWriter = new QRCodeWriter()
    val bitMatrix = barcodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size)
    val image = MatrixToImageWriter.toBufferedImage(bitMatrix)
    val stream = new ByteArrayOutputStream()
    ImageIO.write(image, "png", stream)
    stream.toByteArray
  }

  val maxSize = 1000
  val maxCount = 1000
}
