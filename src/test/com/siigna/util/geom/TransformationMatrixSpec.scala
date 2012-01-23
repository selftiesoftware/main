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

package com.siigna.util.geom

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSuite, FlatSpec}

/**
 * A test class for the Transformation Matrix.
 */
class TransformationMatrixSpec extends FunSuite with ShouldMatchers {

  // Concatenate
  test("A TransformationMatrix can concatenate itself with other matrices") {
    val t1 = TransformationMatrix()
    val t2 = TransformationMatrix(Vector(10, 10), 1000)

    val t3 = TransformationMatrix(Vector(-10, 25.5), -5)
    val t4 = TransformationMatrix(Vector(20, 75), 5)

    t1.concatenate(t2) should equal (TransformationMatrix(Vector(10, 10), 1000))

    t3.concatenate(t4) should equal (TransformationMatrix(Vector(-10, 100.5), 0))
  }

}