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

import com.siigna.app.view.{View, Graphics}
import com.siigna.app.model.Drawing
import com.siigna.util.geom.Vector2D
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * <p>
 *   A TilePainter is an immutable class capable of painting the [[com.siigna.app.model.shape.Shape]]s in a
 *   [[com.siigna.app.model.Drawing]] in one or more tile(s), for one single zoom-level and screen-size.
 * </p>
 *
 * <p>
 *   A TilePainter works asynchronously via the scala 2.10 future-API. Each time a TilePainter is created it tries to
 *   render its [[com.siigna.app.view.native.Tile]]s in the background. As soon as the painter is ready for use any
 *   callbacks defined via the [[com.siigna.app.view.native.TilePainter.onComplete]] will be called.
 * </p>
 *
 * <p>
 *   The trait implement two sub-types: A [[com.siigna.app.view.native.SingleTilePainter]] which
 *   paints the model as a single tile (the user have zoomed so far out that the [[com.siigna.app.model.Drawing]]
 *   can be displayed in one tile), and the [[com.siigna.app.view.native.MultiTilePainter]] which is capable
 *   of painting an intricate tile-system which arranges the view as 9 images (tiles) that can be moved when the
 *   user pans, instead of re-painting the entire drawing.
 * </p>
 */
sealed trait TilePainter {

  /**
   * Interrupts the painter and all its threads running in the background. Efficient for cancelling any renderings
   * if the screen has changed or the painter is rendered obsolete otherwise.
   * @return  True if the painter could be interrupted, false if the painter has already finished its calculations.
   */
  def interrupt : Boolean

  /**
   * Executes the given function when the [[com.siigna.app.view.native.TilePainter]] has rendered the necessary
   * background-[[com.siigna.app.view.native.Tile]]s and is ready to be used.
   * @param func  The callback function.
   * @tparam U  The return-type of function `func`
   */
  def onComplete[U](func : Try[TilePainter] => U)

  /**
   * Draws the painter with the given [[com.siigna.app.view.Graphics]], [[com.siigna.app.model.Drawing]] and
   * [[com.siigna.app.view.View]].
   * @param graphics  The graphics to draw the
   * @param pan  The amount the user have panned in the current zoom-level.
   */
  def paint(graphics : Graphics, pan : Vector2D)

}

/**
 * A [[com.siigna.app.view.native.TilePainter]] painting the [[com.siigna.app.model.Drawing]] in one tile always.
 * @param drawing  The drawing to extract [[com.siigna.app.model.shape.Shape]]s to draw and boundaries to determine
 *                 which areas to clear.
 * @param view  The view to retrieve information on screen size, transformations etc.
 */
class SingleTilePainter(view : View, drawing : Drawing) extends TilePainter {

  protected val centerTile = new Tile(drawing, view, drawing.boundary.transform(view.drawingTransformation))

  def interrupt: Boolean = centerTile.interrupt()

  def onComplete[U](func : Try[TilePainter] => U) {
    centerTile.onComplete{ t => Success(t.map(_ => this)) }
  }

  def paint(graphics : Graphics, pan : Vector2D) {
    val x = pan.x.round.toInt
    val y = pan.y.round.toInt
    centerTile.image.foreach( image =>
      graphics.AWTGraphics.drawImage(image, x, y, null)
    )
  }
}

/**
 * <p>
 *   A [[com.siigna.app.view.native.TilePainter]] that arranges the drawing into 9 tiles.
 * </p>
 *
 * <p>
 *   The tiles are arranged so the center tile covers the entire view at the time of rendering. If the user pans the
 *   view, one or more of the other tiles will be rendered to cover the "gap" left by the pan. If the user pans more
 *   than the width of the view divided by 2 - that is, the center-point of the view is moved to another tile - we
 *   "move" the center to a new tile. Thus, the center-tile can "move" and maintain a constant of 9 tiles to render
 *   the entire view.
 * </p>
 * {{{
 *
 *   +----+----+----+
 *   | NW | N  | NE |
 *   +----+----+----+
 *   | W  | C  | E  |
 *   +----+----+----+
 *   | SW | S  | SE |
 *   +----+----+----+
 *
 * }}}
 *
 * @param drawing  The drawing to extract [[com.siigna.app.model.shape.Shape]]s to draw and boundaries to determine
 *                 which areas to clear.
 * @param view  The view to retrieve information on screen size, transformations etc.
 */
class MultiTilePainter(view : View, drawing : Drawing) extends TilePainter {

  // The tile deltas for the two axis
  protected var tileDeltaX = 0; protected var tileDeltaY = 0

  def interrupt: Boolean = false

  def onComplete[U](func : Try[TilePainter] => U) {
    // TODO
  }

  def paint(graphics : Graphics, pan : Vector2D) {

  }

  protected def updateTiles(drawing : Drawing, view : View) {

  }

}