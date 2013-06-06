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

import java.awt.image.BufferedImage

import com.siigna.app.Siigna
import com.siigna.app.model.Drawing
import com.siigna.app.view.{Graphics, View, Renderer}
import com.siigna.util.Implicits._
import com.siigna.util.geom.{SimpleRectangle2D, Rectangle2D}
import scala.concurrent.{future, promise, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

/**
 * <p>
 *   Siignas own implementation of the [[com.siigna.app.view.Renderer]] which draws a chess-checkered background,
 *   a canvas for the [[com.siigna.app.model.Drawing]] and the shapes using caching-techniques. The SiignaRenderer
 *   uses the colors and attributes defined in the [[com.siigna.app.SiignaAttributes]] (accessible via the
 *   [[com.siigna.app.Siigna]] object).
 * </p>
 *
 * <p>
 *   This renderer uses [[com.siigna.app.view.native.Tile]]s to cache the content of the current view in images.
 *   Instead of painting the view at each paint-loop we simply paint the cached images, which drastically improves
 *   performance. Unless the drawing can be contained in one tile (the user has zoomed out to view the entire drawing)
 *   we render the center tile of the view synchronously and queueing the other tiles up asynchronously.
 * </p>
 */
trait SiignaRenderer extends Renderer {

  // The background-image promise that paints the background of the screen
  private var background : Promise[BufferedImage] = promise[BufferedImage]()

  // The image itself for synchronous access
  private var backgroundImage : Option[BufferedImage] = None

  /**
   * The active [[com.siigna.app.view.native.TilePainter]] capable of actually painting the model.
   */
  protected var painter : Option[TilePainter] = None

  /**
   * The drawing to search for [[com.siigna.app.model.shape.Shape]]s to draw.
   * @return  An instance of a [[com.siigna.app.model.Drawing]] containing the shapes we want to draw.
   */
  protected def drawing : Drawing

  // A boolean value to indicate that the view is zoomed out enough to only need one single tile
  protected def isSingleTile(drawing : SimpleRectangle2D, view : SimpleRectangle2D) =
    view.width >= drawing.width && view.height >= drawing.height

  // Simply forwards the painting to the active painter
  def paint(graphics : Graphics, drawing : Drawing, view : View) {
    // Draw the background (if any)
    backgroundImage.foreach(image => graphics.AWTGraphics.drawImage(image, 0, 0, null))

    // Draw a white background for the drawing-area
    val boundary = drawing.boundary.transform(View.drawingTransformation)
    graphics.AWTGraphics.setColor(Siigna.color("colorBackground").getOrElse(java.awt.Color.white))
    graphics.AWTGraphics.clearRect(boundary.xMin.toInt, boundary.yMin.toInt,
                                   boundary.width.toInt, boundary.height.toInt)
    graphics.AWTGraphics.setColor(Siigna.color("colorBackgroundBorder").getOrElse(java.awt.Color.gray))
    graphics.drawRectangle(boundary.bottomLeft, boundary.topRight)

    // Draw the painter
    val pan = boundary.bottomLeft
    painter.foreach(_.paint(graphics, pan))
  }

  /**
   * Renders a background-image consisting of "chess checkered" fields on an image equal to the size of the given
   * rectangle.
   * Should only be called every time the screen resizes.
   * @param screen  The screen given in device coordinates (from (0, 0) to (width, height)).
   * @return  A buffered image with dimensions equal to the given screen and a chess checkered field drawn on it.
   */
  def renderBackground(screen : Rectangle2D) : BufferedImage = {
    // Create image
    val image = new BufferedImage(screen.width.toInt, screen.height.toInt, BufferedImage.TYPE_4BYTE_ABGR)
    val g = image.getGraphics
    val size = Siigna.int("backgroundTileSize").getOrElse(12)
    var x = 0
    var y = 0

    // Clear background
    g setColor Siigna.color("colorBackgroundDark").getOrElse("#DADADA".color)
    g fillRect (0, 0, screen.width.toInt, screen.height.toInt)
    g setColor Siigna.color("colorBackgroundLight").getOrElse("E9E9E9".color)

    // Draw a chess-board pattern
    var evenRow = false
    while (x < screen.width) {
      while (y < screen.height) {
        g.fillRect(x, y, size, size)
        y += size << 1
      }
      x += size
      y = if (evenRow) 0 else size
      evenRow = !evenRow
    }
    image
  }

  /**
   * Sets the painter to the [[com.siigna.app.view.native.SingleTilePainter]] if the view is zoomed out enough,
   * otherwise we use the [[com.siigna.app.view.native.MultiTilePainter]] to render it as multiple
   * [[com.siigna.app.view.native.Tile]].
   */
  private def updatePainter() {
    val boundary = drawing.boundary.transform(view.drawingTransformation)
    painter = Some(
      if (isSingleTile(boundary, view.screen))
        new SingleTilePainter(view, drawing)
      else
        new MultiTilePainter(view, drawing)
    )
  }

  /**
   * The view to retrieve graphical information and [[com.siigna.util.geom.TransformationMatrix]] to transform
   * coordinates from.
   * @return  An instance of a [[com.siigna.app.view.View]] describing the screen we are drawing upon.
   */
  protected def view : View

}

/**
 * The concrete instance of the [[com.siigna.app.view.native.SiignaRenderer]]. The trait and object have been split
 * up for testing purposes.
 */
object SiignaRenderer extends SiignaRenderer {

  val drawing = Drawing
  val view = View

  drawing.addActionListener((_, _) => if (isActive) updatePainter())
  drawing.addSelectionListener(_   => if (isActive) updatePainter())

  // Adds a zoom-listener and resize-listener so we can tell if we are still within one tile
  view.addZoomListener((zoom) => if (isActive) (updatePainter()) )
  view.addResizeListener((screen) => if (isActive) {
    // Update the painter
    updatePainter()

    // Interrupt the old promise (if it is not already done)
    background.tryFailure(new InterruptedException)

    // Create a new promise
    val p = promise[BufferedImage]()

    // Create the future to render the background-image
    background = p completeWith future {
      renderBackground(screen)
    }

    // Store the background when done
    background.future.onComplete(_ match {
      case Success(i) => backgroundImage = Some(i)
      case _ =>
    })
  } )

}
