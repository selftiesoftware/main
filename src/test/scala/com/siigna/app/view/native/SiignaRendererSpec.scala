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

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.siigna.util.geom.{SimpleRectangle2D, Vector2D}
import com.siigna.app.view.{Interface, View}
import com.siigna.app.model.Drawing
import java.awt.Graphics

/**
 * Tests the [[com.siigna.app.view.native.SiignaRenderer]]
 */
class SiignaRendererSpec extends FunSpec with ShouldMatchers with SiignaRenderer {

  var drawing = new Drawing {
    def boundary = SimpleRectangle2D(0, 0, 100, 100)
  }

  val view = new View {
    _screen = SimpleRectangle2D(0, 0, 10, 10)
    def paint(screenGraphics: Graphics, drawing: Drawing, interface: Option[Interface]) {}
  }

  describe ("The Siigna Renderer") {

    it ("can calculate the correct positions of the tiles") {
      renderedDelta should equal (Vector2D(0, 0))
      renderedPan should equal (Vector2D(0, 0))
      tileDeltaX should equal(0)
      tileDeltaY should equal(0)


    }

  }

}
