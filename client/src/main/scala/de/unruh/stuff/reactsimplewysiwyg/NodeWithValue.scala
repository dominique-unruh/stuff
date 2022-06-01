package de.unruh.stuff.reactsimplewysiwyg

import org.scalajs.dom.Node

trait NodeWithValue extends Node {
  def value: String
}
