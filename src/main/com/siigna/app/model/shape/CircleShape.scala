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

//import com.siigna.util.dxf.DXFSection
import com.siigna.util.geom.{Rectangle2D, Circle2D, TransformationMatrix, Vector2D}
import com.siigna.util.collection.{Attributes}
import com.siigna.app.Siigna

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
  
  def apply(part : ShapeSelector) = part match {
    case CircleShape.Selector(xs) => {
      Some(new PartialShape((t : TransformationMatrix) => LineShape(
        if(xs)  center.transform(t) else center,
        if(!xs) p2.transform(t)
        else p2,
        attributes)))
    }
    case FullSelector => Some(new PartialShape(transform))
    case _ => None
  }

  def delete(part: ShapeSelector) = part match {
    //case PartialSelector(_) | FullSelector => None
    case _ => Some(this)
  }

  /**
   * The distance to the closest handle from a given point.
   */
  def distanceToHandlesFrom(point : Vector2D) = geometry.vertices.map(_ distanceTo(point)).reduceLeft((a, b) => if(a < b) a else b)

  def getPart(rect: Rectangle2D) = 
    if (rect.contains(geometry)) FullSelector 
    else {
      val contains = geometry.vertices.exists(p => rect.contains(p))
      if (contains) {
        FullSelector
      } else EmptySelector
    }

  def getPart(point: Vector2D) =
    if (distanceTo(point) > Siigna.int("selectionDistance").getOrElse(5)) EmptySelector
    else {
      FullSelector
    }

  def getVertices(selector: ShapeSelector) = selector match {
    case FullSelector => geometry.vertices
    //case PartialSelector(1) => Seq(center)
    case _ => Seq()
  }

  def setAttributes(attributes : Attributes) = new CircleShape(center, radius, attributes)

  def transform(t : TransformationMatrix) =
    CircleShape(center transform(t), radius * t.scaleFactor, attributes)
  
}

object CircleShape
{                     

  /**
   * The selector for CircleShapes.
   */
  sealed case class Selector(part : Boolean) extends ShapeSelector

  def apply(center : Vector2D, p : Vector2D) = new CircleShape(center, (center - p).length, Attributes())

}