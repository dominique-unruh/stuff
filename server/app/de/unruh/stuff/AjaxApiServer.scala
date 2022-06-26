package de.unruh.stuff

import de.unruh.stuff.shared.{AjaxApi, Item}
import de.unruh.stuff.shared.Item.Id
import play.api.libs.json.JsValue
import Paths.dbPath
import de.unruh.stuff.db.YamlDb
import ujson.play.PlayJson

import scala.concurrent.ExecutionContext.Implicits.global

class AjaxApiImpl(user: String)(implicit config: Config) extends AjaxApi {
  type Db = Map[Item.Id, Item]
  private var _db : Db = _

  private def getDb: Db =
    if (_db != null)
      _db
    else synchronized {
      if (_db != null)
        _db
      else {
        _db = YamlDb.loadDb(user)
        _db
      }
    }

  /** Only use in a synchronized block with `db` fetched within that block. */
  private def setDb(db: Db): Unit = {
    _db = db
  }

  override def search(searchString: String, numResults: Int, showFirst: Option[Item.Id]): Seq[(Item.Id, Long)] = {
    val db = getDb
    def sortBy(item: Item) =
      if (showFirst.contains(item.id)) Long.MinValue
      else - item.lastModified
    val results = Search.search(db, searchString)
    results
      .sortBy(sortBy)
      .take(numResults)
      .map(_.idAndTime)
  }

  override def getItem(id: Id): Item = {
    val db = getDb
    db.getOrElse(id, throw new IllegalArgumentException(s"Unknown item id $id"))
  }

  override def updateItem(item: Item): Long = synchronized {
    val db = getDb
    val item2 = YamlDb.updateItem(user, item)
    setDb(db.updated(item2.id, item2))
    item2.lastModified
  }

  override def createItem(item: Item): (Item.Id, Long) = synchronized {
    val db = getDb
    val item2 = YamlDb.createItem(user, item)
    setDb(db.updated(item2.id, item2))
    item2.idAndTime
  }

  /** Sets the last modified time of the item (not made persistent) */
  override def touchLastModified(id: Id): Unit = synchronized {
    val db = getDb
    val item = db.getOrElse(id, throw new IllegalArgumentException(s"Unknown item id $id"))
    setDb(db.updated(id, item.updateLastModified))
  }

  override def clearLocation(id: Id): Id = synchronized {
    val db = getDb
    val item = db.getOrElse(id, throw new IllegalArgumentException(s"Unknown item id $id"))
      .clearLocation
      .updateLastModified
    setDb(db.updated(id, item))
    item.lastModified
  }

  override def setLocation(id: Id, locationId: Id): Id = synchronized {
    val db = getDb
    val item = db.getOrElse(id, throw new IllegalArgumentException(s"Unknown item id $id"))
      .setLocation(locationId)
      .updateLastModified
    setDb(db.updated(id, item))
    item.lastModified
  }
}

object AjaxApiServer extends autowire.Server[JsValue, upickle.default.Reader, upickle.default.Writer] {
  def write[Result: upickle.default.Writer](r: Result): JsValue = upickle.default.transform(r).to(PlayJson)
  def read[Result: upickle.default.Reader](p: JsValue): Result = PlayJson.transform(p, upickle.default.reader[Result])
  private var impls : Map[String, AjaxApiImpl] = Map()
  private def getImpl(user: String)(implicit config: Config) : AjaxApiImpl =
    impls.get(user) match {
      case Some(impl) => impl
      case None => synchronized {
        impls.get(user) match {
          case Some(impl) => impl
          case None =>
            val impl = new AjaxApiImpl(user)
            impls = impls.updated(user, impl)
            impl
        }
      }
    }

  def routes(user: String)(implicit config: Config): AjaxApiServer.Router = AjaxApiServer.route[AjaxApi](getImpl(user))
}

