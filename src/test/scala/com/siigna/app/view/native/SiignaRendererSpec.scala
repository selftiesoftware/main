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

import org.scalatest.{BeforeAndAfter, FunSpec}
import org.scalatest.matchers.ShouldMatchers
import com.siigna.util.geom.SimpleRectangle2D
import com.siigna.app.view.{Interface, View}
import com.siigna.app.model.Drawing

/**
 * Tests the [[com.siigna.app.view.native.SiignaRenderer]]
 */
class SiignaRendererSpec extends SiignaRenderer with FunSpec with ShouldMatchers with BeforeAndAfter {

  var drawing : Drawing { def boundary_=(s : SimpleRectangle2D) } = null

  var view : View  = null

  before {
    drawing = new Drawing {
      var _boundary = SimpleRectangle2D(0, 0, 100, 100)
      def boundary = _boundary
      def boundary_=(s : SimpleRectangle2D) { _boundary = s }
    }
    view = new View {
      var _screen : SimpleRectangle2D = SimpleRectangle2D(0, 0, 10, 10)
      def screen = _screen
      def paint(screenGraphics: java.awt.Graphics, drawing: Drawing, interface: Option[Interface]) {}
    }
  }

  describe ("The Siigna Renderer") {

    /*it ("can know whether the drawing can be seen entirely in the view") {
      isSingleTile should equal(false)
      drawing.boundary = SimpleRectangle2D(4, 4, 6, 6)
      isSingleTile should equal(true)
      drawing.boundary = SimpleRectangle2D(0, 0, 10, 10)
      isSingleTile should equal(true)
      drawing.boundary = SimpleRectangle2D(-1, -1, 11, 11)
      isSingleTile should equal(false)
    }

    it ("can calculate the tiles") {
      renderedDelta should equal (Vector2D(0, 0))
      renderedPan should equal (Vector2D(0, 0))
      tileDeltaX should equal(0)
      tileDeltaY should equal(0)
      tileDelta should equal(Vector2D(0, 0))

      tile(vC)  should equal (SimpleRectangle2D(  0,  0, 10, 10))
      tile(vNE) should equal (SimpleRectangle2D( 10,-10, 20,  0))
      tile(vN)  should equal (SimpleRectangle2D(  0,-10, 10,  0))
      tile(vNW) should equal (SimpleRectangle2D(-10,-10,  0,  0))
      tile(vW)  should equal (SimpleRectangle2D(-10,  0,  0, 10))
      tile(vSE) should equal (SimpleRectangle2D( 10, 10, 20, 20))
      tile(vS)  should equal (SimpleRectangle2D(  0, 10, 10, 20))
      tile(vSW) should equal (SimpleRectangle2D(-10, 10,  0, 20))
      tile(vE)  should equal (SimpleRectangle2D( 10,  0, 20, 10))
    }*/

  }

}
