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

/**
 * Tests an open polyline shape
 */
class PolylineShapeSpec extends FunSpec with ShouldMatchers {

  describe("The PolylineShape object") {

    it ("can remove subsequent duplicates from a list") {
      val l1 = List(1, 2, 2, 3)
      PolylineShape.distinctNeighbour(l1) should equal(List(1, 2, 3))

      val l2 = List(1, 1, 1, 1, 1, 1)
      PolylineShape.distinctNeighbour(l2) should equal(List(1))

      val l3 = List()
      PolylineShape.distinctNeighbour(l3) should equal(Nil)

      val l4 = List(1, 4, 9, 123, 4, 1)
      PolylineShape.distinctNeighbour(l4) should equal(List(1, 4, 9, 123, 4, 1))
    }

  }

}
