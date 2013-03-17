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
              AWTGraphics setColor raster.get
              AWTGraphics.fillPolygon(px.toArray, py.toArray, px.size)

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

}
