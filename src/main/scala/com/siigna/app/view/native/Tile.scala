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

import com.siigna.util.geom.{Vector2D, TransformationMatrix, SimpleRectangle2D}
import java.awt.image.BufferedImage
import scala.concurrent._
import ExecutionContext.Implicits.global
import java.awt.{RenderingHints, Graphics2D}
import com.siigna.app.Siigna
import com.siigna.app.model.Drawing
import com.siigna.app.view.Graphics
import scala.util.Try
import com.siigna.app.model.shape.CircleShape

/**
 * A tile represents a part of the drawing that can be rendered.
 * @param drawing  The drawing to retrieve data from.
 * @param window  The dimensions of this tile in drawing-coordinates.
 * @param scale  The scale to adjust the shapes to.
 *
 */
case class Tile(drawing : Drawing, window : SimpleRectangle2D, scale : Double, func : Graphics2D => Graphics) {

  /**
   * The image retrieved after calculation, for synchronous access.
   */
  protected var _image : Option[BufferedImage] = None

  /**
   * Renders the [[com.siigna.app.model.Drawing]] in the given area and outputs an image with the same dimensions as
   * the area and with the shapes drawn in the.
   * @return  An image with the same proportions of the given area with the shapes from that area drawn on it.
   * @throws  IllegalArgumentException  if the width or height of the given screen are zero
   */
  protected val _promise : Promise[BufferedImage] = promise[BufferedImage]() completeWith{
    val f = future {
      // Create a width and height of at least 1
      val width  = if (window.width * scale > 0) (window.width * scale).toInt else 1
      val height = if (window.height * scale > 0) (window.height * scale).toInt else 1

      // Create an image with dimensions equal to the width and height of the area
      val image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
      val g = image.getGraphics.asInstanceOf[Graphics2D]
      val graphics : Graphics = func(g)

      // Setup anti-aliasing
      val antiAliasing = Siigna.boolean("antiAliasing").getOrElse(true)
      val hints = if (antiAliasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF
      g setRenderingHint(RenderingHints.KEY_ANTIALIASING, hints)

      // Create the transformation matrix to move the shapes to (0, 0) of the image
      val transformation = TransformationMatrix((Vector2D(-window.topLeft.x, window.topLeft.y) * scale).round, scale).flipY
      //apply the graphics class to the model with g - (adds the changes to the image)
      drawing(window).foreach(t => if (!Drawing.selection.contains(t._1)) {
        graphics.draw(t._2.transform(transformation))
      })

      // Return the image
      image
    }
    f.onComplete(t => _image = t.map(Some.apply).getOrElse(None))
    f
  }

  /**
   * Retrieves the image of the tile synchronously. Useful for painting, since this cannot be done asynchronously.
   * @return  Some[BufferedImage] if the tile has been drawn successfully, None otherwise.
   */
  def image : Option[BufferedImage] = _image

  /**
   * Interrupts the calculation of the tile, if it's not already finished.
   * @return  True if it was interrupted without finishing the rendering, false if it already finished.
   */
  def interrupt() : Boolean = { _promise.tryFailure(new InterruptedException) }

  /**
   * Executes the given function when the tile-rendering has completed.
   * @param f  The function to call when the tile has been rendered
   * @tparam T  The return-type of the callback function.
   */
  def onComplete[T](f : Try[BufferedImage] => T) { _promise.future.onComplete(f) }

}
