package de.unruh.stuff

import de.unruh.stuff.shared.{Item, Utils}
import play.api.Configuration

import java.nio.file
import java.nio.file.Path

object Paths {

  def dbRoot(implicit config: Config): Path = config.dbRoot.normalize()
  def dbPath(user: String)(implicit config: Config): Path = {
    val userSanitized = Utils.escapeFilename(user)
    dbRoot.resolve(userSanitized)
  }

  def itemsPath(user: String)(implicit config: Config): Path = dbPath(user).resolve("items")
  def itemPath(user: String, id: Item.Id)(implicit config: Config): Path = itemsPath(user).resolve(s"$id.yaml")

  def filesPath(user: String)(implicit config: Config): Path = dbPath(user).resolve("files")
  def filesPath(user: String, id: Item.Id)(implicit config: Config): Path = filesPath(user).resolve(id.toString)
  def filePath(user: String, id: Long, filename: String)(implicit config: Config): Path = {
    val dir = filesPath(user, id)
    val path = dir.resolve(filename).normalize()
    assert(dir == path.getParent)
    path
  }

}
