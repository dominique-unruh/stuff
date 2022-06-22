package de.unruh.stuff

import autowire.clientCallable
import de.unruh.stuff.shared.{AjaxApi, Item}
import de.unruh.stuff.shared.Item.{INVALID_ID, Id}
import japgolly.scalajs.react.callback.AsyncCallback

import scala.collection.mutable
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object DbCache {
  /** Retrieves item `id` from the server or the cache.
   * If `modificationTime` is newer than the cached copy, reload from server. */
  def getItem(id: Id, modificationTime: Long): Future[Item] = {
    val counter = updateCounter
    cache.get(id) match {
      case Some(item) if item.lastModified >= modificationTime =>
        Future.successful(item)
      case _ =>
        for (item <- AjaxApiClient[AjaxApi].getItem(id).call();
             _ = if (updateCounter == counter) cache.put(id, item);
             item2 <- getItem(id, modificationTime))
          yield item2
    }
  }

  private implicit class FinallyFuture[A](val future: Future[A]) extends AnyVal {
    def finalmente(code: => Unit): Future[A] = future.transform { result => code; result }
  }

  def updateOrCreateItem(item: Item) : Future[(Item.Id, Long)] =
    if (item.id == INVALID_ID)
      createItem(item)
    else
      for (time <- updateItem(item))
        yield (item.id, time)

  def updateItem(item: Item) : Future[Long] = {
    assert(item.id != INVALID_ID)
    for (time <- AjaxApiClient[AjaxApi].updateItem(item).call()
                  .finalmente { updateCounter += 1; cache.remove(item.id) };
         // Removing the item from the cache because the server may process it in .updateItem
         _ = cache.remove(item.id))
      yield time
  }

  def createItem(item: Item) : Future[(Item.Id, Long)] = {
    assert(item.id == INVALID_ID)
    AjaxApiClient[AjaxApi].createItem(item).call()
  }

  def updateOrCreateItemReact(item: Item) : AsyncCallback[(Item.Id, Long)] =
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

  /** Sets the location of `itemId` to `locationId`.
   * @return the new `lastModification` time */
  def setLocation(itemId: Item.Id, locationId: Item.Id): Future[Long] = {
    cache.remove(itemId)
    AjaxApiClient[AjaxApi].setLocation(itemId, locationId).call()
  }

  def setLocationReact(itemId: Item.Id, locationId: Item.Id): AsyncCallback[Long] =
    AsyncCallback.fromFuture(setLocation(itemId, locationId))

  /** Clears the location of `itemId`
   * @return the new `lastModification` time */
  def clearLocation(itemId: Item.Id): Future[Long] = {
    cache.remove(itemId)
    AjaxApiClient[AjaxApi].clearLocation(itemId).call()
  }

  def clearLocationReact(itemId: Item.Id): AsyncCallback[Long] =
    AsyncCallback.fromFuture(clearLocation(itemId))

  private val cache = mutable.HashMap[Item.Id, Item]()
  /** Increase this when updating the DB so that pending Ajax calls will be ignored. */
  private var updateCounter = 0
}
