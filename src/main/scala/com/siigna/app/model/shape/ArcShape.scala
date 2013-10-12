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

package com.siigna.app.model.shape

import com.siigna.util.geom.{SimpleRectangle2D, TransformationMatrix, Vector2D, Arc2D}
import com.siigna.util.collection.Attributes
import com.siigna.app.Siigna
import com.siigna.app.model.selection._
import com.siigna.util.geom.SimpleRectangle2D
import scala.Some
import com.siigna.app.model.selection.BitSetShapeSelector
import collection.immutable.BitSet

/**
 * This class draws an arc.
 *
 * You can use the following attributes:
 * <pre>
 *  - Color        Color   The color of the arc.
 *  - StrokeWidth  Double  The width of the linestroke used to draw.
 * </pre>
 *
 * @param center  The center of the circle-piece.
 * @param radius  The distance from the center to the periphery.
 * @param startAngle  The angle where the arc starts (counting from 3'clock CCW).
 * @param angle  The angles the arc is spanning.
 *
 * TODO: Refactor so shape-parts include handles
 */
case class ArcShape(center : Vector2D, radius : Double, startAngle : Double, angle : Double, attributes : Attributes) extends BasicShape {

  type T = ArcShape

  val geometry = Arc2D(center, radius, startAngle, angle)

  def delete(part: ShapeSelector) = part match {
    case BitSetShapeSelector(_) | FullShapeSelector => Nil
    case _ => Seq(this)
  }

  def getPart(part : ShapeSelector) = {
    val t : Option[TransformationMatrix => Shape] = part match {
      case FullShapeSelector => {
        Some(transform)
      }
      case ShapeSelector(0)    => Some(tm => ArcShape(geometry.startPoint.transform(tm), geometry.midPoint.transform(tm), geometry.endPoint))
      case ShapeSelector(1)    => Some(tm => ArcShape(geometry.startPoint, geometry.midPoint.transform(tm), geometry.endPoint))
      case ShapeSelector(0, 1) => Some(tm => ArcShape(geometry.startPoint.transform(tm), geometry.midPoint.transform(tm), geometry.endPoint))
      case ShapeSelector(2)    => Some(tm => ArcShape(geometry.startPoint, geometry.midPoint.transform(tm), geometry.endPoint.transform(tm)))
      case ShapeSelector(0, 2) => Some(tm => ArcShape(geometry.startPoint.transform(tm), geometry.midPoint, geometry.endPoint.transform(tm)))
      case ShapeSelector(1, 2) => Some(tm => ArcShape(geometry.startPoint, geometry.midPoint.transform(tm), geometry.endPoint.transform(tm)))
      case ShapeSelector(0, 1, 2) => Some(tm => ArcShape(geometry.startPoint.transform(tm), geometry.midPoint.transform(tm), geometry.endPoint.transform(tm)))
      case _ => None
    }
    t.map((f : TransformationMatrix => Shape) => new PartialShape(this, f))
  }

  def getSelector(rect: SimpleRectangle2D) = if (rect.contains(geometry)) FullShapeSelector else
  {
    ShapeSelector(BitSet(geometry.vertices.zipWithIndex.filter(t => rect.contains(t._1)).map(_._2):_*))
  }

  def getSelector(point: Vector2D) = {
    var v0 : Boolean = false
    var v1 : Boolean = false
    var v2 : Boolean = false
    if (geometry.startPoint.distanceTo(point) < Siigna.selectionDistance) v0 = true
    if (geometry.midPoint.distanceTo(point) < Siigna.selectionDistance) v1 = true
    if (geometry.endPoint.distanceTo(point) < Siigna.selectionDistance) v2 = true
    if( v0 == false & v1 == false && v2 == false ) EmptyShapeSelector
    else if( v0 == true & v1 == false && v2 == false ) ShapeSelector(0)
    else if( v0 == true & v1 == true && v2 == false ) ShapeSelector(0,1)
    else if( v0 == true & v1 == false && v2 == true ) ShapeSelector(0,2)
    else if( v0 == true & v1 == true && v2 == true ) ShapeSelector(0,1,2)
    else if( v0 == false & v1 == true && v2 == false ) ShapeSelector(1)
    else if( v0 == false & v1 == true && v2 == true ) ShapeSelector(1,2)
    else if( v0 == false & v1 == false && v2 == true ) ShapeSelector(2)
    else EmptyShapeSelector
  }
  
  def getShape(s : ShapeSelector) = s match {
    case FullShapeSelector => Some(this)
    case _ => None
  }

  def getVertices(selector: ShapeSelector) = selector match {
    case FullShapeSelector   => geometry.vertices
    case ShapeSelector(0)    => Seq(geometry.startPoint)
    case ShapeSelector(1)    => Seq(geometry.midPoint)
    case ShapeSelector(0, 1) => Seq(geometry.startPoint, geometry.midPoint)
    case ShapeSelector(2)    => Seq(geometry.endPoint)
    case ShapeSelector(0, 2) => Seq(geometry.startPoint, geometry.endPoint)
    case ShapeSelector(1, 2) => Seq(geometry.midPoint, geometry.endPoint)
    case ShapeSelector(0, 1, 2) => Seq(geometry.startPoint, geometry.midPoint, geometry.endPoint)
    case _ => Seq()
  }

  def setAttributes(attributes : Attributes) = new ArcShape(center, radius, startAngle, angle, attributes)

  def transform(t : TransformationMatrix) = {
      ArcShape(t.transform(center),
               radius * t.scale,
               startAngle, angle,
               attributes)
  }
}

/**
 * Companion object to ArcShape.
 */
object ArcShape
{

  /**
   * Creates an arc from three given points by calculating the center and setting the right radius and angles.
   * @param start  The vector where the arc starts (CCW).
   * @param middle  The middle vector of the arc.
   * @param end  The vector where the arc stops (CCW).
   * @return  An ArcShape with empty attributes
   */
  def apply(start : Vector2D, middle : Vector2D, end : Vector2D) : ArcShape = {
    val a = Arc2D(start, middle, end)
    new ArcShape(a.center, a.radius, a.startAngle, a.angle, Attributes())
  }

  /**
   * Creates an arc with the given center, radius and angles.
   * @param center  The center of the arc.
   * @param radius  The radius of the arc.
   * @param startAngle  The start angle in degrees given from 3 o'clock and CCW.
   * @param endAngle  The end angle in degrees CCW from 3 o'clock.
   * @return  An ArcShape with empty attributes
   */
  def apply(center : Vector2D, radius : Double, startAngle : Double, endAngle : Double) : ArcShape = {
    new ArcShape(center, radius, startAngle, endAngle, Attributes())
  }

  /**
   * Creates an arc from the given geometry.
   * @param geometry  The [[com.siigna.util.geom.Arc2D]] geometry containing the information to create the arc
   * @return  An ArcShape with empty attributes
   */
  def apply(geometry : Arc2D) = {
    new ArcShape(geometry.center, geometry.radius, geometry.startAngle, geometry.endAngle, Attributes())
  }

}