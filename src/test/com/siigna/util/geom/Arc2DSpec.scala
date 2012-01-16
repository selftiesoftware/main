/*
 * Copyright (c) 2011. Siigna is released under the creative common license by-nc-sa. You are free
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

  test("Can find a center point in an arc given three points on the periphery.") {
    val f = Arc2D.findCenterPoint(_, _, _)
    val p1 = Vector2D(-5, 0)
    val p2 = Vector2D(0, 5)
    val p3 = Vector2D(5, 0)

    f(p1, p2, p3) should equal (Vector2D(0, 0))
    f(Vector2D(5, 5), Vector2D(math.hypot(5, 5), 0), Vector2D(5, -5)) should equal(Vector(0, 0))
  }

  test("Can calculate the angle-span of an arc from two given angles") {
    val f = Arc2D.findArcSpan(_, _)

    f(90, 270) should equal (180d)
    f(270, 90) should equal (180d)
    f(45, 90) should equal  (45d)
    f(90, 45) should equal  (315d)
    f(45, 225) should equal (180d)
    f(225, 45) should equal (180d)
    f(225, 90) should equal (225d)
    f(316, 19) should equal (63d)
    f(19, 316) should equal (297d)
    f(311,330) should equal (19d)
  }

  test("Can create an arc given three points on the periphery") {
    val p1 = Vector2D(0, 1)
    val p2 = Vector2D(1, 0)
    val p3 = Vector2D(0, -1)
    val p4 = Vector(5, 5)
    val p5 = Vector(math.hypot(5, 5), 0)
    val p6 = Vector(5, -5)

    Arc2D(p1, p2, p3) should equal (Arc2D(Vector2D(0, 0), 1, 270, 180))
    Arc2D(p2, p3, p1) should equal (Arc2D(Vector2D(0, 0), 1, 90, 270))
    Arc2D(p3, p2, p1) should equal (Arc2D(Vector2D(0, 0), 1, 90, 180))
    Arc2D(p2, p1, p3) should equal (Arc2D(Vector2D(0, 0), 1, 0, 270))
    Arc2D(p1, p3, p2) should equal (Arc2D(Vector2D(0, 0), 1, 90, 270))
    Arc2D(p4, p5, p6) should equal (Arc2D(Vector2D(0, 0), math.hypot(5, 5), 315, 90))
    // Hard case #1
    Arc2D(Vector2D(150,-21),Vector2D(158,0),Vector2D(150,21)).angle should equal (83.41783215831344)
  }

}