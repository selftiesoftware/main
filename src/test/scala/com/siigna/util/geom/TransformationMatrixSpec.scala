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

package com.siigna.util.geom

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

/**
 * A test class for the Transformation Matrix.
 */
class TransformationMatrixSpec extends FunSpec with ShouldMatchers {

  val t = TransformationMatrix()
  val u = TransformationMatrix(Vector2D(math.Pi, math.E), 12.2)
  val v = Vector2D(1, 1)

  describe("An empty TransformationMatrix") {

    it ("can rotate around a center of (0, 0)") {
      val x = t.rotate(90)
      x.rotation should equal (90)
      x.scale should equal (1)
      x.translation should equal (Vector2D(0, 0))

      val y = t.rotate(1)
      y.rotation should equal(1)
      y.scale should equal(1)
      y.translation should equal(Vector2D(0, 0))

      val m = Vector2D(100, -542.765)
      val z = t.rotate(180).translate(m)
      z.rotation should equal(180)
      z.scale should equal (1)
      z.translation should equal (m * -1)
    }

    it ("can concatenate itself with other empty matrices") {
      t.concatenate(t) should equal(t)
    }

    it ("can concatenate itself with other non-empty matrices") {
      t.concatenate(u) should equal(u)
      u.concatenate(t) should equal(u)
    }

    it ("can flip the x axis") {
      t.flipX.transform(v) should equal (Vector2D(-1, 1))
    }

    it ("can flip the y axis") {
      t.flipY.transform(v) should equal (Vector2D(1, -1))
    }
  }

  describe("A non-empty TransformationMatrix") {

    it ("can concatenate itself with other matrices") {
      val t1 = TransformationMatrix()
      val t2 = TransformationMatrix(Vector2D(10, 10), 1000)

      t1.concatenate(t2) should equal (t2)
    }

  }

}