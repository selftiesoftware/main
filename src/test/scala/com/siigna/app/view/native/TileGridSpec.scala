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
import com.siigna.util.geom.{SimpleRectangle2D, Vector2D}

/**
 * Tests the [[com.siigna.app.view.native.TileGrid]].
 */
class TileGridSpec extends FunSpec with ShouldMatchers {

  val d = MultiTilePainterSpec.d
  val grid = MultiTilePainterSpec.g
  val v = MultiTilePainterSpec.v

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

  describe("A TileGrid") {

    it ("can be initialized correctly") {
      testGrid(grid, Vector2D(0, 0))
    }

    it ("can be moved east") {
      val g = grid.move(TileGrid.East)(d, v)
      testGrid(g, Vector2D(1, 0))
    }

    it ("can be moved north") {
      val g = grid.move(TileGrid.North)(d, v)
      testGrid(g, Vector2D(0, -1))
    }

    it ("can be moved west") {
      val g = grid.move(TileGrid.West)(d, v)
      testGrid(g, Vector2D(-1, 0))
    }

    it ("can be moved south") {
      val g = grid.move(TileGrid.South)(d, v)
      testGrid(g, Vector2D(0, 1))
    }

    it ("can calculate the right direction to move the grid to") {
      grid.direction(0, 0) should equal (None)
      grid.direction(0.5, 0.5) should equal (None)
      grid.direction(1.0, 1.0) should equal (None)

      grid.direction( 1.5,    0).get should equal(TileGrid.East)
      grid.direction(   0, -1.5).get should equal(TileGrid.North)
      grid.direction(   0,  1.5).get should equal(TileGrid.South)
      grid.direction(-1.5,    0).get should equal(TileGrid.West)
    }

  }

}
