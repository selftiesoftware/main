/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.model

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import com.siigna._
import com.siigna.app.model.selection.FullShapePart
import com.siigna.app.model.selection

/**
 * Test the [[selection.Selection]], [[NonEmptySelection]] and
 * [[com.siigna.app.model.EmptySelection]].
 */
class SelectionSpec extends FunSpec with ShouldMatchers {

  describe("An empty selection") {

    val selection = Selection.empty

    it ("is empty") { selection.isEmpty should equal(true) }

    it ("can be transformed with no effect") {
      selection.transform(TransformationMatrix(Vector2D(10, 0))) should equal(selection)
    }

    it ("can change its attributes with no effect") {
      selection.addAttribute("Color" -> "#123456".color) should equal(selection)
      selection.attributes.empty should equal(true)
    }

  }

  describe("A non empty selection") {

    val shape = LineShape(0, 0, 10, 10)
    val selection = NonEmptySelection(Map(1 -> (shape, FullShapePart)))

    it ("is not empty") {
      selection.isEmpty should equal(false)
    }

    it ("can be transformed") {
      val t = TransformationMatrix(Vector2D(10, 2.7656))
      selection.transform(t)
      selection.transformation should equal(t)
    }

    it ("can change attributes") {
      val a = Attributes("Color" -> "#123456".color)
      selection.setAttributes(a)
      selection.attributes should equal(a)
    }

    it ("can find the distance to a point") {
      val p = Vector2D(12, 10)
      selection.distanceTo(p) should equal(2)
    }

    it ("has a boundary") {
      selection.boundary should equal(Rectangle2D(0, 0, 10, 10))
    }

  }
}