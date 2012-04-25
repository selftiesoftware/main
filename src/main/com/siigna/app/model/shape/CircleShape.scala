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

import com.siigna.util.dxf.DXFSection
import com.siigna.util.geom.{Rectangle2D, Circle2D, TransformationMatrix, Vector2D}
import com.siigna.util.collection.{Preferences, Attributes}

/**
 * This class represents a circle.
 *
 * You can use the following attributes:
 * <pre>
 *  - Color        Color   The color of the circle.
 *  - StrokeWidth  Double  The width of the line-stroke used to draw.
 * </pre>
 */
case class CircleShape(center : Vector2D, radius : Double, attributes : Attributes) extends EnclosedShape {

  type T = CircleShape

  val geometry = Circle2D(center, radius)

  /**
   * Graphical handles for the circle.
   * A handle is a point on a circle used for visual feedback and manipulation
   * of the circle.
   */
  val handleE = Vector2D(center.x + radius, center.y)
  val handleN = Vector2D(center.x, center.y + radius)
  val handleW = Vector2D(center.x - radius, center.y)
  val handleS = Vector2D(center.x, center.y - radius)
  val handles = Seq(handleE, handleN, handleW, handleS)
  
  def apply(part : ShapePart) = part match {
    case FullShapePart | SmallShapePart(_) => Some(new PartialShape(transform))
    case _ => None
  }

  def delete(part: ShapePart) = part match {
    case SmallShapePart(_) | FullShapePart => None
    case _ => Some(this)
  }

  /**
   * The distance to the closest handle from a given point.
   */
  def distanceToHandlesFrom(point : Vector2D) = handles.map(_ distanceTo(point)).reduceLeft((a, b) => if(a < b) a else b)

  def select(rect: Rectangle2D) = if (rect.contains(geometry)) FullShapePart else EmptyShapePart

  def select(point: Vector2D) = if (distanceTo(point) < Preferences.double("selectionDistance")) FullShapePart else EmptyShapePart

  def setAttributes(attributes : Attributes) = new CircleShape(center, radius, attributes)

  // TODO export circles
  def toDXF = DXFSection(List())

  def transform(t : TransformationMatrix) =
    CircleShape(center transform(t), radius * t.scaleFactor, attributes)

}

object CircleShape
{

  def apply(center : Vector2D, p : Vector2D) = new CircleShape(center, (center - p).length, Attributes())

}