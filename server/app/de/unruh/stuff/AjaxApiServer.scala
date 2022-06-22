package de.unruh.stuff

import de.unruh.stuff.shared.{AjaxApi, Item}
import de.unruh.stuff.shared.Item.Id
import play.api.libs.json.JsValue
import Paths.dbPath
import de.unruh.stuff.db.YamlDb
import ujson.play.PlayJson

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global

object AjaxApiImpl extends AjaxApi {
  type Db = Map[Item.Id, Item]
  private var _db : Db = _

  private def getDb: Db =
    if (_db != null)
      _db
    else synchronized {
      if (_db != null)
        _db
      else {
        _db = YamlDb.loadDb(dbPath)
        _db
      }
    }

  /** Only use in a synchronized block with `db` fetched within that block. */
  private def setDb(db: Db): Unit = {
    _db = db
  }

  override def search(searchString: String, numResults: Int): Seq[Item.Id] = {
    val db = getDb
    val results = Search.search(db, searchString)
    results
      .sortBy(-_.lastModified)
      .take(numResults)
      .map(_.id)
    // TODO return id,lastModified pairs -> client knows which to invalidate
  }

  override def getItem(id: Id): Item = {
    val db = getDb
    db.getOrElse(id, throw new IllegalArgumentException(s"Unknown item id $id"))
  }

  override def updateItem(item: Item): Unit = synchronized {
    val db = getDb
    val item2 = YamlDb.updateItem(dbPath, item)
    setDb(db.updated(item2.id, item2))
  }

  override def createItem(item: Item): Item.Id = synchronized {
    val db = getDb
    val item2 = YamlDb.createItem(dbPath, item)
    setDb(db.updated(item2.id, item2))
    item2.id
  }

  /** Sets the last modified time of the item (not made persistent) */
  override def touchLastModified(id: Id): Unit = synchronized {
    val db = getDb
    val item = db.getOrElse(id, throw new IllegalArgumentException(s"Unknown item id $id"))
    setDb(db.updated(id, item.updateLastModified))
  }
}

object AjaxApiServer extends autowire.Server[JsValue, upickle.default.Reader, upickle.default.Writer] {
  def write[Result: upickle.default.Writer](r: Result): JsValue = upickle.default.transform(r).to(PlayJson)
  def read[Result: upickle.default.Reader](p: JsValue): Result = PlayJson.transform(p, upickle.default.reader[Result])
  val routes: AjaxApiServer.Router = AjaxApiServer.route[AjaxApi](AjaxApiImpl)
}

