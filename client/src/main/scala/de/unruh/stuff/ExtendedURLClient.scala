package de.unruh.stuff

import de.unruh.stuff.shared.Utils

import java.net.URI

object ExtendedURLClient {
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
