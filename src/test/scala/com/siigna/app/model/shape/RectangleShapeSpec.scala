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

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.siigna.util.geom._
import com.siigna.util.collection.Attributes
import com.siigna.app.model.selection.{EmptyShapeSelector, FullShapeSelector, ShapeSelector}

/**
 * Tests the [[com.siigna.app.model.shape.RectangleShape]] class.
 */
class RectangleShapeSpec extends FunSpec with ShouldMatchers {

  describe("A RectangleShape") {

    val r = RectangleShape(Vector2D(0, 0), 100, 100, 0) // Test instance
    val s = RectangleShape(Vector2D(0, 0), 100, 100, 45) // Test instance
    val t = TransformationMatrix(Vector2D(50, 50))

    it("can be created without attributes") {
      new RectangleShape(Vector2D(0, 0), 100, 100, 0, Attributes()) should equal (RectangleShape(Vector2D(0,0),100,100,0))
    }

    it("can be created from four coordinates") {
      new RectangleShape(Vector2D(50, 50), 100, 100, 0, Attributes()) should equal (RectangleShape(0, 0, 100, 100))
    }

    it("can be created from two vectors") {
      new RectangleShape(Vector2D(50, 50), 100, 100, 0, Attributes()) should equal (RectangleShape(Vector2D(0, 0), Vector2D(100, 100)))
    }

    it ("can delete a part of a rectangle") {
      // One point
      r.delete(ShapeSelector(0)) should equal(Seq(PolylineShape(r.p1, r.p2, r.p3)))
      r.delete(ShapeSelector(1)) should equal(Seq(PolylineShape(r.p2, r.p3, r.p0)))
      r.delete(ShapeSelector(2)) should equal(Seq(PolylineShape(r.p3, r.p0, r.p1)))
      r.delete(ShapeSelector(3)) should equal(Seq(PolylineShape(r.p0, r.p1, r.p2)))

      // Two points
      r.delete(ShapeSelector(0, 1)) should equal(Seq(LineShape(r.p2, r.p3)))
      r.delete(ShapeSelector(2, 3)) should equal(Seq(LineShape(r.p0, r.p1)))
      r.delete(ShapeSelector(3, 0)) should equal(Seq(LineShape(r.p1, r.p2)))
      // Two points: opposite
      r.delete(ShapeSelector(0, 2)) should equal(Seq())
      r.delete(ShapeSelector(1, 3)) should equal(Seq())

      // Three points
      r.delete(ShapeSelector(0, 1, 2)) should equal(Seq())
      r.delete(ShapeSelector(1, 2, 3)) should equal(Seq())
      r.delete(ShapeSelector(0, 2, 3)) should equal(Seq())
      r.delete(ShapeSelector(0, 1, 3)) should equal(Seq())
    }

    it ("can return an empty partial shape from an empty selector") {
      val empty = ShapeSelector() // Same as EmptyShapeSelector

      // No selection means no partial shape
      r.getPart(empty) should equal(None)
    }

    it ("can transform a single point of the rectangle from a given selector") {
      val s0  = ShapeSelector(0)
      val s1  = ShapeSelector(1)
      val s2  = ShapeSelector(2)
      val s3  = ShapeSelector(3)

      val x0 = r.getPart(s0).get.apply(t).asInstanceOf[RectangleShape]
      x0.geometry.p0 should equal (Vector2D(100, 100))
      x0.geometry.p1 should equal (Vector2D(-50, 100))
      x0.geometry.p2 should equal (Vector2D(-50, -50))
      x0.geometry.p3 should equal (Vector2D(100, -50))

      val x1 = r.getPart(s1).get.apply(t).asInstanceOf[RectangleShape]
      x1.geometry.p0 should equal (Vector2D( 50, 100))
      x1.geometry.p1 should equal (Vector2D(  0, 100))
      x1.geometry.p2 should equal (Vector2D(  0, -50))
      x1.geometry.p3 should equal (Vector2D( 50, -50))

      val x2 = r.getPart(s2).get.apply(t).asInstanceOf[RectangleShape]
      x2.geometry.p0 should equal (Vector2D( 50,  50))
      x2.geometry.p1 should equal (Vector2D(  0,  50))
      x2.geometry.p2 should equal (Vector2D(  0,   0))
      x2.geometry.p3 should equal (Vector2D( 50,   0))

      val x3 = r.getPart(s3).get.apply(t).asInstanceOf[RectangleShape]
      x3.geometry.p0 should equal (Vector2D(100,  50))
      x3.geometry.p1 should equal (Vector2D(-50,  50))
      x3.geometry.p2 should equal (Vector2D(-50,   0))
      x3.geometry.p3 should equal (Vector2D(100,   0))
    }

    it ("can transform two points from a given selection") {
      val s01 = ShapeSelector(0, 1)
      val s02 = ShapeSelector(0, 2)
      val s03 = ShapeSelector(0, 3)
      val s12 = ShapeSelector(1, 2)
      val s13 = ShapeSelector(1, 3)
      val s23 = ShapeSelector(2, 3)

      // cannot select across
      r.getPart(s02) should equal(None)
      r.getPart(s13) should equal(None)

      // select lines - should restrict movement to one axis
      val x01 = r.getPart(s01).get.apply(t).asInstanceOf[RectangleShape]
      x01.geometry.p0 should equal(Vector2D(50d, 100d))
      x01.geometry.p1 should equal(Vector2D(-50d, 100d))
      x01.geometry.p2 should equal(Vector2D(-50d, -50d))
      x01.geometry.p3 should equal(Vector2D(50d, -50d))

      val x03 = r.getPart(s03).get.apply(t).asInstanceOf[RectangleShape]
      x03.geometry.p0 should equal(Vector2D(100, 50))
      x03.geometry.p1 should equal(Vector2D(-50, 50))
      x03.geometry.p2 should equal(Vector2D(-50, -50))
      x03.geometry.p3 should equal(Vector2D(100, -50))

      val x12 = r.getPart(s12).get.apply(t).asInstanceOf[RectangleShape]
      x12.geometry.p0 should equal(Vector2D(50, 50))
      x12.geometry.p1 should equal(Vector2D(0, 50))
      x12.geometry.p2 should equal(Vector2D(0, -50))
      x12.geometry.p3 should equal(Vector2D(50, -50))

      val x23 = r.getPart(s23).get.apply(t).asInstanceOf[RectangleShape]
      x23.geometry.p0 should equal(Vector2D( 50, 50))
      x23.geometry.p1 should equal(Vector2D(-50, 50))
      x23.geometry.p2 should equal(Vector2D(-50,  0))
      x23.geometry.p3 should equal(Vector2D( 50,  0))
    }

    it ("can transform three points from a given selection") {
      val s012  = ShapeSelector(0, 1, 2)
      val s013  = ShapeSelector(0, 1, 3)
      val s023  = ShapeSelector(0, 2, 3)
      val s123  = ShapeSelector(1, 2, 3)

      val x012 = r.getPart(s012).get.apply(t).asInstanceOf[RectangleShape]
      x012.geometry.p0 should equal (Vector2D(100, 100))
      x012.geometry.p1 should equal (Vector2D(  0, 100))
      x012.geometry.p2 should equal (Vector2D(  0,   0))
      x012.geometry.p3 should equal (Vector2D(100,   0))

      val x013 = r.getPart(s013).get.apply(t).asInstanceOf[RectangleShape]
      x013.geometry.p0 should equal (Vector2D(100, 100))
      x013.geometry.p1 should equal (Vector2D(  0, 100))
      x013.geometry.p2 should equal (Vector2D(  0,   0))
      x013.geometry.p3 should equal (Vector2D(100,   0))

      val x023 = r.getPart(s023).get.apply(t).asInstanceOf[RectangleShape]
      x023.geometry.p0 should equal (Vector2D(100, 100))
      x023.geometry.p1 should equal (Vector2D(  0, 100))
      x023.geometry.p2 should equal (Vector2D(  0,   0))
      x023.geometry.p3 should equal (Vector2D(100,   0))

      val x123 = r.getPart(s123).get.apply(t).asInstanceOf[RectangleShape]
      x123.geometry.p0 should equal (Vector2D(100, 100))
      x123.geometry.p1 should equal (Vector2D(  0, 100))
      x123.geometry.p2 should equal (Vector2D(  0,   0))
      x123.geometry.p3 should equal (Vector2D(100,   0))
    }


    it ("can transform the entire shape from a given selection") {
      val s1 = ShapeSelector(0, 1, 2, 3)
      val s2 = FullShapeSelector

      val x1 = r.getPart(s1).get.apply(t).asInstanceOf[RectangleShape]
      x1 should equal(RectangleShape(Vector2D(50, 50), 100, 100, 0))

      val x2 = r.getPart(s2).get.apply(t).asInstanceOf[RectangleShape]
      x2 should equal(RectangleShape(Vector2D(50, 50), 100, 100, 0))
    }


    it("can be selected by a point") {
      // Vertices
      r.getSelector(Vector2D(50,50))   should equal (ShapeSelector(0))
      r.getSelector(Vector2D(-50,50))  should equal (ShapeSelector(1))
      r.getSelector(Vector2D(-50,-50)) should equal (ShapeSelector(2))
      r.getSelector(Vector2D(50,-50))  should equal (ShapeSelector(3))

      // Segments
      r.getSelector(Vector2D(  0, 50)) should equal (ShapeSelector(0, 1))
      r.getSelector(Vector2D(-50,  0)) should equal (ShapeSelector(1, 2))
      r.getSelector(Vector2D(  0,-50)) should equal (ShapeSelector(2, 3))
      r.getSelector(Vector2D( 50,  0)) should equal (ShapeSelector(0, 3))

      // Rotated
      val pTR = Vector2D(20,20)
      val pTL = Vector2D(-20,20)
      val pBL = Vector2D(-20,-20)
      val pBR = Vector2D(20,-20)
      val w = 40.0
      val h = 40.0
      val center = Vector2D(0,0)
      val x = RectangleShape(center, w,h, -90, Attributes())

      val selector1 = x.getSelector(pTR)
      val selector2 = x.getSelector(pTL)
      val selector3 = x.getSelector(pBL)
      val selector4 = x.getSelector(pBR)

      selector1 should equal (ShapeSelector(1)) //SHOULD BE 2 IF CW ROTATION
      selector2 should equal (ShapeSelector(2))
      selector3 should equal (ShapeSelector(3))
      selector4 should equal (ShapeSelector(0)) //SHOULD BE TWO (CCW) OR 3 (CW)
    }

    it ("can be selected by a rectangle") {
      // Zero points
      r.getSelector(SimpleRectangle2D(-1, -1, 1, 1)) should equal (EmptyShapeSelector)
      r.getSelector(SimpleRectangle2D(-49.9999, -49.9999, 49.9999, 49.9999)) should equal (EmptyShapeSelector)
      // One point
      r.getSelector(SimpleRectangle2D(  0,  0, 60, 60)) should equal(ShapeSelector(0))
      r.getSelector(SimpleRectangle2D(-60,  0,  0, 60)) should equal(ShapeSelector(1))
      r.getSelector(SimpleRectangle2D(-60,-60,  0,  0)) should equal(ShapeSelector(2))
      r.getSelector(SimpleRectangle2D(  0,-60, 60,  0)) should equal(ShapeSelector(3))
      // Two points
      r.getSelector(SimpleRectangle2D(-60,  0, 60, 60)) should equal(ShapeSelector(0, 1))
      r.getSelector(SimpleRectangle2D(-60,-60,  0, 60)) should equal(ShapeSelector(1, 2))
      r.getSelector(SimpleRectangle2D(-60,-60, 60,  0)) should equal(ShapeSelector(2, 3))
      r.getSelector(SimpleRectangle2D(  0,-60, 60, 60)) should equal(ShapeSelector(0, 3))
      // Three points
      s.getSelector(SimpleRectangle2D(-1, -100, 100, 100)) should equal (ShapeSelector(0, 2, 3))
      // Four points
      r.getSelector(SimpleRectangle2D(-60, -60, 60, 60)) should equal (FullShapeSelector)
    }

    it ("can find a shape from a given selector") {
      // None
      r.getShape(EmptyShapeSelector) should equal(None)
      r.getShape(ShapeSelector(0)) should equal(None)
      r.getShape(ShapeSelector(0, 2)) should equal(None)
      r.getShape(ShapeSelector(1, 3)) should equal(None)
      r.getShape(ShapeSelector(1, 3)) should equal(None)
      // Two points
      r.getShape(ShapeSelector(0, 1)) should equal(Some(LineShape(r.p0, r.p1)))
      r.getShape(ShapeSelector(1, 2)) should equal(Some(LineShape(r.p1, r.p2)))
      r.getShape(ShapeSelector(2, 3)) should equal(Some(LineShape(r.p2, r.p3)))
      r.getShape(ShapeSelector(0, 3)) should equal(Some(LineShape(r.p0, r.p3)))
      // Three points
      r.getShape(ShapeSelector(0, 1, 2)) should equal(Some(PolylineShape(r.p0, r.p1, r.p2)))
      r.getShape(ShapeSelector(0, 1, 3)) should equal(Some(PolylineShape(r.p3, r.p0, r.p1)))
      r.getShape(ShapeSelector(0, 2, 3)) should equal(Some(PolylineShape(r.p2, r.p3, r.p0)))
      r.getShape(ShapeSelector(1, 2, 3)) should equal(Some(PolylineShape(r.p1, r.p2, r.p3)))
      // Full
      r.getShape(ShapeSelector(0, 1, 2, 3)) should equal(Some(r))
      r.getShape(FullShapeSelector) should equal(Some(r))
    }

    it ("can find the selected vertices from a selector") {
      // Zero
      r.getVertices(EmptyShapeSelector) should equal(Seq())
      // One
      r.getVertices(ShapeSelector(0)) should equal(Seq(r.geometry.vertices(0)))
      r.getVertices(ShapeSelector(1)) should equal(Seq(r.geometry.vertices(1)))
      r.getVertices(ShapeSelector(2)) should equal(Seq(r.geometry.vertices(2)))
      r.getVertices(ShapeSelector(3)) should equal(Seq(r.geometry.vertices(3)))
      // Two
      r.getVertices(ShapeSelector(0, 1)) should equal(Seq(r.geometry.vertices(0), r.geometry.vertices(1)))
      r.getVertices(ShapeSelector(0, 2)) should equal(Seq(r.geometry.vertices(0), r.geometry.vertices(2)))
      r.getVertices(ShapeSelector(0, 3)) should equal(Seq(r.geometry.vertices(0), r.geometry.vertices(3)))
      r.getVertices(ShapeSelector(1, 2)) should equal(Seq(r.geometry.vertices(1), r.geometry.vertices(2)))
      r.getVertices(ShapeSelector(1, 3)) should equal(Seq(r.geometry.vertices(1), r.geometry.vertices(3)))
      r.getVertices(ShapeSelector(2, 3)) should equal(Seq(r.geometry.vertices(2), r.geometry.vertices(3)))
      // Three
      r.getVertices(ShapeSelector(0, 1, 2)) should equal(r.geometry.vertices.take(3))
      r.getVertices(ShapeSelector(0, 1, 3)) should equal(r.geometry.vertices.zipWithIndex.filter(_._2 != 2).map(_._1))
      r.getVertices(ShapeSelector(0, 2, 3)) should equal(r.geometry.vertices.zipWithIndex.filter(_._2 != 1).map(_._1))
      r.getVertices(ShapeSelector(1, 2, 3)) should equal(r.geometry.vertices.tail)
      // Four
      r.getVertices(ShapeSelector(0, 1, 2, 3)) should equal(r.geometry.vertices)
      r.getVertices(FullShapeSelector) should equal(r.geometry.vertices)
    }

    it("can be transformed") {
      // Translation
      val t1 = TransformationMatrix(Vector2D(10, 10), 1)
      r.transform(t1) should equal (RectangleShape(-40, -40, 60, 60))

      // Rotation
      val t2 = TransformationMatrix(Vector2D(0,0),1).rotate(90)
      r.transform(t2) should equal(new RectangleShape(Vector2D(0, 0), 100, 100, 90, Attributes()))

      // Scaling
      val t3 = TransformationMatrix(Vector2D(0,0),2)
      r.transform(t3) should equal(new RectangleShape(Vector2D(0, 0), 200, 200, 0, Attributes()))
      val t4 = TransformationMatrix(Vector2D(0,0),1).scale(2, 1, Vector2D(0, 0))
      r.transform(t4) should equal(new RectangleShape(Vector2D(0, 0), 200, 100, 0, Attributes()))
      val t5 = TransformationMatrix(Vector2D(0,0),1).scale(1, 2, Vector2D(0, 0))
      r.transform(t5) should equal(new RectangleShape(Vector2D(0, 0), 100, 200, 0, Attributes()))
    }

  }
}