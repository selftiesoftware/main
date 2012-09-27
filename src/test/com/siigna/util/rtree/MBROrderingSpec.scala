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

package com.siigna.util.rtree

import com.siigna.util.geom.Rectangle2D

import org.scalatest.{FunSpec, Spec}
import org.scalatest.matchers.ShouldMatchers

/**
 * A test for the MBROrdering classes.
 */
class MBROrderingSpec extends FunSpec with ShouldMatchers {

  val r1 = Rectangle2D(0, 0, 10, 10);
  val r2 = Rectangle2D(20, 20, 0, 0);

  describe("A MBROrdering") {

    describe("OrderMinX should be able to find the least x-coordinate") {
      OrderMinX.compare(r1, r2) should be (-1)
      OrderMinX.compare(r1, r1) should be (0)
      OrderMinX.compare(r2, r1) should be (1)
    }
    
    describe("OrderMinY should be able to find the least y-coordinate") {
      OrderMinY.compare(r1, r2) should be (-1)
      OrderMinY.compare(r2, r2) should be (0)
      OrderMinY.compare(r2, r1) should be (1)
    }

    describe("OrderMaxX should be able to find the largest x-coordinate") {
      OrderMaxX.compare(r1, r2) should be (-1)
      OrderMaxX.compare(r2, r2) should be (0)
      OrderMaxX.compare(r2, r1) should be (1)
    }

    describe("OrderMaxY should be able to find the largest y-coordinate") {
      OrderMaxY.compare(r1, r2) should be (-1)
      OrderMaxY.compare(r2, r2) should be (0)
      OrderMaxY.compare(r2, r1) should be (1)
    }

  }

}