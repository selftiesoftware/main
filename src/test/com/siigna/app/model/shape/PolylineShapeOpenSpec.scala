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
import com.siigna.util.geom.Vector2D
import com.siigna.util.collection.Attributes

/**
 * Tests an open polyline
 */
class PolylineShapeOpenSpec extends FunSpec with ShouldMatchers {

  describe("An open PolylineShape") {

    val pl1 = PolylineShape(Vector2D(0, 0), Seq(new PolylineLineShape(Vector2D(10, 10))), Attributes())
    val pl2 = PolylineShape(Vector2D(0, 0), Seq(new PolylineLineShape(Vector2D(10, 10)), new PolylineLineShape(Vector2D(0, 10))), Attributes())

    it("can (re)create all the shapes inside the polyline") {
      pl1.shapes should equal(LineShape(0, 0, 10, 10))
    }

    it("can (re)create all the shapes inside the polyline") {
      pl2.shapes should equal(LineShape(0, 0, 10, 10), LineShape(10, 10, 0, 10))
    }

  }

}
