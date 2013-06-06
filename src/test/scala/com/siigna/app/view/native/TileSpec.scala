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

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import com.siigna.app.model.Drawing
import com.siigna.util.geom.SimpleRectangle2D
import com.siigna.app.view.{Interface, View}
import java.awt.Graphics
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Tests a tile used in the SiignaRenderer
 */
class TileSpec extends FunSpec with ShouldMatchers {

  describe("A Tile") {

    val drawing = new Drawing {
      def boundary: SimpleRectangle2D = SimpleRectangle2D(-10, -10, 10, 10)
    }

    val view = new View {
      def paint(screenGraphics: Graphics, drawing: Drawing, interface: Option[Interface]) {}
      def screen: SimpleRectangle2D = SimpleRectangle2D(4, 4, 6, 7)
    }

    val tile = new Tile(drawing, view, view.screen)

    it ("can be created with a drawing, view and rectangle") {
      tile.image.foreach { image =>
        image.getWidth should equal (2)
        image.getHeight should equal (3)
      }
    }

    it ("can calculate a window of the drawing") {
      // The center of the screen - the screen-coordinates (flipped = a negative y-axis)
      val centerX = 5.0
      val centerY = 5.5
      tile.window should equal (SimpleRectangle2D(4 - centerX, 4 - centerY, 6 - centerX, 7 - centerY))
    }

  }

}
