package de.unruh.stuff

import de.unruh.stuff.shared.Item

import java.nio.file.Path

object Paths {
  val dbPath: Path = Path.of("../my-stuff").toAbsolutePath.normalize()
  val filesPath: Path = dbPath.resolve("files")
  def itemsPath(path: Path): Path = path.resolve("items")
  def itemsPath(path: Path, id: Item.Id): Path = itemsPath(path).resolve(s"$id.yaml")
  def itemsPath(id: Item.Id): Path = itemsPath(dbPath, id)
  val itemsPath: Path = itemsPath(dbPath)
}
