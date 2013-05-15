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

//import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSuite, FlatSpec}
import collection.mutable.ArrayBuffer

/**
 * Tests the CollectionGeometry2D
 */
//class CollectionGeometry2DSpec extends FunSuite with FunSpec with ShouldMatchers {
class CollectionGeometry2DSpec extends FunSuite with ShouldMatchers {

  /*
  describe ("CollectionGeometry2D") {

    def G(geoms : Geometry2D*) = new CollectionGeometry2D(geoms)

    it("can calculate the boundary") {
      G(Segment2D(0, 0, 10, 10)).boundary should equal (SimpleRectangle2D(0, 0, 10, 10))
      G(Segment2D(-10, -10, 0, 0)).boundary should equal (SimpleRectangle2D(-10, -10, 0, 0))

      G(Segment2D(-10, -10, 0, 0), Segment2D(10, 10, 0, 0)).boundary should equal(SimpleRectangle2D(-10, -10, 10, 10))
    }

  }
  */
  test("Can tell if a CollectionGeometry and a a Segment2D intersect") {

    val p1 = Vector2D(-20,  0)
    val p2 = Vector2D(10,  20)
    val p3 = Vector2D(20,  10)

    val c = CollectionGeometry2D(ArrayBuffer(Segment2D(p1,p2),Segment2D(p2,p3)))

    val s1 = Segment2D(Vector2D(0,20),Vector2D(0,-20))
    val s2 = Segment2D(Vector2D(-30,20),Vector2D(-30,-20))
    val s3 = Segment2D(Vector2D(-20,0),Vector2D(-30,-20))

    c.intersects(s1) should equal (true)
    c.intersects(s2) should equal (false)
    c.intersects(s3) should equal (true) //when the ends of a segment and collectionGeom coincide, there should be one point of intersection
  }
  test("Can tell if a CollectionGeometry and a CollectionGeometry intersect") {

    val p1 = Vector2D(-20,  0)
    val p2 = Vector2D(10,  20)
    val p3 = Vector2D(20,  10)

    val p4 = Vector2D(-20, 20)
    val p5 = Vector2D( 0,   0)
    val p6 = Vector2D(20, -10)

    val p7 = Vector2D(-20,-50)
    val p8 = Vector2D(-50,-20)
    val p9 = Vector2D(-20,100)

    val c1 = CollectionGeometry2D(ArrayBuffer(Segment2D(p1,p2),Segment2D(p2,p3)))
    val c2 = CollectionGeometry2D(ArrayBuffer(Segment2D(p4,p5),Segment2D(p5,p6)))
    val c3 = CollectionGeometry2D(ArrayBuffer(Segment2D(p7,p8),Segment2D(p8,p9)))


    c1.intersects(c1) should equal (false)  // Two identical collectionShapes should not intersect
    c1.intersects(c2) should equal (true)
    c1.intersects(c3) should equal (false)
  }
}
