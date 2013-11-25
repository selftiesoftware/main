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
 * Test a finite line segment.
 */
class Segment2DSpec  extends FunSuite with ShouldMatchers {

  test("Can tell if two finite line segments intersect") {

    val l1 = Segment2D(Vector2D(0,20),Vector2D(0,-20))
    val l2 = Segment2D(Vector2D(-20,0),Vector2D(20,0))
    val l3 = Segment2D(Vector2D(-20,0),Vector2D(0,0))
    val l4 = Segment2D(Vector2D(-20,0),Vector2D(-1,0))
    val l5 = Segment2D(Vector2D(0,0),Vector2D(0,20))

    l1.intersects(l1) should equal (false)  // TODO: should two identical lines intersect?
    l1.intersects(l2) should equal (true)
    l1.intersects(l3) should equal (true)  //should equal true because l3 touches l1.
    l1.intersects(l4) should equal (false)
    //l1.intersects(l5) should equal (false) //  TODO: should two lines on top of each other intersect?
  }
  test("Can tell if a Segment2D and a SimpleRectangle2D intersect") {
    val l1 = Segment2D(Vector2D(0,20),Vector2D(0,-20))
    val r1 = SimpleRectangle2D(-10,-10,10,10)
    val r2 = SimpleRectangle2D(-22,-22,22,22)
    val r3 = SimpleRectangle2D(0,0,10,10)
    val r4 = SimpleRectangle2D(-40,40,0,20)

    l1.intersects(r1) should equal (true)
    l1.intersects(r2) should equal (false)
    l1.intersects(r3) should equal (true)
    l1.intersects(r4) should equal (true)
  }
  test("can calculate the intersection of two line segments") {
    val l1 = Segment2D(Vector2D(0,20),Vector2D(0,-20))
    val l2 = Segment2D(Vector2D(-10,0),Vector2D(10,0))
    val l3 = Segment2D(Vector2D(0,20),Vector2D(40,20))
    val l4 = Segment2D(Vector2D(-40,10),Vector2D(-30,-10))
    val l5 = Segment2D(Vector2D(-40,0),Vector2D(-30,0))

    l1.intersections(l2) should equal (Set(Vector2D(0,0)))
    l1.intersections(l3) should equal (Set(Vector2D(0,20)))
    l1.intersections(l4) should equal (Set()) //should give no intersection
    l5.intersections(l1) should equal (Set())   //ditto
    l1.intersections(l5) should equal (Set())
    l1.intersections(l1) should equal (Set())   //coinciding segments should not give an intersection

  }
}