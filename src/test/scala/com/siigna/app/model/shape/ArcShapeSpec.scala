/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */

package com.siigna.app.model.shape

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.siigna.util.geom.{TransformationMatrix, SimpleRectangle2D, Vector2D}
import com.siigna.app.model.selection.{FullShapeSelector, EmptyShapeSelector, ShapeSelector}

/**
 * Tests the [[com.siigna.app.model.shape.ArcShape]].
 */
class ArcShapeSpec extends FunSpec with ShouldMatchers {

  describe("An ArcShape") {

    val a = ArcShape(Vector2D(0, 0), 20,0,180)

    it ("can find a selector given a point") {
      a.getSelector(Vector2D(0, 0)) should equal (FullShapeSelector)
    }
  }
}
