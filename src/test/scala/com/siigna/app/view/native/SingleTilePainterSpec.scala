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

package com.siigna.app.view.native

import com.siigna.app.model.Drawing
import com.siigna.util.geom.SimpleRectangle2D
import com.siigna.app.view.View
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

/**
 * Tests the [[com.siigna.app.view.native.SingleTilePainter]].
 */
class SingleTilePainterSpec extends SingleTilePainter(
  new Drawing {
    def boundary: SimpleRectangle2D = SimpleRectangle2D(-1, -1, 1, 1)
  },
  new View {
    def screen = SimpleRectangle2D(0, 0, 1, 1)
    def paint(screenGraphics: java.awt.Graphics, drawing: Drawing, interface: Option[com.siigna.app.view.Interface]) {}
  }
) with FunSpec with ShouldMatchers {

  describe ("The Single tile painter") {

    it ("can correctly instantiate the single tile") {
      val v = new View {
        def screen = SimpleRectangle2D(0, 0, 1, 1)
        def paint(screenGraphics: java.awt.Graphics, drawing: Drawing, interface: Option[com.siigna.app.view.Interface]) {}
      }

      centerTile.window should equal(SimpleRectangle2D(-1, -1, 1, 1))
      centerTile.image foreach { image =>
        image.getWidth should equal (2)
        image.getHeight should equal (2)
      }
    }

  }

}
