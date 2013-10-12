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
import collection.mutable.ArrayBuffer

/**
 * Test an arc.
 */
class Arc2DSpec  extends FunSuite with ShouldMatchers {
  /*
  test("Calculate the angle-span of an arc from two given angles") {
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

  test("Create an arc from three points on the periphery") {
    val p1 = Vector2D(0, 1)
    val p2 = Vector2D(1, 0)
    val p3 = Vector2D(0, -1)
    val p4 = Vector2D(math.cos(math.Pi / 4), math.sin(math.Pi / 4))
    //val p5 = Vector2D(1, 0)
    //val p6 = Vector2D(p4.x, -p4.y)

    val arc1 = Arc2D(p1, p2, p3)
    arc1 should equal (Arc2D(Vector2D(0, 0), 1, 270, 180))
    
    val arc2 = Arc2D(p3, p2, p1)
    arc2 should equal (Arc2D(Vector2D(0, 0), 1, 270, 180))
  }
  */


  test("Correct angle with correst center, radius, start angle, angle and end angle from 3 points: ") {
    val p1 = Vector2D(-10, 0)
    val p2 = Vector2D(0, 10)
    val p3 = Vector2D(10, 0)

    val arc = Arc2D(p1, p2, p3)
    val arc2 = Arc2D(p2,p1,p3)
    val arc3 = Arc2D(p3,p2,p1)

    arc.center should equal (Vector2D(0,0))
    arc.radius should equal (10)
    arc.startAngle should equal (0)
    arc.angle should equal (180)
    arc.endAngle should equal (180)
    arc2.center should equal (Vector2D(0,0))
    arc2.radius should equal (10)
    arc2.startAngle should equal (90)
    arc2.angle should equal (270)
    arc2.endAngle should equal (0)
    arc3.center should equal (Vector2D(0,0))
    arc3.radius should equal (10)
    arc3.startAngle should equal (0)
    arc3.angle should equal (180)
    arc3.endAngle should equal (180)

  }

  test("Can calculate the start point of an arc") {
    val p1 = Vector2D(-10, 0)
    val p2 = Vector2D(0, 10)
    val p3 = Vector2D(10, 0)

    val arc = Arc2D(p1, p2, p3)
    val arc2 = Arc2D(p2,p1,p3)
    val arc3 = Arc2D(p3,p2,p1)
    //val arc3 = Arc2D(Vector2D(30,20),Vector2D(22.9289,37.0711),Vector2D(40,30))


    arc.startPoint should equal (p3)
    arc2.startPoint should equal (p2)
    arc3.startPoint should equal (p3)

    //arc2.startPoint should equal (p2)
    //arc2.endPoint should equal (p3)
    //arc2.midPoint should equal (Vector2D(-7.07107,-7.07107))

    //arc with center in 30,30
    //arc3.startPoint should equal (Vector2D(29.99998,19.99996))
    //arc3.endPoint should equal (Vector2D(40,30))
    //arc3.midPoint should equal (Vector2D(22.9289,37.0711))
  }

  test("Can calculate the mid points of an arc") {
    val p1 = Vector2D(-10, 0)
    val p2 = Vector2D(0, 10)
    val p3 = Vector2D(10, 0)

    val arc = Arc2D(p1, p2, p3)
    val arc2 = Arc2D(p2,p1,p3)
    val arc3 = Arc2D(Vector2D(30,20),Vector2D(22.9289,37.0711),Vector2D(40,30))

    arc.midPoint should equal (p2)
    arc2.midPoint should equal (Vector2D(-7.07107,-7.07107))
    arc3.midPoint should equal (Vector2D(22.9289,37.0711))

  }

  test("Can calculate the end points of an arc") {
    val p1 = Vector2D(-10, 0)
    val p2 = Vector2D(0, 10)
    val p3 = Vector2D(10, 0)

    val arc = Arc2D(p1, p2, p3)
    val arc2 = Arc2D(p2,p1,p3)
    val arc3 = Arc2D(Vector2D(30,20),Vector2D(22.9289,37.0711),Vector2D(40,30))

    arc.endPoint should equal (p1)
    arc2.endPoint should equal (p3)
    arc3.endPoint should equal (Vector2D(30,20))

    //arc2.startPoint should equal (p2)
    //arc2.endPoint should equal (p3)
    //arc2.midPoint should equal (Vector2D(-7.07107,-7.07107))

    //arc with center in 30,30
    //arc3.startPoint should equal (Vector2D(29.99998,19.99996))
    //arc3.endPoint should equal (Vector2D(40,30))
    //arc3.midPoint should equal (Vector2D(22.9289,37.0711))
  }

  test("Can tell if an Arc2D and a segment2D intersect") {
    val p1 = Vector2D(-10, 0)
    val p2 = Vector2D(0, 10)
    val p3 = Vector2D(10, 0)
    val arc = Arc2D(p1, p2, p3)

    val p4 = Vector2D(-14.142135624,0)
    val p5 = Vector2D(0, 14.142135624)
    val p6 = Vector2D(14.142135624,0)
    val arc2 = Arc2D(p4, p5, p6)

    val l1 = Segment2D(Vector2D(0,40),Vector2D(10,45))
    val l2 = Segment2D(Vector2D(-20,-20),Vector2D(20,20))
    val l3 = Segment2D(Vector2D(-20,10),Vector2D(20,10))
    val l4 = Segment2D(Vector2D(-20,20),Vector2D(0,0))
    val l5 = Segment2D(Vector2D(-20,-20),Vector2D(0,0))
    val l6 = Segment2D(Vector2D(20,5),Vector2D(-20,5))
    val l7 = Segment2D(Vector2D(14.142135624,0),Vector2D(7.07107,7.07107))
    val l8 = Segment2D(Vector2D(20,0),Vector2D(10,10))
    val l9 = Segment2D(Vector2D(0,0),Vector2D(20,0))

    //arc.intersects(l1) should equal (false)
    //arc.intersects(l2) should equal (true) //should have one intersection only.
    //arc.intersects(l3) should equal (true) //line segment tangent to the arc.
    //arc.intersects(l4) should equal (true) //should have one intersection only.
    //arc.intersects(l5) should equal (false) //should not have an intersection.
    //arc.intersects(l6) should equal (true) //should have two intersections.
    //arc.intersects(l7) should equal (true) //should have one intersection.

    //arc2.intersects(l8) should equal (true) //should have one intersection.
    //arc2.intersects(l9) should equal (true) //should have one intersection.
  }
  test("Can calculate intersections between an Arc2D and a segment2D") {
    val p1 = Vector2D(-10, 0)
    val p2 = Vector2D(0, 10)
    val p3 = Vector2D(10, 0)
    val arc = Arc2D(p1, p2, p3)

    val p4 = Vector2D(-14.142135624,0)
    val p5 = Vector2D(0, 14.142135624)
    val p6 = Vector2D(14.142135624,0)
    val arc2 = Arc2D(p4, p5, p6)

    val l1 = Segment2D(Vector2D(0,40),Vector2D(10,45))
    val l2 = Segment2D(Vector2D(-20,-20),Vector2D(20,20))
    val l3 = Segment2D(Vector2D(-20,10),Vector2D(20,10))
    val l4 = Segment2D(Vector2D(-20,-20),Vector2D(0,0))
    val l5 = Segment2D(Vector2D(20,5),Vector2D(-20,5))
    val l6 = Segment2D(Vector2D(-10,0),Vector2D(-20,0))
    val l7 = Segment2D(Vector2D(14.142135624,0),Vector2D(7.071067811865475,7.071067811865475))
    val l8 = Segment2D(Vector2D(10,20),Vector2D(0,14.142135624))

    /*arc.intersections(l1) should equal (Set())
    arc.intersections(l2) should equal (Set(Vector2D(7.0712,7.0712))) //one intersection only.
    /arc.intersections(l3) should equal (Set(Vector2D(0,10)))//TODO: check that the tangent is on the arc
    arc.intersections(l4) should equal (Set()) //a segment which crosses the circle outside the arc and ends inside the arc should have zero intersections
    arc.intersections(l5) should equal (Set(Vector2D(-8.6604,5.0), Vector2D(8.6604,5.0)))
    arc.intersections(l6) should equal (Set(Vector2D(-10,0)))
    arc.intersections(l7) should equal (Set(Vector2D(7.07107,7.07107)))
    arc2.intersections(l8) should equal (Set(Vector2D(0.0,14.14214)))
    */
  }
  test("Can calculate intersections between an Arc2D and a collectionGeom2D") {
    val p1 = Vector2D(0, -10)
    val p2 = Vector2D(10, 0)
    val p3 = Vector2D(0, 10)
    val arc = Arc2D(p1, p2, p3)

    val p9 = Vector2D(-14.142135624,0)
    val p10 = Vector2D(0, 14.142135624)
    val p11 = Vector2D(14.142135624,0)
    val arc2 = Arc2D(p9, p10, p11)

    //c1
    val p4 = Vector2D(0,0)
    val p5 = Vector2D(20,0)
    val p6 = Vector2D(10,10)
    val p7 = Vector2D(10,20)
    val p8 = Vector2D(0,14.142135624)

    //c2
    val p12 = Vector2D(-10,-30)
    val p13 = Vector2D(-10,20)
    val p14 = Vector2D(20,-20)


    val c1 = CollectionGeometry2D(ArrayBuffer(Segment2D(p4,p5),Segment2D(p5,p6),Segment2D(p6,p7),Segment2D(p7,p8)))
    val c2 = CollectionGeometry2D(ArrayBuffer(Segment2D(p12,p13),Segment2D(p13,p14)))

    //arc.intersections(c1) should equal (Set(Vector2D(10.0,0.0)))
    //arc2.intersections(c1) should equal (Set(Vector2D(14.1422,0.0), Vector2D(10.0001,9.9999), Vector2D(10.0,10.0), Vector2D(0.0,14.14214)))
    //arc2.intersections(c2) should equal (Set(Vector2D(-10.0,10.0), Vector2D(-4.9387,13.2516)))
  }

  test("Can calculate distance to point") {
    val p1 = Vector2D(-10, 0)
    val p2 = Vector2D(0, 10)
    val p3 = Vector2D(10, 0)

    val pa = Vector2D(0, 0)
    val pb = Vector2D(1, 0)
    val pc = Vector2D(10,0)
    val pd = Vector2D(100,0)

    val arc = Arc2D(p1, p2, p3)

    arc.distanceTo(pa) should equal (10)
    arc.distanceTo(pb) should equal (9)
    arc.distanceTo(pc) should equal (0)
    arc.distanceTo(pd) should equal (90)

  }


}