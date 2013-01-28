/*
 * Copyright (c) 2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.model

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import shape.LineShape
import com.siigna.util.geom.SimpleRectangle2D
import com.siigna.util.collection.Attributes

/**
 * Tests the spatial model.
 */
class SpatialModelSpec extends FunSpec with ShouldMatchers {

  describe ("Spatial Model") {

    it ("can calculate the minimum-bounding rectangle") {
      val l1 = LineShape(0, 0, 10, 10)
      new Model(Map(0 -> l1), Nil, Nil, Attributes()).mbr should equal(SimpleRectangle2D(0, 0, 10, 10))

      val l2 = LineShape(-10, -10, 0, 10)
      new Model(Map(0 -> l2), Nil, Nil, Attributes()).mbr should equal(SimpleRectangle2D(-10, -10, 0, 0))
    }

  }

}
