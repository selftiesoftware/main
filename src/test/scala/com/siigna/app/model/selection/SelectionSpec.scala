/*
 *
 *  * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 *  * to Share — to copy, distribute and transmit the work,
 *  * to Remix — to adapt the work
 *  *
 *  * Under the following conditions:
 *  * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 *  * Noncommercial — You may not use this work for commercial purposes.
 *  * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 *
 */

package com.siigna.app.model.selection

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import scala.collection.immutable.BitSet
import com.siigna.app.model.shape.{Shape, PolylineShape, CircleShape, LineShape}
import com.siigna.util.geom.{TransformationMatrix, Rectangle2D, Vector2D}
import com.siigna.util.collection.Attributes

/**
 * Test the Selections
 */
class SelectionSpec extends FunSpec with ShouldMatchers {

  val circle = CircleShape(Vector2D(0, 0), 10)
  val polyline = PolylineShape(Vector2D(0, 0), Vector2D(-10, 15), Vector2D(200, 16))
  val selector = ShapeSelector(1, 2)

  def nonEmpty = NonEmptySelection(
    Map(1 -> (LineShape(0, 0, 10, 10) -> FullShapeSelector),
        2 -> (circle -> ShapeSelector(0, 1)))
  )

  describe("A NonEmptySelection") {

    it ("can add another shape") {
      nonEmpty.add(3, (polyline -> selector)) should equal (
        NonEmptySelection(nonEmpty.selection + (3 -> (polyline -> selector)))
      )
    }

    it ("can add another shape that already exists") {
      nonEmpty.add(2, (circle -> selector)) should equal (
        NonEmptySelection(nonEmpty.selection.updated(2, (circle -> ShapeSelector(0, 1, 2))))
      )
    }

    it ("can add a number of shapes") {
      val s = Map(3 -> (polyline -> selector))
      nonEmpty.add(s) should equal (NonEmptySelection(nonEmpty.selection ++ s))
    }

    it ("can add a number of shapes that already exists") {
      val s = Map(2 -> (circle -> selector))
      nonEmpty.add(s) should equal (NonEmptySelection(nonEmpty.selection.updated(2, (circle -> ShapeSelector(0, 1, 2)))))
    }

    it ("can extract its parts") {
      nonEmpty.parts should equal(nonEmpty.selection.map(t => t._2._1.getShape(t._2._2)).flatten)
    }

    it ("can remove a shape") {
      nonEmpty.remove(1) should equal(NonEmptySelection(nonEmpty.selection - 1))
    }

    it ("can remove a number of shapes") {
      nonEmpty.remove(Seq(1, 2)) should equal (EmptySelection)
    }

    it ("can remove a part of a shape") {
      nonEmpty.remove(2, ShapeSelector(1)) should equal (NonEmptySelection(
        nonEmpty.selection.updated(2, (circle -> ShapeSelector(0)))
      ))
    }

    it ("can remove a part of a shape that is not included") {
      nonEmpty.remove(4, selector) should equal (nonEmpty)
    }

    it ("can remove a number of parts from a number of shapes") {
      val m = Map(2 -> selector, 3 -> FullShapeSelector)
      val x = nonEmpty.remove(m)
      x should equal (NonEmptySelection(
        nonEmpty.selection.updated(2, circle -> ShapeSelector(0))
      ))
    }

    it ("can be transformed") {
      val t = TransformationMatrix().rotate(12.653)
      nonEmpty.transform(t).transformation should equal (t)
    }

    it ("can change its attributes") {
      val a = Attributes("Test" -> "37aæøå")
      nonEmpty.setAttributes(a).attributes should equal (a)
    }

    it ("can return its shapes") {
      nonEmpty.shapes should equal (nonEmpty.selection.map(t => t._1 -> t._2._1))
    }

    it ("can return its shapes after changing tranformation and attributes") {
      val a = Attributes("Test" -> "37aæøå")
      val t = TransformationMatrix().rotate(12.653)
      val n = nonEmpty.setAttributes(a).transform(t)
      val y = nonEmpty.selection.map(x => x._1 -> x._2._1.setAttributes(a).transform(t)).toMap[Int, Shape]
      n.shapes should equal (y)
    }

  }

}