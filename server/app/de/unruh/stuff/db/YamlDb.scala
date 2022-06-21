package de.unruh.stuff.db

import de.unruh.stuff.Paths
import de.unruh.stuff.shared.Item

import java.nio.file.{Files, Path}
import scala.util.Random

import net.jcazevedo.moultingyaml.{PimpedAny, PimpedString}
import de.unruh.stuff.YamlRW._
import scala.jdk.StreamConverters._
import scala.jdk.CollectionConverters.IteratorHasAsScala

/** Manipulating Yamls files in the DB */
object YamlDb {
  def parse(yaml: String): Item = yaml.parseYaml.convertTo[Item]

  def parse(path: Path): Item = parse(Files.readString(path))

  def loadDb(path: Path): Map[Long, Item] = {
    Map.from(
      for (file <- Files.list(Paths.itemsPath(path)).iterator.asScala;
           filename = file.getFileName.toString;
           if !filename.startsWith(".")
           if !filename.endsWith("~")
           ) yield {
        assert(filename.endsWith(".yaml"))
        val id = filename.stripSuffix(".yaml").toLong
        val item = parse(file)
        assert(item.id == id)
        id -> item
      })
  }

  def itemExists(id: Item.Id): Boolean =
    Files.exists(Paths.itemsPath(id))

  def createItem(path: Path, item: Item): Item = {
    val item2 = item.copy(id = Random.nextInt(Int.MaxValue))
    assert(!itemExists(item2.id))
    updateItemMaybeNonExisting(path, item2)
  }

  def updateItem(path: Path, item: Item): Item = {
    assert(itemExists(item.id))
    updateItemMaybeNonExisting(path, item)
  }

  private def updateItemMaybeNonExisting(path: Path, item: Item): Item = {
    val item2 = ProcessItems.processItem(item)
    assert(item2.id != Item.INVALID_ID && item2.id >= 0)
    val itemPath = Paths.itemsPath(path, item2.id)
    val yaml = item2.toYaml.prettyPrint
    Files.writeString(itemPath, yaml)
    item2
  }
}
