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
import com.siigna.app.model.shape.RectangleShape
import com.siigna.util.collection.Attributes

class ComplexRectangle2DSpec extends FunSuite with ShouldMatchers {

  test("Can create a complex rectangle") {
    val c1 = Vector2D(0,0)
    val c2 = Vector2D(10,10)
    val width1 = 100
    val width2 = 50
    val height1 = 100
    val height2 = 100
    val rotation = 0
    val rotation2 = 30

    val r1 = ComplexRectangle2D(c1,width1,height1,rotation)
    val r2 = ComplexRectangle2D(c2,width2,height2,rotation)
    val r3 = ComplexRectangle2D(c2,width2,height2,rotation2) //rotated rectangle

    r1.topLeft should equal (Vector2D(-50, 50))
    r1.topRight should equal (Vector2D(50, 50))
    r1.bottomRight should equal (Vector2D(50,-50))
    r1.bottomLeft should equal (Vector2D(-50,-50))

    r2.topLeft should equal (Vector2D(-15, 60))
    r2.topRight should equal (Vector2D(35, 60))
    r2.bottomRight should equal (Vector2D(35,-40))
    r2.bottomLeft should equal (Vector2D(-15,-40))

    //test if rotated rectangles return the correct corner points
    r3.topLeft should equal (Vector2D(-36.65063509461097,40.80127018922194))
    r3.topRight should equal (Vector2D(56.65063509461096,40.80127018922193))
    r3.bottomRight should equal (Vector2D(6.650635094610973,-45.80127018922193))
    r3.bottomLeft should equal (Vector2D(-36.65063509461096,-20.80127018922194))

    val rShape = RectangleShape(c2,width2,height2,rotation2, Attributes())

    rShape.geometry.topLeft should equal (Vector2D(13.34936490538903,65.80127018922194))
  }
}
