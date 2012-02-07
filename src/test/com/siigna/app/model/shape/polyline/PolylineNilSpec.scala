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

/**
 * A test for the PolylineNil case object.
 */
class PolylineNilSpec extends Spec with ShouldMatchers {
  
  describe("An empty polyline list") {
    
    it ("is empty") { PolylineNil.isEmpty should be(true) }

    it ("does not have a tail or head element") {
      evaluating {
        PolylineNil.head
      } should produce [UnsupportedOperationException]
      evaluating {
        PolylineNil.tail
      } should produce [UnsupportedOperationException]
    }

    it ("cannot be converted to shapes") {
      evaluating {
        PolylineNil.toShapes
      } should produce [UnsupportedOperationException]
    }

    it ("cannot be transformed") {
      evaluating {
        PolylineNil.transform(TransformationMatrix(Vector2D(0, 0), 1))
      } should produce [UnsupportedOperationException]
    }
    
  }

}
