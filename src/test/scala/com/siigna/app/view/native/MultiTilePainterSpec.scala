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

import com.siigna.util.geom.{SimpleRectangle2D, Vector2D}
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.siigna.app.model.Drawing
import com.siigna.app.view.{Interface, View}
import java.awt.Graphics

object MultiTilePainterSpec {
  val d = new Drawing {
    def boundary: SimpleRectangle2D = SimpleRectangle2D(-10, -10, 10, 10)
  }
  val v = new View {
    def paint(screenGraphics: Graphics, drawing: Drawing, interface: Option[Interface]) {}
    def screen: SimpleRectangle2D = SimpleRectangle2D(0, 0, 1, 1)
  }
  def g = {
    val r = v.screen.transform(v.deviceTransformation)
    TileGrid(r, v.screen,
      List(-1, 0, 1).map(i => r + Vector2D(i * r.width,  r.height)).map(new Tile(d, _, v.zoom, v.graphics)),
      List(-1, 0, 1).map(i => r + Vector2D(i * r.width,         0)).map(new Tile(d, _, v.zoom, v.graphics)),
      List(-1, 0, 1).map(i => r + Vector2D(i * r.width, -r.height)).map(new Tile(d, _, v.zoom, v.graphics))
    )
  }
}

/**
 * Tests the [[com.siigna.app.view.native.MultiTilePainter]].
 */
class MultiTilePainterSpec
  extends MultiTilePainter(MultiTilePainterSpec.d, MultiTilePainterSpec.v, MultiTilePainterSpec.g, Vector2D(0, 0))
     with FunSpec with ShouldMatchers {

  def testGrid(g : TileGrid, p : Vector2D) {
    val c = SimpleRectangle2D(-0.5, -0.5, 0.5, 0.5) + p

    g.window should equal (c)

    g.rowNorth(0).window should equal  (c + Vector2D(-1,  1))
    g.rowNorth(1).window should equal  (c + Vector2D( 0,  1))
    g.rowNorth(2).window should equal  (c + Vector2D( 1,  1)) // North row

    g.rowCenter(0).window should equal (c + Vector2D(-1,  0))
    g.rowCenter(1).window should equal (c + Vector2D( 0,  0))
    g.rowCenter(2).window should equal (c + Vector2D( 1,  0)) // Center row

    g.rowSouth(0).window should equal  (c + Vector2D(-1, -1)) // South row
    g.rowSouth(1).window should equal  (c + Vector2D( 0, -1))
    g.rowSouth(2).window should equal  (c + Vector2D( 1, -1))
  }

  describe ("A MultiTilePainter") {

    it ("can position the tiles correctly") {
      testGrid(grid, Vector2D(0, 0))
    }

    it("can move the grid according to a pan") {

      val m1 = pan(Vector2D(2, 0)).asInstanceOf[MultiTilePainter]
      testGrid(m1.grid, Vector2D(1, 0))

      val m2 = m1.pan(Vector2D(-2, 0)).asInstanceOf[MultiTilePainter]
      testGrid(m2.grid, Vector2D(0, 0))

      val m3 = m2.pan(Vector2D(0, 2)).asInstanceOf[MultiTilePainter]
      testGrid(m3.grid, Vector2D(0, -1))

      val m4 = m3.pan(Vector2D(0, -2)).asInstanceOf[MultiTilePainter]
      testGrid(m4.grid, Vector2D(0, 0))
    }

    it ("can calculate the pan of the grid") {
      val m0 = new MultiTilePainter(drawing, view, grid, Vector2D(0, 0))
      m0.gridPan should equal ((0, 0))

      val m1 = new MultiTilePainter(drawing, view, grid, Vector2D(0, 0))
      m1.pan(Vector2D(0.5, 0))
      m1.gridPan should equal ((0.5, 0))

      val m2 = new MultiTilePainter(drawing, view, grid, Vector2D(0, 0))
      m2.pan(Vector2D(0, -0.5))
      m2.gridPan should equal ((0, -0.5))
    }

  }

}
