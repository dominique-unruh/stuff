package de.unruh.stuff

import de.unruh.stuff.db.Yaml
import de.unruh.stuff.shared.{AjaxApi, Item}
import de.unruh.stuff.shared.Item.Id
import play.api.libs.json.JsValue
import play.twirl.api.TemplateMagic.anyToDefault
import Paths.dbPath
import ujson.play.PlayJson

import java.nio.file.Path
import scala.concurrent.ExecutionContext.Implicits.global

object AjaxApiImpl extends AjaxApi {
  // TODO: truncate to reasonable number of results
  // TODO: most recent first
  override def search(searchString: String): Seq[Item.Id] = {
    // TODO don't reload DB each time
    val db = Yaml.loadDb(dbPath)
    Search.search(db, searchString).map(_.id)
  }

  override def getItem(id: Id): Item = {
    // TODO don't reload DB each time
    val db = Yaml.loadDb(dbPath)
    db.getOrElse(id, throw new IllegalArgumentException(s"Unknown item id $id"))
  }

  // TODO: Replace any data-url photos (or files) by local URLs to files
  override def updateItem(item: Item): Unit = {
    Yaml.updateItem(dbPath, item)
  }
}

object AjaxApiServer extends autowire.Server[JsValue, upickle.default.Reader, upickle.default.Writer] {
  def write[Result: upickle.default.Writer](r: Result): JsValue = upickle.default.transform(r).to(PlayJson)
  def read[Result: upickle.default.Reader](p: JsValue): Result = PlayJson.transform(p, upickle.default.reader[Result])
  val routes: AjaxApiServer.Router = AjaxApiServer.route[AjaxApi](AjaxApiImpl)
}

