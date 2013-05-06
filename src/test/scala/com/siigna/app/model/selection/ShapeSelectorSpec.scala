/*
 *
 *  * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 *  * to Share — to copy, distribute and transmit the work,
 *  * to Remix — to adapt the work
 *  *
 *  * Under the following conditions:
 *  * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 *  * Noncommercial — You may not use this work for commercial purposes.
 *  * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 *
 */

package com.siigna.app.model.selection

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import scala.collection.immutable.BitSet

/**
 * Test the model
 */
class ShapeSelectorSpec extends FunSpec with ShouldMatchers {

  describe("A BitSetShapeSelector") {

    val t = BitSetShapeSelector(BitSet(1))
    val f = BitSetShapeSelector(BitSet(2))

    it ("can add another empty selector") {
      (t ++ EmptyShapeSelector) should equal(t)
    }

    it ("can add another full selector") {
      (t ++ FullShapeSelector) should equal(FullShapeSelector)
    }

    it ("can add another non-empty selector") {
      (t ++ f) should equal(BitSetShapeSelector(BitSet(1, 2)))
      (f ++ t) should equal(BitSetShapeSelector(BitSet(1, 2)))
    }

  }

}