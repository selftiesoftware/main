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
import com.siigna.app.view.View

/**
 * A tile represents a part of the drawing that can be rendered.
 * @param drawing  The drawing to retrieve data from.
 * @param view  The view to fetch zoom, pan and [[com.siigna.util.geom.TransformationMatrix]]'es from.
 * @param screen  The part of the screen this tile should render, in screen-coordinates.
 */
class Tile(drawing : Drawing, view : View, screen : SimpleRectangle2D) {

  // Create a 'window' of the drawing, as seen through the screen. That is, the view in the drawing-coordinates
  lazy val window = screen.transform(view.deviceTransformation)

  /**
   * Renders the [[com.siigna.app.model.Drawing]] in the given area and outputs an image with the same dimensions as
   * the area and with the shapes drawn in the.
   * @return  An image with the same proportions of the given area with the shapes from that area drawn on it.
   * @throws  IllegalArgumentException  if the width or height of the given screen are zero
   */
  val image : Promise[BufferedImage] = promise[BufferedImage]() completeWith future {
    // Create a width and height of at least 1
    val width  = if (screen.width > 0) screen.width.toInt else 1
    val height = if (screen.height > 0) screen.height.toInt else 1

    // Create an image with dimensions equal to the width and height of the area
    val image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
    val g = image.getGraphics.asInstanceOf[Graphics2D]
    val graphics = view.graphics(g)

    // Setup anti-aliasing
    val antiAliasing = Siigna.boolean("antiAliasing").getOrElse(true)
    val hints = if (antiAliasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF
    g setRenderingHint(RenderingHints.KEY_ANTIALIASING, hints)

    // Create the transformation matrix to move the shapes to (0, 0) of the image
    val scale       = view.drawingTransformation.scale

    val transformation = TransformationMatrix((Vector2D(-window.topLeft.x, window.topLeft.y) * scale).round, scale).flipY
    //apply the graphics class to the model with g - (adds the changes to the image)
    drawing(window).foreach(t => if (!Drawing.selection.contains(t._1)) {
      graphics.draw(t._2.transform(transformation))
    })

    // Return the image
    image
  }

}
