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
import org.scalatest.{FunSuite, FlatSpec}

/**
 * Test an arc.
 */
class Arc2DSpec  extends FunSuite with ShouldMatchers {

  test("Find a center point in an arc given three points on the periphery. -OK") {
    val p1 = Vector2D(-5, 0)
    val p2 = Vector2D(0, 5)
    val p3 = Vector2D(5, 0)

    Arc2D.findCenterPoint(p1, p2, p3) should equal (Vector2D(0, 0))
  }

  test("Calculate the angle-span of an arc from two given angles -OK") {
    val a1 = 90
    val a2 = 270
    val a3 = 45
    val a4 = 225
    val a5 = 44

    Arc2D.findArcSpan(a1, a2) should equal (180d)
    Arc2D.findArcSpan(a2, a1) should equal (180d)
    Arc2D.findArcSpan(a3, a4) should equal (180d)
    Arc2D.findArcSpan(a4, a3) should equal (180d)
    Arc2D.findArcSpan(a3, a5) should equal (359d)
    Arc2D.findArcSpan(a5, a3) should equal (1d)
  }

  test("Create an arc from three points on the periphery -OK") {
    val p1 = Vector2D(0, 1)
    val p2 = Vector2D(1, 0)
    val p3 = Vector2D(0, -1)
    val p4 = Vector2D(math.cos(math.Pi / 4), math.sin(math.Pi / 4))
    val p5 = Vector2D(1, 0)
    val p6 = Vector2D(p4.x, -p4.y)

    val arc1 = Arc2D(p1, p2, p3)
    arc1 should equal (Arc2D(Vector2D(0, 0), 1, 270, 180))
    
    val arc2 = Arc2D(p3, p2, p1)
    arc2 should equal (Arc2D(Vector2D(0, 0), 1, 270, 180))
  }
  test("Can tell if an Arc2D and a segment2D intersect") {

    val p1 = Vector2D(-10, 0)
    val p2 = Vector2D(0, 10)
    val p3 = Vector2D(10, 0)
    val arc = Arc2D(p1, p2, p3)
    val l1 = Segment2D(Vector2D(0,40),Vector2D(10,45))
    val l2 = Segment2D(Vector2D(-20,-20),Vector2D(20,20))
    val l3 = Segment2D(Vector2D(-20,10),Vector2D(20,10))

    arc.intersects(l1) should equal (false)
    arc.intersects(l2) should equal (true)
    arc.intersects(l3) should equal (true) //line segment tangent to the arc.
  }
}