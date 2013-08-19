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

package com.siigna.app.model.shape

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.siigna.util.geom.{TransformationMatrix, SimpleRectangle2D, Vector2D}
import com.siigna.app.model.selection.{FullShapeSelector, EmptyShapeSelector, ShapeSelector}

/**
 * Tests the [[com.siigna.app.model.shape.CircleShape]].
 */
class CircleShapeSpec extends FunSpec with ShouldMatchers {

  describe("A CircleShape") {

    val c = CircleShape(Vector2D(0, 0), 20)

    it ("can find a selector given a point") {
      c.getSelector(Vector2D(  0,  0)) should equal (FullShapeSelector)
      c.getSelector(Vector2D( 20,  0)) should equal (ShapeSelector(1))
      c.getSelector(Vector2D(  0, 20)) should equal (ShapeSelector(2))
      c.getSelector(Vector2D(-20,  0)) should equal (ShapeSelector(3))
      c.getSelector(Vector2D(  0,-20)) should equal (ShapeSelector(4))
      c.getSelector(Vector2D(10000, 10000)) should equal (EmptyShapeSelector)

      c.getSelector(Vector2D(17, 17))
    }

    it ("can find a selector given a rectangle") {
      c.getSelector(SimpleRectangle2D(-1, -1, 1, 1)) should equal (ShapeSelector(0))
      c.getSelector(SimpleRectangle2D(-1, -1, 20, 1)) should equal (ShapeSelector(0, 1))
      c.getSelector(SimpleRectangle2D(-1, -1, 20, 20)) should equal (ShapeSelector(0, 1, 2))
      c.getSelector(SimpleRectangle2D(-20, -1, 20, 20)) should equal (ShapeSelector(0, 1, 2, 3))
      c.getSelector(SimpleRectangle2D(-20, -20, 20, 20)) should equal (FullShapeSelector)
      c.getSelector(SimpleRectangle2D(-1, -1, 1, 20)) should equal (ShapeSelector(0, 2))
      c.getSelector(SimpleRectangle2D(-20, -1, 1, 20)) should equal (ShapeSelector(0, 2, 3))
      c.getSelector(SimpleRectangle2D(-20, -20, 1, 20)) should equal (ShapeSelector(0, 2, 3, 4))
      c.getSelector(SimpleRectangle2D(-20, -1, 1, 1)) should equal (ShapeSelector(0, 3))
      c.getSelector(SimpleRectangle2D(-20, -20, 1, 1)) should equal (ShapeSelector(0, 3, 4))

      c.getSelector(SimpleRectangle2D(1, -1, 20, 1)) should equal (ShapeSelector(1))
      c.getSelector(SimpleRectangle2D(-1, 19, 1, 21)) should equal (ShapeSelector(2))
      c.getSelector(SimpleRectangle2D(-21, -1, -19, 1)) should equal (ShapeSelector(3))
      c.getSelector(SimpleRectangle2D(-1, -21, 1, -19)) should equal (ShapeSelector(4))
    }

    it ("can find a number of vertices given a shapeselector") {
      c.getVertices(EmptyShapeSelector) should equal (Nil)
      c.getVertices(FullShapeSelector) should equal (c.geometry.vertices)

      c.getVertices(ShapeSelector(0)) should equal (Seq(c.geometry.vertices(0)))
      c.getVertices(ShapeSelector(1)) should equal (Seq(c.geometry.vertices(1)))
      c.getVertices(ShapeSelector(2)) should equal (Seq(c.geometry.vertices(2)))
      c.getVertices(ShapeSelector(3)) should equal (Seq(c.geometry.vertices(3)))
      c.getVertices(ShapeSelector(4)) should equal (Seq(c.geometry.vertices(4)))
      c.getVertices(ShapeSelector(0, 1, 2, 3, 4)) should equal (c.geometry.vertices)
    }

    it ("can find a part from a selector") {
      val t1 = TransformationMatrix(Vector2D( 10,  0), 0.5)
      val t2 = TransformationMatrix(Vector2D(-10,  0), 2.0)

      // Test the entire circle
      c.getPart(FullShapeSelector).get.apply(t1) should equal (CircleShape(Vector2D(10, 0), 10))
      c.getPart(ShapeSelector(0)).get.apply(t1) should equal (CircleShape(Vector2D(10, 0), 10))
      c.getPart(ShapeSelector(1, 2, 3, 4)).get.apply(t1) should equal (CircleShape(Vector2D(10, 0), 10))

      // Two points => entire circle
      c.getPart(ShapeSelector(1, 3)).get.apply(t1) should equal (CircleShape(Vector2D(10, 0), 10))
      c.getPart(ShapeSelector(2, 4)).get.apply(t1) should equal (CircleShape(Vector2D(10, 0), 10))

      // Test single point
      c.getPart(ShapeSelector(1)).get.apply(t2) should equal (CircleShape(Vector2D(5, 0), 25))

      // Test two points
      c.getPart(ShapeSelector(1, 2)).get.apply(TransformationMatrix(Vector2D( 10, 10), 1.0)
        ) should equal (CircleShape(Vector2D( 5.606601717798213, 5.606601717798213), 26.213203435596427))
      c.getPart(ShapeSelector(2, 3)).get.apply(TransformationMatrix(Vector2D(-10, 10), 1.0)
        ) should equal (CircleShape(Vector2D(-5.606601717798213, 5.606601717798213), 26.213203435596427))
      c.getPart(ShapeSelector(3, 4)).get.apply(TransformationMatrix(Vector2D(-10,-10), 1.0)
        ) should equal (CircleShape(Vector2D(-5.606601717798213,-5.606601717798213), 26.213203435596427))
      c.getPart(ShapeSelector(1, 4)).get.apply(TransformationMatrix(Vector2D( 10,-10), 1.0)
        ) should equal (CircleShape(Vector2D( 5.606601717798213,-5.606601717798213), 26.213203435596427))

      // Test three points
      c.getPart(ShapeSelector(1, 2, 3)).get.apply(TransformationMatrix(Vector2D(  0, 10), 2.0)
        ) should equal (CircleShape(Vector2D( 0, 15), 35))
      c.getPart(ShapeSelector(1, 2, 3)).get.apply(TransformationMatrix(Vector2D(  0, 10), 2.0)
        ).geometry.vertices(4) should equal (Vector2D(  0,-20))
      c.getPart(ShapeSelector(2, 3, 4)).get.apply(TransformationMatrix(Vector2D(-10,  0), 2.0)
        ) should equal (CircleShape(Vector2D(-15, 0), 35))
      c.getPart(ShapeSelector(2, 3, 4)).get.apply(TransformationMatrix(Vector2D(-10,  0), 2.0)
        ).geometry.vertices(1) should equal (Vector2D( 20,  0))
      c.getPart(ShapeSelector(1, 3, 4)).get.apply(TransformationMatrix(Vector2D(  0,-10), 2.0)
        ) should equal (CircleShape(Vector2D( 0,-15), 35))
      c.getPart(ShapeSelector(1, 3, 4)).get.apply(TransformationMatrix(Vector2D(  0,-10), 2.0)
        ).geometry.vertices(2) should equal (Vector2D(  0, 20))
      c.getPart(ShapeSelector(1, 2, 4)).get.apply(TransformationMatrix(Vector2D( 10,  0), 2.0)
        ) should equal (CircleShape(Vector2D(15,  0), 35))
      c.getPart(ShapeSelector(1, 2, 4)).get.apply(TransformationMatrix(Vector2D( 10,  0), 2.0)
        ).geometry.vertices(3) should equal (Vector2D(-20,  0))
    }
  }
}
