package de.unruh.stuff.db

import de.unruh.stuff.{Config, ExtendedURL, Paths}
import de.unruh.stuff.shared.Item
import io.lemonlabs.uri.DataUrl
import org.apache.tika.mime.{MimeTypeException, MimeTypes}
import play.twirl.api

import java.net.{URI, URL}
import java.nio.file.{FileAlreadyExistsException, Files, StandardOpenOption}
import java.text.SimpleDateFormat
import java.time.{Instant, ZoneId}
import java.util.concurrent.atomic.AtomicInteger
import java.util.{Calendar, TimeZone}

object ProcessItems {
  private val mimeTypes = MimeTypes.getDefaultMimeTypes

  /** Returns the preferred file extension for a given mime-type (incl. dot).
   * `.data` if unknown */
  def extensionForMimetype(mimeType: String): String = {
    try {
      val extension = mimeTypes.forName(mimeType).getExtension
      if (extension.isEmpty) ".data" else extension
    } catch {
      case _ : MimeTypeException => ".data"
    }
  }

  private val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
  private val dateFormat = new SimpleDateFormat("yyyymmdd_hhmmss")
  private val counter = new AtomicInteger()
  def dataToLocalUrl(username: String, prefix: String = "", itemId: Item.Id, url: URI)(implicit config: Config): URI = url.getScheme match {
    case "data" =>
      val dataUrl = DataUrl.parse(url.toString)
      val extension = extensionForMimetype(dataUrl.mediaType.value)
      val filename = s"$prefix${dateFormat.format(calendar.getTime)}_${counter.incrementAndGet}$extension"
      val path = Paths.filePath(username, itemId, filename)
      assert(!Files.exists(path))
      val localUrl = ExtendedURL.forFile(itemId, filename)
      try Files.createDirectory(path.getParent)
      catch { case _ : FileAlreadyExistsException => }
      Files.write(path, dataUrl.data, StandardOpenOption.CREATE_NEW)
      localUrl
    case _ => url
  }

  def processItem(username: String, item: Item)(implicit config: Config): Item = {
    item.copy(photos = item.photos.map(dataToLocalUrl(username, "photo-", item.id, _)))
      .updateLastModified
  }
}
