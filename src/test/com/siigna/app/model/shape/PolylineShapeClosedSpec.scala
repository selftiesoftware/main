package com.siigna.app.model.shape

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.siigna.util.geom.Vector2D
import collection.mutable
import com.siigna.app.Siigna

/**
 * Tests an open polyline shape
 */
class PolylineShapeClosedSpec extends FunSpec with ShouldMatchers {

  describe("A closed PolylineShape") {

    val PLClosed = PolylineShape(Vector2D(0, 0), Vector2D(0, 100), Vector2D(100, 100), Vector2D(0, 0)).asInstanceOf[PolylineShape.PolylineShapeClosed]

    it ("can select a single point from a point") {
      PLClosed.getPart(Vector2D(0, 0)) should equal(PolylineShape.Selector(mutable.BitSet(0)))
      PLClosed.getPart(Vector2D(0, 100)) should equal(PolylineShape.Selector(mutable.BitSet(1)))
      PLClosed.getPart(Vector2D(100, 100)) should equal(PolylineShape.Selector(mutable.BitSet(2)))
      PLClosed.getPart(Vector2D(-100, -100)) should equal(EmptySelector)
      PLClosed.getPart(Vector2D(50, -50)) should equal(EmptySelector)
    }

    it ("can select line segments from a point") {
      PLClosed.getPart(Vector2D(0, 50)) should equal(PolylineShape.Selector(mutable.BitSet(0, 1)))
      PLClosed.getPart(Vector2D(50, 100)) should equal(PolylineShape.Selector(mutable.BitSet(1, 2)))
      PLClosed.getPart(Vector2D(50, 50)) should equal(PolylineShape.Selector(mutable.BitSet(0, 2)))
      PLClosed.getPart(Vector2D(50, 0)) should equal(EmptySelector)
    }

  }

}
