/*
 * Copyright (c) 2012. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.model.shape.polyline

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{Spec}
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.app.model.shape.{LineShape, ArcShape}

/**
 * A test for the PolylineNil case object.
 */
class PolylineArcShapeSpec extends Spec with ShouldMatchers {

  val arc1 = PolylineArcShape(Vector2D(-1, 0), Vector2D(20, 73), PolylineEndPoint(Vector2D(40, -73)))
  val arc2 = PolylineArcShape(Vector2D(80000000, -80000000), Vector2D(10000000, 1000000), PolylineLineShape(Vector2D(-400, 60), PolylineEndPoint(Vector2D(40, -73))))
  
  describe("A polyline arc") {

    it ("is not empty") { arc1.isEmpty should be(false) }

    it ("has a head and middle vector") {
      arc1.head should be(Vector2D(-1, 0))
      arc1.middle should be(Vector2D(20, 73))
      arc2.head should be(Vector2D(80000000, -80000000))
      arc2.middle should be(Vector2D(10000000, 1000000))
    }
    
    it ("can instantiate itself to a sequence of shapes") {
      arc1.toShapes should be(Seq(ArcShape(Vector2D(-1, 0), Vector2D(20, 73), Vector2D(40, -73))))
      arc2.toShapes should be(Seq(ArcShape(Vector2D(80000000, -80000000), Vector2D(10000000, 1000000), Vector2D(-400, 60)),
                                  LineShape(Vector2D(-400, 60), Vector2D(40, -73))))
    }

    it ("can be transformed with a transformationMatrix") {
      val t = TransformationMatrix(Vector2D(-100, 20), 1)
      arc1.transform(t) should be(PolylineArcShape(Vector2D(-101, 20), Vector2D(-80, 93), PolylineEndPoint(Vector2D(-60, -53))))
    }

  }

}
