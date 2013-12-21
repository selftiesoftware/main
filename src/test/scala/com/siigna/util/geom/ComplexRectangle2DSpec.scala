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

package com.siigna.util.geom

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

class ComplexRectangle2DSpec extends FunSpec with ShouldMatchers {

  describe("A ComplexRectangle2D") {

    val r1 = ComplexRectangle2D(Vector2D(0,0),100,100,0)
    val r2 = ComplexRectangle2D(Vector2D(10,10),50,100,0)
    val r3 = ComplexRectangle2D(Vector2D(10,10),50,100,30) //rotated rectangles
    val r4 = ComplexRectangle2D(Vector2D(0,0), 100, 100, 180)
    val r5 = ComplexRectangle2D(Vector2D(0,0), 100, 100, 90)

    it("Can create an instance") {
      val rectGeom = ComplexRectangle2D(Vector2D(0,0),100,100,90)
      rectGeom.p0 should equal (Vector2D(-50, 50))
      rectGeom.p1 should equal (Vector2D(-50, -50))
      rectGeom.p2 should equal (Vector2D(50, -50))
      rectGeom.p3 should equal (Vector2D(50, 50))
    }

    /*
      p1  p0
      *   *
      *   *
      p2  p3
    */
    it ("can calculate p0") {
      r1.p0 should equal (Vector2D(50, 50))
      r2.p0 should equal (Vector2D(35, 60))
      r4.p0 should equal (Vector2D(-50, -50))
    }

    it ("can calculate p1") {
      r1.p1 should equal (Vector2D(-50, 50))
      r2.p1 should equal (Vector2D(-15, 60))
      r4.p1 should equal(Vector2D(50, -50))
    }

    it ("can calculate p2") {
      r1.p2 should equal (Vector2D(-50,-50))
      r2.p2 should equal (Vector2D(-15,-40))
      r4.p2 should equal(Vector2D(50, 50))
    }

    it ("can calculate p3") {
      r1.p3 should equal (Vector2D(50,-50))
      r2.p3 should equal (Vector2D(35,-40))
      r4.p3 should equal(Vector2D(-50, 50))
    }

    it ("can find the closest point to a given vector") {
      r1.closestPoint(Vector2D(50, 50)) should equal (Vector2D(50, 50))
      r1.closestPoint(Vector2D(-50, -50)) should equal (Vector2D(-50, -50))
      r1.closestPoint(Vector2D(1, 0)) should equal (Vector2D(50, 0))
      r1.closestPoint(Vector2D(2, 1)) should equal (Vector2D(50, 1))
      r4.closestPoint(Vector2D(50, 50)) should equal (Vector2D(50, 50))
      r4.closestPoint(Vector2D(-50, -50)) should equal (Vector2D(-50, -50))
      r4.closestPoint(Vector2D(1, 0)) should equal (Vector2D(50, 0))
      r4.closestPoint(Vector2D(2, 1)) should equal (Vector2D(50, 1))
    }

    it ("can know whether a geometry is contained in the rectangle") {
      // Segment2D
      r1.contains(Segment2D(-50, -50, 50, 50)) should equal (true)
      r1.contains(Segment2D(-1, -1, 1, 1)) should equal (true)
      r1.contains(Segment2D(-1, -1, 51, 1)) should equal (false)
      r4.contains(Segment2D(-1, -1, 51, 1)) should equal (false)

      // SimpleRectangle
      r1.contains(SimpleRectangle2D(-10, -10, 10, 10)) should equal (true)
      r1.contains(SimpleRectangle2D(-50, -50, 50, 50)) should equal (true)
      r1.contains(SimpleRectangle2D(-51, -50, 50, 50)) should equal (false)
      r4.contains(SimpleRectangle2D(-10, -10, 10, 10)) should equal (true)
      r4.contains(SimpleRectangle2D(-50, -50, 50, 51)) should equal (false)
      r1.contains(SimpleRectangle2D(-100, -100, 100, 100)) should equal (false)

      // Vector2D
      r1.contains(Vector2D(0, 0)) should equal (true)
      r1.contains(Vector2D(10, 10)) should equal (true)
      r1.contains(Vector2D(-10, 10)) should equal (true)
      r1.contains(Vector2D(100, 100)) should equal (false)
      r1.contains(Vector2D(50, 50)) should equal (true)
      r1.contains(Vector2D(51, 50)) should equal (false)
      r1.contains(Vector2D(51, 1)) should equal (false)
      r1.contains(Vector2D(50, 51)) should equal (false)
      r1.contains(Vector2D(-50, -50)) should equal (true)
      r1.contains(Vector2D(-1000, -50)) should equal (false)
      r4.contains(Vector2D(50, 50)) should equal (true)
      r4.contains(Vector2D(-50, -50)) should equal (true)
      r4.contains(Vector2D(-51, -50)) should equal (false)
      r4.contains(Vector2D(50, 51)) should equal (false)
    }

    it ("can be transformed") {
      // Translation
      r1.transform(TransformationMatrix(Vector2D(10, 0), 1)) should equal(
        ComplexRectangle2D(Vector2D(10, 0), 100, 100, 0))
      r1.transform(TransformationMatrix(Vector2D(0, -10), 1)) should equal(
        ComplexRectangle2D(Vector2D(0, -10), 100, 100, 0))

      // Scale
      r1.transform(TransformationMatrix(Vector2D(0, 0), 0.5)) should equal(
        ComplexRectangle2D(Vector2D(0, 0), 50, 50, 0))
      r1.transform(TransformationMatrix(Vector2D(0, 0), 1).scale(2, 1, Vector2D(0, 0))) should equal( // X
        ComplexRectangle2D(Vector2D(0, 0), 200, 100, 0))
      r1.transform(TransformationMatrix(Vector2D(0, 0), 1).scale(1, 2, Vector2D(0, 0))) should equal( // Y
        ComplexRectangle2D(Vector2D(0, 0), 100, 200, 0))

      // Rotation
      r1.transform(TransformationMatrix(Vector2D(0, 0), 1).rotate(180)) should equal(
        ComplexRectangle2D(Vector2D(0, 0), 100, 100, 180))
      r1.transform(TransformationMatrix(Vector2D(10, 0), 1).rotate(-10)) should equal(
        ComplexRectangle2D(Vector2D(10, 0), 99.99999999999999, 99.99999999999999, 350))
      r4.transform(TransformationMatrix(Vector2D(0, 0), 1).rotate(180)) should equal(
        ComplexRectangle2D(Vector2D(0, 0), 100, 100, 0))

    }

    /****** INTERSECTIONS ********/

    it ("can calculate rectangle-arc intersections") {
      val a = Arc2D(Vector2D(0, 0), 50, 0, 180)
      r1.intersections(a) should equal(Set(Vector2D(-50, 0), Vector2D(50, 0), Vector2D(0, 50)))
    }

    it ("can calculate rectangle-circle intersections") {
      val circle = Circle2D(Vector2D(-50,-100),100)
      r1.intersections(circle) should equal (Set(Vector2D(-50,0), Vector2D(36.60254037844386,-50.0)))
    }

  }
}
