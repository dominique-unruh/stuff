package de.unruh.stuff.shared

trait AjaxApi {
  /** Returns the first `numResults` matches in the item database.
   * Most recently modified first. */
  def search(searchString: String, numResults: Int) : Seq[Item.Id]
  def getItem(id: Item.Id) : Item
  /** Updates this item. `item.id` must refer to an already existing item. */
  def updateItem(item: Item) : Unit
  /** Adds this item to the database. `item.id` must be [[Item.INVALID_ID]].
   * The id will be chosen by the server. */
  def createItem(item: Item) : Item.Id
  /** Sets the last modified time of the item (not made persistent) */
  def touchLastModified(id: Item.Id) : Unit
}

