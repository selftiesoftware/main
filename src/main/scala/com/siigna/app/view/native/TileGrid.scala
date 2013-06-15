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
import com.siigna.app.model.Drawing
import com.siigna.app.view.View

/**
 * An immutable grid used in the [[com.siigna.app.view.native.TilePainter]] to keep track of the position of the tiles.
 *
 * @param window  The drawing-window to render as the center tile, in drawing-coordinates.
 * @param screen  The screen to render as the center tile, in device-coordinates.
 * @param rowNorth  The north-west, north and north-east tiles.
 * @param rowCenter  The west, center and east tiles.
 * @param rowSouth  The south-west, south and south-east tiles.
 */
case class TileGrid(window : SimpleRectangle2D, screen : SimpleRectangle2D,
                    rowNorth : List[Tile], rowCenter : List[Tile], rowSouth : List[Tile]) {

  /**
   * Calculates the direction to move the grid from the given pan
   * @param panX  The panning on the x-axis
   * @param panY  The panning on the y-axis
   * @return  A [[com.siigna.app.view.native.TileGrid$.Direction]] if the panning is large enough to cause a move
   *          operation.
   */
  def direction(panX : Double, panY : Double) : Option[TileGrid.Direction] = {
    if      (panX < screen.xMin) Some(TileGrid.West)
    else if (panX > screen.xMax) Some(TileGrid.East)
    else if (panY < screen.yMin) Some(TileGrid.South)
    else if (panY > screen.yMax) Some(TileGrid.North)
    else None
  }

  /**
   * Moves the entire grid (based on the center) in the given direction by re-using tiles still in sight.
   * @param direction  The direction to move the center of the grid.
   * @param drawing  The drawing in scope to use to create tiles.
   * @param view  The view in scope to use to create tiles.
   * @return  A new instance of a TileGrid with the tiles moved in the given direction.
   */
  def move(direction : TileGrid.Direction)(implicit drawing : Drawing, view : View) : TileGrid = {
    direction match {
      case TileGrid.East  => {
        val w = window + Vector2D(window.width, 0)
        new TileGrid(w, screen + Vector2D(screen.width, 0),
          rowNorth .tail :+ new Tile(drawing, w + Vector2D( w.width, w.height), view.zoom, view.graphics),
          rowCenter.tail :+ new Tile(drawing, w + Vector2D( w.width,        0), view.zoom, view.graphics),
          rowSouth .tail :+ new Tile(drawing, w + Vector2D( w.width,-w.height), view.zoom, view.graphics)
        )
      }
      case TileGrid.North => {
        val w = window + Vector2D(0, window.height)
        new TileGrid(w, screen + Vector2D(0, screen.height),
          rowCenter,
          rowSouth,
          List(-1, 0, 1).map(i => w + Vector2D(i * w.width, w.height)).map(new Tile(drawing, _, view.zoom, view.graphics))
        )
      }
      case TileGrid.West  => {
        val w = window - Vector2D(window.width, 0)
        new TileGrid(w, screen - Vector2D(screen.width, 0),
          new Tile(drawing, w - Vector2D(w.width,-w.height), view.zoom, view.graphics) :: rowNorth .take(2),
          new Tile(drawing, w - Vector2D(w.width,        0), view.zoom, view.graphics) :: rowCenter.take(2),
          new Tile(drawing, w - Vector2D(w.width, w.height), view.zoom, view.graphics) :: rowSouth .take(2)
        )
      }
      case TileGrid.South => {
        val w = window + Vector2D(0, -window.height)
        new TileGrid(w, screen - Vector2D(0, screen.height),
          List(-1, 0, 1).map(i => w + Vector2D(i * w.width, -w.height)).map(new Tile(drawing, _, view.zoom, view.graphics)),
          rowNorth,
          rowCenter
        )
      }
    }
  }
}

/**
 * A companion object to the [[com.siigna.app.view.native.TileGrid]] containing an
 * enumeration describing different directions the [[com.siigna.app.view.native.TileGrid]] can move.
 */
object TileGrid extends Enumeration {

  /**
   * A direction in which the tile-grid can shift
   */
  type Direction = Value
  val East, North, West, South = Value

}
