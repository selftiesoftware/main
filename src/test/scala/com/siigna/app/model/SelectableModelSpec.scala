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
import org.scalatest.{BeforeAndAfter, FunSpec}
import com.siigna.app.model.shape.{PolylineShape, LineShape}
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.util.collection.Attributes
import com.siigna.app.model.selection._
import com.siigna.app.model.action.{SequenceAction, AddAttributes, TransformShapeParts, TransformShape}

/**
 * Test the selectable model
 */
class SelectableModelSpec extends FunSpec with ShouldMatchers with BeforeAndAfter {

  val line = LineShape(0, 0, 10, 10)
  val openPL = PolylineShape(Vector2D(0, 0), Vector2D(10, 0), Vector2D(10, 10), Vector2D(0, 10))
  val closedPL = PolylineShape(Vector2D(0, 0), Vector2D(10, 0), Vector2D(10, 10), Vector2D(0, 10), Vector2D(0, 0))
  val selector = ShapeSelector(1, 3)

  var model : SelectableModel = null

  before {
    model = new SelectableModel {
      model = new Model(Map(
        -1 -> line,
        -2 -> openPL,
        -3 -> closedPL
      ), Nil, Nil, Attributes())
    }
  }

  describe("A selectable model") {

    it ("can select a single shape") {
      model.select(-1).selection should equal (Selection(-1, line -> FullShapeSelector))
    }

    it ("can select a part of a single shape") {
      model.select(-1, selector).selection should equal (Selection(-1, line -> selector))
    }

    it ("can add a number of shapes to the selection") {
      val s = Selection(Map(-1 -> (line -> FullShapeSelector), -3 -> (closedPL -> FullShapeSelector)))
      model.select(Traversable(-1, -3)).selection should equal (s)
    }

    it ("can add a number of shape-parts") {
      val parts = Map(-1 -> selector, -3 -> selector)
      val s = Selection(Map(-1 -> (line -> selector), -3 -> (closedPL -> selector)))
      model.select(parts).selection should equal (s)
    }

    it ("can select an entire selection") {
      val s = Selection(-1 -> (line -> selector))
      model.select(-2)
      model.select(s).selection should equal(s.add(-2, openPL -> FullShapeSelector))
    }

    it ("can select everything in the model") {
      model.selectAll().selection should equal(Selection(Map(
        -1 -> (line, FullShapeSelector),
        -2 -> (openPL, FullShapeSelector),
        -3 -> (closedPL, FullShapeSelector)
      )))
    }

    it ("can deselect a single shape") {
      model.select(-1).deselect(-1).selection should equal (EmptySelection)
      model.select(Seq(-1, -2)).deselect(-1).selection should equal (Selection(-2 -> (openPL -> FullShapeSelector)))
    }

    it ("can deselect a part of a shape") {
      model.select(-1, selector).deselect(-1, ShapeSelector(1)).selection should equal(Selection(-1 -> (line, ShapeSelector(3))))
    }

    it ("can deselect a number of shapes") {
      model.select(Seq(-1, -3)).deselect(Seq(-1, -3)).selection should equal(EmptySelection)
      model.select(Seq(-1, -2, -3)).deselect(Seq(-1, -3)).selection should equal(Selection(-2 -> (openPL, FullShapeSelector)))
    }

    it ("can deselect a number of parts from a number of shapes") {
      val s = Map(-1 -> selector, -3 -> selector)
      val t = Map(-1 -> ShapeSelector(1), -3 -> selector)
      model.select(s).deselect(t).selection should equal (Selection(-1 -> (line -> ShapeSelector(3))))
    }

    it ("can clear the selection") {
      model.select(-1).clearSelection().selection should equal(EmptySelection)
    }

    it ("can retrieve actions made to the selection") {
      val t = TransformationMatrix(Vector2D(12, -1332), 0.4723)
      model.select(-1)
      model.selection.transform(t)
      model.getChanges should equal (Some(TransformShapeParts(Map(-1 -> FullShapeSelector), t)))

      model.select(-2, selector)
      model.selection.transform(t)
      model.getChanges should equal (Some(TransformShapeParts(Map(-2 -> selector), t)))

      val a = Attributes("Color" -> java.awt.Color.blue)
      model.select(-1)
      model.selection.setAttributes(a)
      model.getChanges should equal (Some(AddAttributes(Seq(-1), a)))

      model.select(-1)
      model.selection.setAttributes(a).transform(t)
      model.getChanges should equal (Some(SequenceAction(TransformShapeParts(Map(-1 -> FullShapeSelector), t),
                                                         AddAttributes(Map(-1 -> Attributes()), a))))
    }

  }

}