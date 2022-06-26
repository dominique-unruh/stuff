package de.unruh.stuff.shared

import de.unruh.stuff.shared.Utils.escapeFilename
import org.scalatest.funsuite.AnyFunSuite

class UtilsTest extends AnyFunSuite {
  test("escapeFilename") {
    assert(escapeFilename("hello") == "hello")
    assert(escapeFilename(".test") == "%2etest")
    assert(escapeFilename("привет") == "%d0%bf%d1%80%d0%b8%d0%b2%d0%b5%d1%82")
    assert(escapeFilename("test.2@gmail.com") == "test.2@gmail.com")
    assert(escapeFilename("a\\b") == "a%5cb")
    assert(escapeFilename("a/b") == "a%2fb")
    assert(escapeFilename("") == "%__")
  }
}
