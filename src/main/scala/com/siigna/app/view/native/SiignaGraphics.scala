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

import com.siigna.app.view.Graphics
import java.awt.Graphics2D
import com.siigna.app.model.shape._
import com.siigna.util.geom.TransformationMatrix
import com.siigna.app.model.Drawing

/**
 * Siignas own implementation of the [[com.siigna.app.view.Graphics]] trait.
 */
class SiignaGraphics(val AWTGraphics : Graphics2D) extends Graphics {

  def draw(shape : Shape) {
    // Synchronize for thread-safety
    synchronized {

      val attributes = shape.attributes
      val transformation = attributes.transformationMatrix("Transform").getOrElse(TransformationMatrix())
      val transformedShape = shape.transform(transformation)

      // Retrieve the color-value
      val color = attributes color "Color" getOrElse colorDraw

      // Set the server-color
      setColor(color)

      if (attributes.boolean("Visible") != Some(false)) {
        transformedShape match {
          case s : ArcShape         => {
            setStrokeWidth(attributes double "StrokeWidth" getOrElse 1.0)
            drawArc(s.geometry.center, s.geometry.radius, s.geometry.startAngle, s.geometry.angle)
          }
          case s : CircleShape      => {
            setStrokeWidth(attributes double "StrokeWidth"  getOrElse 1.0)
            drawCircle(s.center, s.radius)
            //draw(s.center)
          }
          case s : LineShape        => {
            setStrokeWidth(attributes double "StrokeWidth" getOrElse 0.6)
            if (attributes.boolean("Infinite").getOrElse(false))
              drawLine(s.p1, s.p2)
            else
              drawSegment(s.p1, s.p2)
          }

          case s : RectangleShape => {
            val r = s.geometry.segments.foreach(s => drawSegment(s.p1, s.p2))
          }

          /** COLLECTION SHAPES **/
          // TODO: What about the attributes from the collection-shapes?!
          case s : PolylineShape    => {
            // Examine the raster attribute
            val raster = attributes color "Raster"

            // Draw the raster if it's defined
            if (raster.isDefined) {
              var px = Seq[Int]()
              var py = Seq[Int]()
              s.geometry.vertices.foreach(p => {
                px :+= p.x.toInt
                py :+= p.y.toInt
              })
              AWTGraphics setColor raster.get
              AWTGraphics.fillPolygon(px.toArray, py.toArray, px.size)

              // Draw the outline if the color is different
              if (color != raster) s.shapes.foreach(s => draw(s.setAttributes(attributes)))
            } else {
              s.shapes.foreach(s => draw(s.setAttributes(attributes)))
            }

          }
          case s : TextShape        => {
            val adjustToScale = attributes boolean "AdjustToScale" getOrElse false
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

}
