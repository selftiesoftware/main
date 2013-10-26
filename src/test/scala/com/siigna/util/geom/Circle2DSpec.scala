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
import org.scalatest.FunSuite

/**
 * Test a circle.
 */
class Circle2DSpec extends FunSuite with ShouldMatchers {


  test("Find a center point in an arc given three points on the periphery.") {
    val p1 = Vector2D(-5, 0)
    val p2 = Vector2D(0, 5)
    val p3 = Vector2D(5, 0)

    Circle2D.findCenterPoint(p1, p2, p3) should equal (Vector2D(0, 0))
  }

  test("Can tell if an Circle2D and a segment2D intersect") {
    val circle = Circle2D(Vector2D(0,0),10)
    val l1 = Segment2D(Vector2D(0,40),Vector2D(10,45))
    val l2 = Segment2D(Vector2D(-20,-20),Vector2D(20,20))
    val l3 = Segment2D(Vector2D(-20,10),Vector2D(20,10))
    val l4 = Segment2D(Vector2D(-20,20),Vector2D(0,0))

    circle.intersects(l1) should equal (false)
    circle.intersects(l2) should equal (true) //should have one intersection only.
    circle.intersects(l3) should equal (true) //line segment tangent to the arc.
    circle.intersects(l4) should equal (true) //should have one intersection only.
  }
  test("Can calculate intersections between an Circle2D and a segment2D") {
    val circle = Circle2D(Vector2D(0,0),10)
    val l1 = Segment2D(Vector2D(0,40),Vector2D(10,45))
    val l2 = Segment2D(Vector2D(-20,-20),Vector2D(20,20))
    val l3 = Segment2D(Vector2D(-20,10),Vector2D(20,10))
    val l4 = Segment2D(Vector2D(-20,-20),Vector2D(0,0))
    circle.intersections(l1) should equal (Set())
    circle.intersections(l2) should equal (Set(Vector2D(-7.0710678118654755,-7.0710678118654755), Vector2D(7.0710678118654755,7.0710678118654755)))
    circle.intersections(l3) should equal (Set(Vector2D(0,10)))//TODO: tangent intersection vector coordinates are not calculated yet
    circle.intersections(l4) should equal (Set(Vector2D(-7.0710678118654755,-7.0710678118654755))) //a segment which ends inside the circle should have one point of intersection only
  }

}