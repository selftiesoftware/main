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

package com.siigna.app.view

import java.awt.{Graphics2D, BasicStroke, Color}
import java.awt.font._
import java.awt.geom.{Arc2D => JavaArc}
import com.siigna.app.model.shape._
import com.siigna.util.geom._
import com.siigna.util.Implicits._
import com.siigna.app.Siigna

/**
 * A wrapper class for the Graphics class from AWT.
 *
 * <h2>Drawing</h2>
 * <p>
 *   The Graphics is made primarily to draw [[com.siigna.app.model.shape.Shape]]s on a given underlying
 *   Java [[java.awt.Graphics]] object. The behavior of the drawing method changes according to the
 *   [[com.siigna.util.collection.Attributes]] in the shape. The exact behaviour is described in each
 *   [[com.siigna.app.model.shape.Shape]], but for instance "<code>Color</code>" is used in a number of shapes.
 * </p>
 *
 * <h2>Transforming</h2>
 * <p>
 *   An important step in drawing is the act of transforming shapes to fit the right view. As described in
 *   [[com.siigna.app.view.View]] the screen you are currently looking on have a coordinate space of (0, 0) to
 *   (width, height). A drawing has one that originates in the center and can resize dynamically. This causes some
 *   issues when we try to translate one to the other. So is you need to translate a screen-coordinate into a
 *   drawing coordinate use the <code>[[com.siigna.app.view.View]].deviceTransformation</code> (for instance a
 *   mouse position into a drawing-coordinate), but if you need to take a shape from the drawing and draw it upon
 *   the screen, use the <code>[[com.siigna.app.view.View]].drawingTransformation</code>.
 *   <br />
 *   See [[com.siigna.util.geom.TransformationMatrix]] for a more detailed description.
 * </p>
 *
 * <h2>Overriding default behavious</h2>
 * <p>
 *   The Siigna Graphics object can be created via the Graphics object like so:
 *   {{{
 *     val javaGraphics = ... // This should be received from Java's paint method or extracted from an image
 *     val siignaGraphics = Graphics(javaGraphics)
 *   }}}
 *   This behaviour can be overridden if you wish to use another graphics object to do your painting. Please refer
 *   to the companion object for details, or see the [[com.siigna.app.view.native.SiignaGraphics]] for an example
 *   on an implementation.
 * </p>
 * @see [[com.siigna.util.geom.TransformationMatrix]].
 * @see [[com.siigna.app.view.View]].
 */
trait Graphics {

  /**
   * Get the rendering context of the Font within this graphics context.
   *
   * @see java.lang.awt.Graphics2D#getFontRenderContext
   */
  lazy val fontRenderContext = AWTGraphics getFontRenderContext()

  /**
   * The underlying AWT graphics object to draw upon.
   * @return  A Graphics2D instance, used to forward all methods for drawing.
   */
  def AWTGraphics : Graphics2D

  def colorBackground  = Siigna.color("colorBackground").getOrElse("#F9F9F9".color)
  def colorDraw        = Siigna.color("colorDraw").getOrElse("#000000".color)
  def colorSelected    = Siigna.color("colorSelected").getOrElse("#7777FF".color)

  /**
   * Draws a given shape with the conditions given by its attributes. If none is set, default color, width etc. will
   * be used.
   *
   * @param shape  The [[com.siigna.app.model.shape.Shape]] to be drawn.
   */
  def draw(shape : Shape)

  /**
   * Draws a circle with a radius of 4 pixels around the given point. The color defaults to a transparent grey.
   * @param point  The point to draw.
   * @param color  The color to draw the point in.
   */
  def draw(point : Vector2D, color : Color = new Color(50, 50, 50, 100)) {
    setColor(color)
    drawCircle(point, 4, fill = true)
  }

  /**
   * Draws a arc from a center-point, a radius and a start-angle and end-angle,
   * defined in degrees.
   */
  def drawArc(center : Vector2D, radius : Double, startAngle : Double, arcAngle : Double) {
    // We're using Arc2D.Double instead of the function 'drawArc', since Arc2D.Double is using
    // doubles (weird enough) and are thus more precise.
    val arc2d = new JavaArc.Double(center.x - radius, center.y - radius, radius * 2, radius * 2,
                                   startAngle+180, arcAngle, JavaArc.OPEN)
    AWTGraphics draw arc2d
  }

  /**
   * Draws a circle from a center-point and a radius
   */
  def drawCircle(center : Vector2D, radius : Double, fill : Boolean = false) {
    if (fill) {
      AWTGraphics fillArc(center.x.toInt - radius.toInt, center.y.toInt - radius.toInt,
                          radius.toInt * 2, radius.toInt * 2, 0, 360)
    } else {
      AWTGraphics drawArc(center.x.toInt - radius.toInt, center.y.toInt - radius.toInt,
                          radius.toInt * 2, radius.toInt * 2, 0, 360)
    }
  }

  /**
   * Draws an ellipse from a center-point and the vertical and horizontal radius
   */
  def drawEllipse(center : Vector2D, a : Double, b : Double) {
    AWTGraphics drawOval(center.x.toInt - a.toInt, center.y.toInt - b.toInt, (a * 2).toInt, (b * 2).toInt)
  }

  /**
   * Draws an endless line.
   */
  def drawLine(p1 : Vector2D, p2 : Vector2D) {
    val boundary = View.screen
    val intersections = boundary.intersections(Line(p1, p2))
    if (intersections.size >= 2) drawSegment(intersections.head, intersections.tail.head)
  }

  /**
   * Draws a rectangle from two points
   */
  def drawRectangle(p1 : Vector2D, p2 : Vector2D) {
    val topLeft     = Vector2D(scala.math.min(p1.x, p2.x), scala.math.max(p1.y, p2.y))
    val topRight    = Vector2D(scala.math.max(p1.x, p2.x), scala.math.max(p1.y, p2.y))
    val bottomLeft  = Vector2D(scala.math.min(p1.x, p2.x), scala.math.min(p1.y, p2.y))
    val bottomRight = Vector2D(scala.math.max(p1.x, p2.x), scala.math.min(p1.y, p2.y))

    drawSegment(topLeft, topRight)
    drawSegment(topRight, bottomRight)
    drawSegment(bottomRight, bottomLeft)
    drawSegment(bottomLeft, topLeft)
  }

  /**
   * Draws a line-segment (limited by two points) from two points
   */
  def drawSegment(p1 : Vector2D, p2 : Vector2D) {
    AWTGraphics drawLine(p1.x.toInt, p1.y.toInt, p2.x.toInt, p2.y.toInt)
  }

  /**
   * Draws text using Javas TextLayout feature.
   */
  def drawText(layout : TextLayout, position : Vector2D) {
    layout draw(AWTGraphics, position.x.toFloat, position.y.toFloat)
  }

  /**
   * Sets the pen color to use for server (including text).
   *
   * @param  color  the pen color to use.
   */
  def setColor(color : Color) {
    AWTGraphics setColor color
  }

  /**
   * Sets the pen width to use for server (excluding text).
   *
   * @param  width  the pen width to use.
   */
  def setStrokeWidth(width : Double) {
    AWTGraphics setStroke new BasicStroke(width.asInstanceOf[Float]*2.54f) // the width is multiplied by 2.54 to the a value in inches.
  }
}