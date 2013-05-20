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
import com.siigna.app.model.shape.LineShape

/**
 * A test class for the Transformation Matrix.
 */
class TransformationMatrixSpec extends FunSpec with ShouldMatchers {

  val t = TransformationMatrix()
  val u = TransformationMatrix(Vector2D(math.Pi, math.E), 12.2)
  val v = Vector2D(1, 1)

  describe("An empty TransformationMatrix") {

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
      val t2 = TransformationMatrix(Vector2D(10, 10), 1000)

      t.concatenate(t2) should equal (t2)
    }

    it ("can rotate a transformationMatrix without altering the scale factor") {

      //test if rotation alters scale
      t.rotate(0).scaleFactor should equal (1.0)
      t.rotate(90).scaleFactor should equal (1.0)
      t.getTranslate should equal (Vector2D(0, 0))
    }

    it ("can rotate a vector without altering the scale factor or origin") {
      val l = LineShape(Vector2D(0,0),Vector2D(10,5))
      println(l.transform(t.rotate(90)))
      //test if rotation alters scale and origin point

    }

  }

}