package de.unruh.stuff.shared

trait AjaxApi {
  def search(searchString: String) : Seq[Item.Id]
  def getItem(id: Item.Id) : Item
  def updateItem(item: Item) : Unit
}

