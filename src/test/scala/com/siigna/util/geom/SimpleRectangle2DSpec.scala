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

package com.siigna.util.geom

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

class SimpleRectangle2DSpec extends FunSpec with ShouldMatchers {

  describe("A SimpleRectangle2D") {

    val r = SimpleRectangle2D(0, 0, 10, 10)

    /****** INTERSECTION ********/
    it ("can determine if it intersects with a complex rectangle") {
      r.intersects(ComplexRectangle2D(Vector2D(5, 5), 5, 5, 0)) should equal (true)
      r.intersects(ComplexRectangle2D(Vector2D(5, 5), 4, 4, 0)) should equal (false)
      r.intersects(ComplexRectangle2D(Vector2D(7, 5), 4, 4, 0)) should equal (true)
      r.intersects(ComplexRectangle2D(Vector2D(5, 5), 6, 4, 90)) should equal (true)
      r.intersects(ComplexRectangle2D(Vector2D(5, 5), 5, 5, 45)) should equal (true)
    }

    /****** INTERSECTIONS ********/
    it ("can calculate rectangle-arc intersections") {
      val a = Arc2D(Vector2D(0, 0), 10, 0, 180)
      val r = SimpleRectangle2D(-10, -10, 10, 10)
      r.intersections(a) should equal(Set(Vector2D(-10, 0), Vector2D(10, 0), Vector2D(0, 10)))
    }

  }
}
