package de.unruh.stuff

import autowire.clientCallable
import de.unruh.stuff.shared.{AjaxApi, Item}
import de.unruh.stuff.shared.Item.{INVALID_ID, Id}
import japgolly.scalajs.react.callback.AsyncCallback

import scala.collection.mutable
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object DbCache {
  def getItem(id: Id): Future[Item] = {
    val counter = updateCounter
    cache.get(id) match {
      case Some(item) => Future.successful(item)
      case None =>
        for (item <- AjaxApiClient[AjaxApi].getItem(id).call();
             _ = if (updateCounter == counter) cache.put(id, item);
             item2 <- getItem(id))
          yield item2
    }
  }

  private implicit class FinallyFuture[A](val future: Future[A]) extends AnyVal {
    def finalmente(code: => Unit): Future[A] = future.transform { result => code; result }
  }

  def updateOrCreateItem(item: Item) : Future[Item.Id] =
    if (item.id == INVALID_ID)
      createItem(item)
    else
      for (_ <- updateItem(item))
        yield item.id

  def updateItem(item: Item) : Future[Unit] = {
    assert(item.id != INVALID_ID)
    for (_ <- AjaxApiClient[AjaxApi].updateItem(item).call()
                  .finalmente { updateCounter += 1; cache.remove(item.id) };
         // Removing the item from the cache because the server may process it in .updateItem
         _ = cache.remove(item.id))
      yield ()
  }

  def createItem(item: Item) : Future[Item.Id] = {
    assert(item.id == INVALID_ID)
    AjaxApiClient[AjaxApi].createItem(item).call()
  }

  def updateOrCreateItemReact(item: Item) : AsyncCallback[Item.Id] =
    AsyncCallback.fromFuture(updateOrCreateItem(item))

  /** Updates the `lastModified` field of the selected item (also on the server) but does not make this change persistent.
   * Operates in the background.
   *
   * (The rationale is that this affects the sorting order of recently search results within a session.
   * It is currently used to "touch" items if they are used as a location in a "put" operation.)
   * */
  def touchLastModified(id: Item.Id): Unit = {
    for (item <- cache.get(id))
      cache.update(id, item.updateLastModified)
    AjaxApiClient[AjaxApi].touchLastModified(id).call()
  }

  private val cache = mutable.HashMap[Item.Id, Item]()
  /** Increase this when updating the DB so that pending Ajax calls will be ignored. */
  private var updateCounter = 0
}
