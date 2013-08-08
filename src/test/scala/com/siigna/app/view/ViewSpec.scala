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

package com.siigna.app.view

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.siigna.util.geom.{SimpleRectangle2D, Vector2D}

/**
 * Tests the [[com.siigna.app.view.View]]
 */
class ViewSpec extends FunSpec with ShouldMatchers {

  describe("The View") {

    it ("Can transform a vector from drawing coordinates to screen coordinates") {
      // Unit vector
      Vector2D(0, 0).transform(View.drawingTransformation) should equal(Vector2D(0.5, 0.5))

      // y-coords
      Vector2D(0, 1).transform(View.drawingTransformation) should equal(Vector2D(0.5, -0.5))
    }

    it ("Can transform a vector from drawing coordinates to screen coordinates with different pans") {
      // Simple pan operation
      View.pan(Vector2D(1, 1))
      View.screen should equal (SimpleRectangle2D(0, 0, 1, 1))
      Vector2D(0, 0).transform(View.drawingTransformation) should equal(Vector2D(1.5, 1.5))

      // Simple pan operation
      View.pan(Vector2D(-1, -2))
      View.screen should equal (SimpleRectangle2D(0, 0, 1, 1))
      Vector2D(0, 0).transform(View.drawingTransformation) should equal(Vector2D(0.5, -0.5))

      // Reset
      View.pan = Vector2D(0, 0)
      View.screen should equal (SimpleRectangle2D(0, 0, 1, 1))
      Vector2D(0, 0).transform(View.drawingTransformation) should equal(Vector2D(0.5, 0.5))
    }

    it ("Can transform a vector from drawing coordinates to screen coordinates with different zoom levels") {
      // Simple pan operation
      View.zoom = 2
      View.screen should equal (SimpleRectangle2D(0, 0, 1, 1))
      Vector2D(0, 0).transform(View.drawingTransformation) should equal(Vector2D(0.5, 0.5))
      Vector2D(1, 1).transform(View.drawingTransformation) should equal(Vector2D(2.5, -1.5))
    }

  }

}
