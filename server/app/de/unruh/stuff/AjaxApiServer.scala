package de.unruh.stuff

import de.unruh.stuff.shared.{AjaxApi, Item}
import de.unruh.stuff.shared.Item.Id
import play.api.libs.json.JsValue
import Paths.dbPath
import de.unruh.stuff.db.YamlDb
import ujson.play.PlayJson

import scala.concurrent.ExecutionContext.Implicits.global

object AjaxApiImpl extends AjaxApi {
  override def search(searchString: String, numResults: Int): Seq[Item.Id] = {
    // TODO don't reload DB each time
    val db = YamlDb.loadDb(dbPath)
    val results = Search.search(db, searchString)
    results
      .sortBy(-_.lastModified)
      .take(numResults)
      .map(_.id)
  }

  override def getItem(id: Id): Item = {
    // TODO don't reload DB each time
    val db = YamlDb.loadDb(dbPath)
    db.getOrElse(id, throw new IllegalArgumentException(s"Unknown item id $id"))
  }

  override def updateItem(item: Item): Unit = {
    YamlDb.updateItem(dbPath, item)
  }

  override def createItem(item: Item): Item.Id = {
    YamlDb.createItem(dbPath, item)
  }
}

object AjaxApiServer extends autowire.Server[JsValue, upickle.default.Reader, upickle.default.Writer] {
  def write[Result: upickle.default.Writer](r: Result): JsValue = upickle.default.transform(r).to(PlayJson)
  def read[Result: upickle.default.Reader](p: JsValue): Result = PlayJson.transform(p, upickle.default.reader[Result])
  val routes: AjaxApiServer.Router = AjaxApiServer.route[AjaxApi](AjaxApiImpl)
}

