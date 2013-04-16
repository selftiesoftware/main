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
import collection.mutable
import com.siigna.app.Siigna
import com.siigna.app.model.selection.EmptyShapeSelector$

/**
 * Tests an open polyline shape
 */
class PolylineShapeClosedSpec extends FunSpec with ShouldMatchers {

  describe("A closed PolylineShape") {

    val PLClosed = PolylineShape(Vector2D(0, 0), Vector2D(0, 100), Vector2D(100, 100), Vector2D(0, 0))
    val PLSmall  = PolylineShape(Vector2D(0, 0), Vector2D(-1, 0), Vector2D(0, -1), Vector2D(0, 0))

    it ("can select a single point from a point") {
      PLClosed.getPart(Vector2D(0, 0)) should equal(PolylineShape.Part(mutable.BitSet(0)))
      PLClosed.getPart(Vector2D(0, 100)) should equal(PolylineShape.Part(mutable.BitSet(1)))
      PLClosed.getPart(Vector2D(100, 100)) should equal(PolylineShape.Part(mutable.BitSet(2)))
      PLClosed.getPart(Vector2D(-100, -100)) should equal(EmptyShapeSelector$)
      PLClosed.getPart(Vector2D(50, -50)) should equal(EmptyShapeSelector$)
    }

    it ("can select line segments from a point") {
      PLClosed.getPart(Vector2D(0, 50)) should equal(PolylineShape.Part(mutable.BitSet(0, 1)))
      PLClosed.getPart(Vector2D(50, 100)) should equal(PolylineShape.Part(mutable.BitSet(1, 2)))
      PLClosed.getPart(Vector2D(50, 50)) should equal(PolylineShape.Part(mutable.BitSet(0, 2)))
      PLClosed.getPart(Vector2D(50, 0)) should equal(EmptyShapeSelector$)
    }

    it ("can select two points from several candidates") {
      PLSmall.getPart(Vector2D(-0.5, 0)) should equal (PolylineShape.Part(mutable.BitSet(0, 1)))
      PLSmall.getPart(Vector2D(-0.3, -0.3)) should equal (PolylineShape.Part(mutable.BitSet(1, 2)))
      PLSmall.getPart(Vector2D(0, -0.5)) should equal (PolylineShape.Part(mutable.BitSet(0, 2)))
    }

  }

}
