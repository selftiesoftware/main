/*
 * Copyright (c) 2013. Siigna is released under the creative common license by-nc-sa. You are free
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

/**
 * Tests the CollectionGeometry2D
 */
class CollectionGeometry2DSpec extends FunSpec with ShouldMatchers {

  describe ("CollectionGeometry2D") {

    def G(geoms : Geometry2D*) = new CollectionGeometry2D(geoms)

    it("can calculate the boundary") {
      G(Segment2D(0, 0, 10, 10)).boundary should equal (SimpleRectangle2D(0, 0, 10, 10))
      G(Segment2D(-10, -10, 0, 0)).boundary should equal (SimpleRectangle2D(-10, -10, 0, 0))

      G(Segment2D(-10, -10, 0, 0), Segment2D(10, 10, 0, 0)).boundary should equal(SimpleRectangle2D(-10, -10, 10, 10))
    }

  }

}
