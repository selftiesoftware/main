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
import com.siigna.util.geom.{SimpleRectangle2D, Vector2D}
import scala.util.{Success, Try}
import scala.collection.SeqProxy

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
trait TilePainter {

  protected var _panX : Double = 0
  protected var _panY : Double = 0

  /**
   * The [[com.siigna.app.model.Drawing]] to extract shapes and boundaries from.
   * @return  An instance of a [[com.siigna.app.model.Drawing]]
   */
  protected def drawing : Drawing

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
   */
  def paint(graphics : Graphics)

  /**
   * Pans the [[com.siigna.app.view.native.TilePainter]] to allow caching of the exact tile positions.
   * @param vector  The amount the view have been panned .
   */
  def pan(vector : Vector2D) : TilePainter = {
    _panX += vector.x
    _panY += vector.y

    update()
  }

  /**
   * Updates the [[com.siigna.app.view.native.TilePainter]] and returns an instance that fits the current situation.
   * @return  A [[com.siigna.app.view.native.TilePainter]] capable of drawing the current view.
   */
  protected def update() : TilePainter

  /**
   * The amount of panning on the x-axis as an int.
   * @return  An integer describing the pan on the x-axis.
   */
  def panX = _panX

  /**
   * The amount of panning on the y-axis as an int.
   * @return  An integer describing the pan on the y-axis.
   */
  def panY = _panY

  /**
   * The view to retrieve panning information and screen-coordinates from.
   * @return  An instance of a [[com.siigna.app.view.View]].
   */
  protected def view : View

}

/**
 * Companion object to the [[com.siigna.app.view.native.TilePainter]]. Provides simple constructors to create
 * instances of the [[com.siigna.app.view.native.SingleTilePainter]] or [[com.siigna.app.view.native.MultiTilePainter]].
 */
object TilePainter {

  // A boolean value to indicate that the view is zoomed out enough to only need one single tile
  protected def isSingleTile(drawing : SimpleRectangle2D, view : SimpleRectangle2D) =
    view.width >= drawing.width && view.height >= drawing.height

  /**
   * Creates a new TilePainter using the given [[com.siigna.app.model.Drawing]] and [[com.siigna.app.view.View]].
   * @param drawing  The drawing to retrieve shapes from.
   * @param view  The view to get screen-dimensions, panning and zoom information from.
   * @return  An instance of a [[com.siigna.app.view.native.TilePainter]].
   */
  def apply(drawing : Drawing, view : View) : TilePainter = {
    val boundary = drawing.boundary.transform(view.drawingTransformation)
    if (isSingleTile(boundary, view.screen))
      new SingleTilePainter(drawing, view)
    else
      new MultiTilePainter(drawing, view, TileGrid(drawing, view), view.center)
  }

}

/**
 * A [[com.siigna.app.view.native.TilePainter]] painting the [[com.siigna.app.model.Drawing]] in one tile always.
 * @param view  The view to retrieve information on screen size, transformations etc.
 * @param drawing  The drawing to extract [[com.siigna.app.model.shape.Shape]]s to draw and boundaries to determine
 *                 which areas to clear.
 */
class SingleTilePainter(protected val drawing : Drawing, protected val view : View) extends TilePainter {

  val centerTile = new Tile(drawing, drawing.boundary, view.zoom, view.graphics)
  protected val topLeft = drawing.boundary.topLeft.transform(view.drawingTransformation)
  protected val topLeftX = topLeft.x.round.toInt
  protected val topLeftY = topLeft.y.round.toInt

  def interrupt: Boolean = centerTile.interrupt()

  def onComplete[U](func : Try[TilePainter] => U) {
    centerTile.onComplete{ t => {
      func(t.map(_ => this))
    } }
  }

  def paint(graphics : Graphics) {
    centerTile.image.foreach( image =>
      graphics.AWTGraphics.drawImage(image, (panX + topLeftX).toInt, (panY + topLeftY).toInt, null)
    )
  }

  protected def update() = this

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
 * @param view  The view to retrieve information on screen size, transformations etc.
 * @param drawing  The drawing to extract [[com.siigna.app.model.shape.Shape]]s to draw and boundaries to determine
 *                 which areas to clear.
 */
class MultiTilePainter(protected val drawing : Drawing, protected val view : View, val grid : TileGrid, startPan : Vector2D) extends TilePainter {

  /**
   * Calculates the current pan for the grid.
   * @return  A x- and y-coordinate describing the pan on both axis.
   */
  def gridPan : (Double, Double) = {
    // Examine if the pan has changed and requires the tiles to be moved
    (startPan.x - panX, startPan.y + panY)
  }

  def interrupt: Boolean = {
    grid.rowNorth.forall(_.interrupt())
    grid.rowCenter.forall(_.interrupt())
    grid.rowSouth.forall(_.interrupt())
  }

  def onComplete[U](func : Try[TilePainter] => U) {
    grid.rowCenter(1).onComplete( t => func(t.map(_ => this)) )
  }

  def paint(graphics : Graphics) {
    (grid.rowNorth ++ grid.rowCenter ++ grid.rowSouth).foreach(tile => {
      tile.image.foreach { image =>
        val window = tile.window.transform(view.drawingTransformation)
        if (view.screen.intersects(window)) { // Only draw if the tile is actually visible
          val anchor = window.bottomLeft
          graphics.AWTGraphics.drawImage(image, anchor.x.toInt, anchor.y.toInt, null)
        }
      }
    })
  }

  protected def update() = {
    val (x, y) = gridPan
    grid.direction(x, y).map(d => {
      // Interrupt the current painter
      interrupt

      // Return a new painter with the moved grid
      new MultiTilePainter(drawing, view, grid.move(d)(drawing, view), Vector2D(x, y))
    }).getOrElse(this)
  }

}

