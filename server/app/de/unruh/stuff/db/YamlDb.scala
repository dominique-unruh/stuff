package de.unruh.stuff.db

import de.unruh.stuff.{Config, Paths}
import de.unruh.stuff.shared.Item

import java.nio.file.{Files, Path}
import scala.util.Random
import net.jcazevedo.moultingyaml.{PimpedAny, PimpedString}
import de.unruh.stuff.YamlRW._

import scala.jdk.StreamConverters._
import scala.jdk.CollectionConverters.IteratorHasAsScala

/** Manipulating Yamls files in the DB */
object YamlDb {
  def userExists(user: String)(implicit config: Config): Boolean =
    Files.exists(Paths.itemsPath(user)) &&
      Files.exists(Paths.filesPath(user))

  def parse(yaml: String): Item = yaml.parseYaml.convertTo[Item]

  def parse(path: Path): Item = parse(Files.readString(path))

  def loadDb(user: String)(implicit config: Config): Map[Long, Item] = {
    Map.from(
      for (file <- Files.list(Paths.itemsPath(user)).iterator.asScala;
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

  def itemExists(user: String, id: Item.Id)(implicit config: Config): Boolean =
    Files.exists(Paths.itemPath(user, id))

  def createItem(user: String, item: Item)(implicit config: Config): Item = {
    val item2 = item.copy(id = Random.nextInt(Int.MaxValue))
    assert(!itemExists(user, item2.id))
    updateItemMaybeNonExisting(user, item2)
  }

  def updateItem(user: String, item: Item)(implicit config: Config): Item = {
    assert(itemExists(user, item.id))
    updateItemMaybeNonExisting(user, item)
  }

  private def updateItemMaybeNonExisting(user: String, item: Item)(implicit config: Config): Item = {
    val item2 = ProcessItems.processItem(user, item)
    assert(item2.id != Item.INVALID_ID && item2.id >= 0)
    val itemPath = Paths.itemPath(user, item2.id)
    val yaml = item2.toYaml.prettyPrint
    Files.writeString(itemPath, yaml)
    item2
  }
}
