/*
 * Copyright (c) 2012. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.view

import java.awt.{Graphics2D, BasicStroke, Color}
import java.awt.font._
import java.awt.geom.{Arc2D => JavaArc}
import com.siigna.app.model.shape._
import com.siigna.util.geom._
import com.siigna.util.Implicits._
import com.siigna.app.Siigna
import com.siigna.app.model.{Selection, Drawing, Model}
import com.siigna.util.event.Snap

/**
 * A wrapper class for the Graphics class from AWT.
 *
 * <h2>Drawing</h2>
 * <p>
 *   The Graphics is made primarily to draw [[com.siigna.app.model.shape.Shape]]s on the given underlying
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
 * @see [[com.siigna.util.geom.TransformationMatrix]].
 * @see [[com.siigna.app.view.View]].
 */
class Graphics(val g : Graphics2D)
{

  /**
   * Get the rendering context of the Font within this graphics context.
   *
   * @see java.lang.awt.Graphics2D#getFontRenderContext
   */
  lazy val fontRenderContext = g getFontRenderContext()

  def colorBackground  = Siigna.color("colorBackground").getOrElse("#F9F9F9".color)
  def colorDraw        = Siigna.color("colorDraw").getOrElse("#000000".color)
  def colorSelected    = Siigna.color("colorSelected").getOrElse("#7777FF".color)

  /**
   * Draws a given shape with the conditions given by its attributes. If none is set, default color, width etc. will
   * be used.
   *
   * @param shape  The [[com.siigna.app.model.shape.Shape]] to be drawn.
   */
  def draw(shape : Shape) {
    // Synchronize for thread-safety
    synchronized {

      val attributes = shape.attributes
      val transformation = attributes.transformationMatrix("Transform").getOrElse(TransformationMatrix())
      val transformedShape = shape.transform(transformation)

      // Retrieve the color-value
      val color = attributes color("Color") getOrElse(colorDraw)

      // Set the server-color
      setColor(color)

      if (attributes.boolean("Visible") != Some(false)) {
        transformedShape match {
          case s : ArcShape         => {
            setStrokeWidth(attributes double("StrokeWidth") getOrElse(1.0))
            drawArc(s.geometry.center, s.geometry.radius, s.geometry.startAngle, s.geometry.angle)
            // Something utterly wrong here...
            //draw(s.start.transform(TransformationMatrix(Vector(0, 0), 1).flipY(s.geometry.center)))
            //draw(s.middle.transform(TransformationMatrix(Vector(0, 0), 1).flipY(s.geometry.center)))
            //draw(s.end.transform(TransformationMatrix(Vector(0, 0), 1).flipY(s.geometry.center)))
          }
          case s : CircleShape      => {
            setStrokeWidth(attributes double("StrokeWidth") getOrElse(1.0))
            drawCircle(s.center, s.radius)
            //draw(s.center)
          }
          /*case s : EllipseShape     => {
            setColor(attributes color("Color") getOrElse(if (selected) colorSelected else colorDraw))
            setStrokeWidth(attributes double("StrokeWidth") getOrElse(1.0))
            drawEllipse(s.center, s.a, s.b)
          }*/
          /*case s : ImageShape       => {
            val color = attributes color("Color") getOrElse(colorBackground)
            val x = s.p1.x.toInt
            val y = s.p1.y.toInt
            val height = scala.math.abs(s.p2.y - s.p1.y).toInt
            val width  = scala.math.abs(s.p2.x - s.p1.x).toInt

            // The image is flipped around the X-axis, to compensate for the original
            // negative y-coordinate in java.
            g drawImage(s.toImage,
                        x, y + height, x + width,        y, // The coordinates of the destination-rectangle
                        0, 0,          s.width,   s.height, // The coordinates of the source-rectangle
                        color, null)
          }*/
          case s : LineShape        => {
            setStrokeWidth(attributes double("StrokeWidth") getOrElse(1.0))
            if (attributes.boolean("Infinite").getOrElse(false))
              drawLine(s.p1, s.p2)
            else
              drawSegment(s.p1, s.p2)
          }

          /** COLLECTION SHAPES **/
          // TODO: What about the attributes from the collection-shapes?!
          case s : PolylineShape    => {
            // Examine the raster attribute
            val raster = attributes color "raster"

            // Draw the raster if it's defined
            if (raster.isDefined) {
              var px = Seq[Int]()
              var py = Seq[Int]()
              s.geometry.vertices.foreach(p => {
                px :+= p.x.toInt
                py :+= p.y.toInt
              })
              g setColor raster.get
              g.fillPolygon(px.toArray, py.toArray, px.size)

              // Draw the outline if the color is different
              if (color != raster) s.shapes.foreach(s => draw(s.setAttributes(attributes)))
            } else {
              s.shapes.foreach(s => draw(s.setAttributes(attributes)))
            }

          }
          case s : TextShape        => {
            val adjustToScale = attributes boolean("AdjustToScale") getOrElse(false)
            val shape : TextShape = if (adjustToScale) {
              s.copy(scale = s.scale * Drawing.boundaryScale)
            } else s
            // Draw!
            drawText(shape.layout, shape.position - shape.boundaryPosition - shape.alignmentPosition)
          }
          case _ =>
        }
      }
    }
  }

  /**
   * Draws a circle with a radius of 4 pixels around the given point. The color defaults to a transparent grey.
   * @param point  The point to draw.
   * @param color  The color to draw the point in.
   */
  def draw(point : Vector2D, color : Color = new Color(50, 50, 50, 100)) {
    setColor(color)
    if(Snap.snapEnabled) drawCircle(point, 4, true)
  }

  /**
   * Draws a arc from a center-point, a radius and a start-angle and end-angle,
   * defined in degrees.
   */
  def drawArc(center : Vector2D, radius : Double, startAngle : Double, arcAngle : Double) {
    // We're using Arc2D.Double instead of the function 'drawArc', since Arc2D.Double is using
    // doubles (weird enough) and are thus more precise.
    val arc2d = new JavaArc.Double(center.x - radius, center.y - radius, radius * 2, radius * 2, startAngle, arcAngle, JavaArc.OPEN)
    g draw(arc2d)
  }

  /**
   * Draws a circle from a center-point and a radius
   */
  def drawCircle(center : Vector2D, radius : Double, fill : Boolean = false) {
    if (fill) {
      g fillArc(center.x.toInt - radius.toInt, center.y.toInt - radius.toInt, radius.toInt * 2, radius.toInt * 2, 0, 360)
    } else {
      g drawArc(center.x.toInt - radius.toInt, center.y.toInt - radius.toInt, radius.toInt * 2, radius.toInt * 2, 0, 360)
    }
  }

  /**
   * Draws an ellipse from a center-point and the vertical and horizontal radius
   */
  def drawEllipse(center : Vector2D, a : Double, b : Double) {
    g drawOval(center.x.toInt - a.toInt, center.y.toInt - b.toInt, (a * 2).toInt, (b * 2).toInt)
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
    val topLeft     = Vector(scala.math.min(p1.x, p2.x), scala.math.max(p1.y, p2.y))
    val topRight    = Vector(scala.math.max(p1.x, p2.x), scala.math.max(p1.y, p2.y))
    val bottomLeft  = Vector(scala.math.min(p1.x, p2.x), scala.math.min(p1.y, p2.y))
    val bottomRight = Vector(scala.math.max(p1.x, p2.x), scala.math.min(p1.y, p2.y))

    drawSegment(topLeft, topRight)
    drawSegment(topRight, bottomRight)
    drawSegment(bottomRight, bottomLeft)
    drawSegment(bottomLeft, topLeft)
  }

  /**
   * Draws a line-segment (limited by two points) from two points
   */
  def drawSegment(p1 : Vector2D, p2 : Vector2D) {
    g drawLine(p1.x.toInt, p1.y.toInt, p2.x.toInt, p2.y.toInt)
  }

  /**
   * Draws text using Javas TextLayout feature.
   */
  def drawText(layout : TextLayout, position : Vector2D) {
    layout draw(g, position.x.toFloat, position.y.toFloat)
  }

  /**
   * Sets the pen color to use for server (including text).
   *
   * @param  color  the pen color to use.
   */
  def setColor(color : Color) {
    g setColor color
  }

  /**
   * Sets the pen width to use for server (excluding text).
   *
   * @param  width  the pen width to use.
   */
  def setStrokeWidth(width : Double) {
    g setStroke(new BasicStroke(width.asInstanceOf[Float]))
  }
}
