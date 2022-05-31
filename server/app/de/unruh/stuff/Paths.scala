package de.unruh.stuff

import java.nio.file.Path

object Paths {
  val dbPath: Path = Path.of("../my-stuff").toAbsolutePath.normalize()
  val filesPath: Path = dbPath.resolve("files")
  def itemsPath(path: Path): Path = path.resolve("items")
  val itemsPath: Path = itemsPath(dbPath)
}
