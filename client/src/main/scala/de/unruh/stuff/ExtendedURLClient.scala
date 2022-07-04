package de.unruh.stuff

import de.unruh.stuff.shared.Utils
import org.log4s

import java.net.URI

object ExtendedURLClient {
  /** Note: only converts extended item URLs. (Extended file URLs should not appear in item description.) */
  def externalize(username: String, url: URI): URI = {
    logger.debug(url.toString)
    AppMain.urlToPage(url.toString) match {
      case Some(AppMain.ItemView(id)) => ExtendedURL.forItem(id)
      case _ => url
    }
  }

  private val logger = log4s.getLogger

  def resolve(username: String, url: URI): URI = {
    if (url.getScheme == "localstuff") {
      url.getRawSchemeSpecificPart match {
        case ExtendedURL.fileSchemaSpecificRegex(id, file) =>
          new URI(null, null, s"/files/${Utils.encodeURIComponent(username)}/${id}/${file}", null)
        case ExtendedURL.itemSchemaSpecificRegex(id) =>
          AppMain.pageToUrl(AppMain.ItemView(id.toLong))
        case _ => throw new AssertionError(url.toString)
      }
    } else
      url
  }


}
