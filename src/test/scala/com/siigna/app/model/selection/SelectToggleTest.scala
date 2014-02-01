/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */

package com.siigna.app.model.selection

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, FunSpec}
import com.siigna.app.model.{Model, Drawing}
import com.siigna.app.model.shape.{PolylineShape, LineShape}
import com.siigna.util.collection.Attributes
import com.siigna.util.geom.{SimpleRectangle2D, Vector2D}

/**
 * Test the Select class.
 */
class SelectToggleTest extends FunSpec with ShouldMatchers with BeforeAndAfter {

  var drawing : Drawing = null
  val line     = LineShape(0, 0, 10, 10)
  val openPL   = PolylineShape(Vector2D(0, 0), Vector2D(10, 0), Vector2D(10, 10), Vector2D(0, 10))
  val closedPL = PolylineShape(Vector2D(0, 0), Vector2D(10, 0), Vector2D(10, 10), Vector2D(0, 10), Vector2D(0, 0))

  before {
    drawing = new Drawing {
      override def boundary = SimpleRectangle2D(0, 0, 0, 0)
      model = new Model(Map(
        -1 -> line,
        -2 -> openPL,
        -3 -> closedPL
      ),Nil, Nil, Attributes())
    }
  }

  describe ("SelectToggleTest") {

    it ("can toggle a single shape from its id") {
      SelectToggle(-1)(drawing)
      drawing.selection should equal(Selection(-1 -> (line -> FullShapeSelector)))
    }

    it ("can toggle a part of a line") {
      drawing.selection = Selection(-1 -> (line -> ShapeSelector(0, 1)))
      SelectToggle(-1, ShapeSelector(1))(drawing)
      drawing.selection should equal(Selection(-1 -> (line -> ShapeSelector(0))))
    }

    it ("can toggle a part of an open polyline") {
      SelectToggle(-2, ShapeSelector(0, 1))(drawing)
      drawing.selection should equal(Selection(-2 -> (openPL -> ShapeSelector(0, 1))))

      SelectToggle(-2, ShapeSelector(1, 2))(drawing)
      drawing.selection should equal(Selection(-2 -> (openPL -> ShapeSelector(0, 1, 2))))

      SelectToggle(-2, ShapeSelector(3))(drawing)
      drawing.selection should equal(Selection(-2 -> (openPL -> ShapeSelector(0, 1, 2, 3))))

      SelectToggle(-2, ShapeSelector(1, 2))(drawing)
      drawing.selection should equal(Selection(-2 -> (openPL -> ShapeSelector(0, 3))))
    }

    it ("can toggle a part of a closed polyline") {
      SelectToggle(-3, ShapeSelector(0, 1))(drawing)
      drawing.selection should equal(Selection(-3 -> (closedPL -> ShapeSelector(0, 1))))

      SelectToggle(-3, ShapeSelector(1, 2))(drawing)
      drawing.selection should equal(Selection(-3 -> (closedPL -> ShapeSelector(0, 1, 2))))

      SelectToggle(-3, ShapeSelector(3))(drawing)
      drawing.selection should equal(Selection(-3 -> (closedPL -> ShapeSelector(0, 1, 2, 3))))

      SelectToggle(-3, ShapeSelector(0, 3))(drawing)
      drawing.selection should equal(Selection(-3 -> (closedPL -> ShapeSelector(1, 2))))

      SelectToggle(-3, ShapeSelector(0, 3))(drawing)
      drawing.selection should equal(Selection(-3 -> (closedPL -> ShapeSelector(0, 1, 2, 3))))

      SelectToggle(-3, ShapeSelector(0, 1))(drawing)
      drawing.selection should equal(Selection(-3 -> (closedPL -> ShapeSelector(2, 3))))

      println(drawing.selection)
      SelectToggle(-3, ShapeSelector(0, 1))(drawing)
      println(drawing.selection)
      drawing.selection should equal(Selection(-3 -> (closedPL -> ShapeSelector(0, 1, 2, 3))))

      SelectToggle(-3, ShapeSelector(0))(drawing)
      drawing.selection should equal(Selection(-3 -> (closedPL -> ShapeSelector(1, 2, 3))))
    }

  }

}
