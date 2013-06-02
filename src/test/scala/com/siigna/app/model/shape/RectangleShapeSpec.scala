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

package com.siigna.app.model.shape

import org.scalatest.path.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.siigna.util.geom._
import com.siigna.util.collection.Attributes
import com.siigna.app.model.selection.ShapeSelector

/**
 * Tests the [[com.siigna.app.model.shape.RectangleShape]] class.
 */
class RectangleShapeSpec extends FunSpec with ShouldMatchers {

  describe("A RectangleShape") {
    /*
    it("can be rotated") {
      val s = RectangleShape(Vector2D(0,0),100,100,0, Attributes())
      val t = TransformationMatrix(Vector2D(0,0),1).rotate(90)
      s.transform(t) should equal(new RectangleShape(Vector2D(0, 0), 100, 100, 90, Attributes()))
    }
    */

    /*

    it("can be selected by a point") {
      val pTR = Vector2D(20,20)
      val pTL = Vector2D(-20,20)
      val pBR = Vector2D(20,-20)
      val pBL = Vector2D(-20,-20)
      val w = 40.0
      val h = 40.0
      val center = Vector2D(0,0)
      val r = RectangleShape(center, w,h, 0, Attributes())

      val selector1 = r.getSelector(pTR)
      val selector2 = r.getSelector(pTL)
      val selector3 = r.getSelector(pBR)
      val selector4 = r.getSelector(pBL)

      /*

       1   0
       *   *

       *   *
       2   3

       */

      selector1 should equal (ShapeSelector(1))
      selector2 should equal (ShapeSelector(0))
      selector3 should equal (ShapeSelector(2))
      selector4 should equal (ShapeSelector(3))
    }

    it("can part select a rotated rectangle") {
      val pTR = Vector2D(20,20)
      val pTL = Vector2D(-20,20)
      val pBR = Vector2D(20,-20)
      val pBL = Vector2D(-20,-20)
      val w = 40.0
      val h = 40.0
      val center = Vector2D(0,0)
      val r = RectangleShape(center, w,h, -90, Attributes())

      val selector1 = r.getSelector(pTR)
      val selector2 = r.getSelector(pTL)
      val selector3 = r.getSelector(pBR)
      val selector4 = r.getSelector(pBL)

      selector1 should equal (ShapeSelector(0)) //SHOULD BE 2 IF CW ROTATION
      selector2 should equal (ShapeSelector(3))
      selector3 should equal (ShapeSelector(1)) //SHOULD BE TWO (CCW) OR 3 (CW)
      selector4 should equal (ShapeSelector(2))
    }

    */

    it("can be created without attributes") {
      new RectangleShape(Vector2D(0, 0), 100, 100, 0, Attributes()) should equal (RectangleShape(Vector2D(0,0),100,100,0))
    }

    it("can be created from 4 coordinates") {
      new RectangleShape(Vector2D(50, 50), 100, 100, 0, Attributes()) should equal (RectangleShape(0, 0, 100, 100))
    }

    it("can be moved") {
      val s = RectangleShape(0, 0, 100, 100)
      val t = TransformationMatrix(Vector2D(10, 10), 1)
      s.transform(t) should equal (RectangleShape(10, 10, 110, 110))
    }

    it("can be rotated") {
      val s = RectangleShape(0, 0, 100, 100)
      val t = TransformationMatrix(Vector2D(50, 50), 1).rotate(90)
      s.transform(t) should equal(new RectangleShape(Vector2D(50, 50), 100, 100, 90, Attributes()))
    }
  }
}



