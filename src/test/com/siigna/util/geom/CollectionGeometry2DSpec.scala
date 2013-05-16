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
  it("Can tell if a CollectionGeometry and a a Segment2D intersect") {

    val p1 = Vector2D(-20,  0)
    val p2 = Vector2D(10,  20)
    val p3 = Vector2D(20,  10)

    val c = CollectionGeometry2D(p1,p2,p3)
    val s1 = Segment2D(Vector2D(0,20),Vector2D(0,-20))
    val s2 = Segment2D(Vector2D(-30,20),Vector2D(-30,-20))
    val s3 = Segment2D(Vector2D(-20,0),Vector2D(-30,-20))


    c.intersects(s1) should equal (true)
    c.intersects(s2) should equal (false)
    c.intersects(s3) should equal (true) //one point of intersection
    c.intersects(c) should equal (false)  // TODO: should two identical collectionShapes intersect? - NO..?
  }
}
