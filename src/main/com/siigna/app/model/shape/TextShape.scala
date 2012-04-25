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

package com.siigna.app.model.shape

import java.awt.Font
import java.awt.font._

import com.siigna.util.collection.Attributes
import com.siigna.util.dxf.{DXFSection, DXFValue}
import com.siigna.util.geom._

/**
 * This class represents a text-string.
 *
 * You can use the following attributes:
 * <pre>
 *  - AdjustToScale  Boolean Whether the text is scaled with the scale of the paper. Defaults to false. 
 *  - Color          Color   The color for the text.
 *  - FontSize       Int     The size of the text.
 *  - TextAlignment  Vector  The alignment-vector for the textbox, defined
 *                           as a vector between (0, 0) and (1, 1).
 * </pre>
 *
 * TODO: Redo this! Completely.
 */
case class TextShape(text: String, position : Vector2D, scale : Double, attributes : Attributes) extends Shape {

  type T = TextShape

  final val GlobalFontScale = 0.1

  val geometry = Rectangle2D(boundaryPosition, boundaryPosition + boundarySize)

  val points = Iterable(position)

  def alignment           = attributes.vector2D("TextAlignment") getOrElse (Vector(0, 0))

  def alignmentPosition   = Vector2D(alignment.x * boundarySize.x, alignment.y * boundarySize.y)

  def apply(part : ShapePart) = None

  def boundaryPosition    = Vector2D(layout.getBounds.getX, layout.getBounds.getY)

  def boundarySize        = Vector2D(layout.getBounds.getWidth, layout.getBounds.getHeight)

  def delete(part: ShapePart) = part match {
    case FullShapePart | SmallShapePart(_) => None
    case _ => Some(this)
  }

  def fontSize            = attributes double("FontSize") getOrElse(12.0)

  def font                = new Font("SansSerif", Font.PLAIN, (fontSize * scale * GlobalFontScale) toInt)

  /**
   * Defines the layout of the shape.
   * Uses a TransformationMatrix to transform the layout using the relevant
   * attributes.
   * TODO: Implement rotation.
   */
  def layout : TextLayout = {
//    val rotationDouble = attributes.double("Rotation")
//    val rotation = if (rotationDouble.isDefined) rotationDouble.get
//              else if (attributes.int("Rotation").isDefined) attributes.int("Rotation").get.toDouble
//              else 0.0

    val transformation = TransformationMatrix(Vector(0, 0), 1) //.rotate(rotation, position.point)
    new TextLayout(text, font, new FontRenderContext(transformation.t, true, true))
  }

  def select(rect: Rectangle2D) = throw new UnsupportedOperationException("Not yet implemented")

  def select(point: Vector2D) = throw new UnsupportedOperationException("Not yet implemented")

  def setAttributes(attributes : Attributes) = new TextShape(text, position, scale, attributes)

  def toDXF = DXFSection(DXFValue(0, "TEXT"),
                         DXFValue(5, scala.util.Random.nextString(4)),
                         DXFValue(100, "AcDbEntity"),
                         DXFValue(100, "AcDbText"),
                         DXFValue(62, 1),
                         DXFValue(40, fontSize)) // TODO: + position.toDXF +
              DXFSection(DXFValue(1, text),
                         DXFValue(100, "AcDbText"))

  // TODO: Should we be able to scale the text-factor?
  def transform(transformation : TransformationMatrix) =
    TextShape(text,
              position.transform(transformation),
              scale * transformation.scaleFactor,
              attributes)

}

object TextShape
{

  def apply(text : String, position : Vector2D)                    = new TextShape(text, position, 1.0, Attributes())
  def apply(text : String, position : Vector2D, attr : Attributes) = new TextShape(text, position, 1.0, attr)
  def apply(text : String, position : Vector2D, scale : Double)    = new TextShape(text, position, scale, Attributes())

}