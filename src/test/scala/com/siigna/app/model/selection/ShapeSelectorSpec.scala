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
 * Test the ShapeSelectors
 */
class ShapeSelectorSpec extends FunSpec with ShouldMatchers {

  describe("A BitSetShapeSelector") {

    val t = BitSetShapeSelector(BitSet(1))
    val f = BitSetShapeSelector(BitSet(2))

    it ("can detect the presence of a bit") {
      t(1) should equal(true)
      t(2) should equal(false)
      f(1) should equal(false)
      f(2) should equal(true)
    }

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

    it ("can se that another empty selector is not contained") {
      t.contains(EmptyShapeSelector) should equal (false)
    }

    it ("can se that another full selector is not contained") {
      t.contains(FullShapeSelector) should equal (false)
    }

    it ("can check if another non-empty selector is contained within") {
      t.contains(f) should equal(false)
      t.contains(t) should equal(true)
      BitSetShapeSelector(BitSet(0, 1, 2, 3)).contains(BitSetShapeSelector(BitSet(0, 1))) should equal(true)
      BitSetShapeSelector(BitSet(0, 1)).contains(BitSetShapeSelector(BitSet(0, 1, 2, 3))) should equal(false)
    }

    it ("can determine whether it is empty") {
      t.isEmpty should equal (false)
      BitSetShapeSelector(BitSet()).isEmpty should equal (true)
    }

    it ("can remove another empty selector") {
      (t -- EmptyShapeSelector) should equal (t)
    }

    it ("can remove another full selector") {
      (t -- FullShapeSelector) should equal(EmptyShapeSelector)
    }

    it ("can remove another non-empty selector") {
      (t -- f) should equal(t)
      (t -- t) should equal(EmptyShapeSelector)
    }

  }

  describe("An EmptyShapeSelector") {

    val empty = EmptyShapeSelector
    val full = FullShapeSelector
    val nonEmpty = BitSetShapeSelector(BitSet(0, 2))

    it("does not have any elements") {
      empty(0) should equal(false)
      empty(12) should equal(false)
    }

    it ("can add another empty selector") {
      (empty ++ empty) should equal (empty)
    }

    it ("can add another full selector") {
      (empty ++ full) should equal (full)
    }

    it ("can add another non-empty selector") {
      (empty ++ nonEmpty) should equal (nonEmpty)
    }

    it ("can see that nothing is contained in it") {
      empty.contains(empty) should equal (false)
      empty.contains(full) should equal (false)
      empty.contains(nonEmpty) should equal (false)
    }

    it ("can determine whether it is empty") {
      empty.isEmpty should equal(true)
    }

    it ("can remove an empty selector") {
      (empty -- empty) should equal(empty)
    }

    it ("can remove a full selector") {
      (empty -- full) should equal(empty)
    }

    it ("can remove a non-empty selector") {
      (empty -- nonEmpty) should equal (empty)
    }
  }

  describe("A FullShapeSelector") {

    val empty = EmptyShapeSelector
    val full = FullShapeSelector
    val nonEmpty = BitSetShapeSelector(BitSet(0, 2))

    it("contains all elements") {
      full(0) should equal(true)
      full(12) should equal(true)
    }

    it ("can add another empty selector") {
      (full ++ empty) should equal (full)
    }

    it ("can add another full selector") {
      (full ++ full) should equal (full)
    }

    it ("can add another non-empty selector") {
      (full ++ nonEmpty) should equal (full)
    }

    it ("can see that all selectors are contained in it") {
      full.contains(full) should equal(true)
      full.contains(empty) should equal(true)
      full.contains(nonEmpty) should equal(true)
    }

    it ("can determine whether it is empty") {
      full.isEmpty should equal(false)
    }

    it ("can remove an empty selector") {
      (full -- empty) should equal(full)
    }

    it ("can remove a full selector") {
      (full -- full) should equal(empty)
    }

    it ("can remove a non-empty selector") {
      (full -- nonEmpty) should equal (empty)
    }

  }

}