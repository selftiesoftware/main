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

import com.siigna.util.geom.{SimpleRectangle2D, Circle2D, TransformationMatrix, Vector2D}
import com.siigna.util.collection.Attributes
import com.siigna.app.Siigna
import com.siigna.app.model.selection.{BitSetShapeSelector, FullShapeSelector, EmptyShapeSelector, ShapeSelector}
import scala.collection.immutable.BitSet

/**
 * This class represents a circle.
 *
 * You can use the following attributes:
 * <pre>
 *  - Color        Color   The color of the circle.
 *  - StrokeWidth  Double  The width of the line-stroke used to draw.
 * </pre>
 */
case class CircleShape(center : Vector2D, radius : Double, attributes : Attributes) extends ClosedShape {

  type T = CircleShape

  val geometry = Circle2D(center, radius)

  def delete(part: ShapeSelector) = part match {
    case BitSetShapeSelector(_) | FullShapeSelector => Nil
    case _ => Seq(this)
  }

  override def distanceTo(point : Vector2D, scale : Double) = {
    val distToGeometry = geometry.distanceTo(point)
    val distToCenter   = center.distanceTo(point)
    (if (distToGeometry < distToCenter) distToGeometry else distToCenter) * scale
  }

  /**
   * The distance to the closest handle from a given point.
   * @param point  The point to calculate the distance from this circle.
   * @return  A positive double indicating the distance from the given point to the circle.
   */
  def distanceToHandles(point : Vector2D) = geometry.vertices.map(_.distanceTo(point)).reduceLeft((a, b) => if(a < b) a else b)


  def getPart(part : ShapeSelector) = {
    def createCirclePartFromOppositePoints(transform : Int, static : Int) = {
      Some(new PartialShape(this, t => {
        val p1 = geometry.vertices(transform).transform(t)
        val p2 = geometry.vertices(static)
        val c = (p1 + p2) / 2
        new CircleShape(c, (p2 - c).length, attributes)
      }))
    }

    def createCirclePartFromDirection(direction : Vector2D, static1 : Int, static2 : Int) = {
      val p1 = findMiddlePointOnCircumference(direction)
      val p2 = geometry.vertices(static1)
      val p3 = geometry.vertices(static2)
      Some(new PartialShape(this, t => {
        CircleShape(p1.transform(t), p2, p3, attributes)
      }))
    }

    def findMiddlePointOnCircumference(direction: Vector2D) : Vector2D = {
      center + (direction * (radius / direction.length))
    }

    part match {
      // The full shape (or two opposite points on the periphery)
      case FullShapeSelector | ShapeSelector(0) | ShapeSelector(1, 2, 3, 4) |
           ShapeSelector(1, 3) | ShapeSelector(0, 1, 4) |
           ShapeSelector(2, 4) | ShapeSelector(0, 1, 4) => Some(new PartialShape(this, transform))
      // Transform a single point on the periphery
      case ShapeSelector(1) | ShapeSelector(0, 1) => createCirclePartFromOppositePoints(1, 3)
      case ShapeSelector(2) | ShapeSelector(0, 2) => createCirclePartFromOppositePoints(2, 4)
      case ShapeSelector(3) | ShapeSelector(0, 3) => createCirclePartFromOppositePoints(3, 1)
      case ShapeSelector(4) | ShapeSelector(0, 4) => createCirclePartFromOppositePoints(4, 2)
      // Transform two adjacent points on the periphery
      case ShapeSelector(1, 2) | ShapeSelector(0, 1, 2) => createCirclePartFromDirection(Vector2D( 1, 1), 3, 4)
      case ShapeSelector(2, 3) | ShapeSelector(0, 2, 3) => createCirclePartFromDirection(Vector2D(-1, 1), 1, 4)
      case ShapeSelector(3, 4) | ShapeSelector(0, 3, 4) => createCirclePartFromDirection(Vector2D(-1,-1), 1, 2)
      case ShapeSelector(1, 4) | ShapeSelector(0, 1, 4) => createCirclePartFromDirection(Vector2D( 1,-1), 2, 3)
      // Transform three points on the periphery
      case ShapeSelector(1, 2, 3) | ShapeSelector(0, 1, 2, 3) => createCirclePartFromOppositePoints(2, 4)
      case ShapeSelector(2, 3, 4) | ShapeSelector(0, 1, 3, 4) => createCirclePartFromOppositePoints(3, 1)
      case ShapeSelector(1, 3, 4) | ShapeSelector(0, 1, 3, 4) => createCirclePartFromOppositePoints(4, 2)
      case ShapeSelector(1, 2, 4) | ShapeSelector(0, 1, 2, 4) => createCirclePartFromOppositePoints(1, 3)

      case _ => None
    }
  }

  def getSelector(rect: SimpleRectangle2D) =
    if (rect.contains(geometry)) FullShapeSelector
    else {
      ShapeSelector(BitSet(geometry.vertices.zipWithIndex.filter(t => rect.contains(t._1)).map(_._2):_*))
    }

  def getSelector(point: Vector2D) = {
    val distanceSelection = Siigna.selectionDistance
    val distanceToCircle = distanceTo(point)
    // If the circumference is far away, but the center is close, select the entire shape
    if (distanceToCircle > distanceSelection && center.distanceTo(point) < distanceSelection
        || center.distanceTo(point) < distanceSelection) {
      FullShapeSelector
    // If nothing is close, select nothing
    } else if (distanceTo(point) > distanceSelection ) {
      EmptyShapeSelector
    // Otherwise we have a hit
    } else {
      val (distanceToVertex, id) = geometry.vertices.map(_.distanceTo(point)).zipWithIndex.min
      // This covers situations where the mouse is closer to the entire circle, than a vertex -> full selection
      if (distanceToCircle * 1.5 < distanceToVertex) FullShapeSelector
      else ShapeSelector(id)
    }
  }

  def getShape(s : ShapeSelector) = s match {
    case FullShapeSelector => Some(this)
    case _ => None
  }

  def getVertices(selector: ShapeSelector) =
    selector match {
      case FullShapeSelector => geometry.vertices
      case BitSetShapeSelector(xs) => xs.toSeq.map(i => geometry.vertices.apply(i))
      case x => Nil
    }

  def setAttributes(attributes : Attributes) = new CircleShape(center, radius, attributes)

  def transform(t : TransformationMatrix) =
    CircleShape(center transform t, radius * t.scale, attributes)
  
}

/**
 * A companion object for [[com.siigna.app.model.shape.CircleShape]].
 */
object CircleShape {

  def apply(center : Vector2D, p : Vector2D) =
    new CircleShape(center, (center - p).length, Attributes())

  def apply(center : Vector2D, p : Vector2D, attributes : Attributes) =
    new CircleShape(center, (center - p).length, attributes)

  def apply(p1 : Vector2D, p2 : Vector2D, p3 : Vector2D) : CircleShape = {
    val c = Circle2D.findCenterPoint(p1, p2, p3)
    new CircleShape(c, (p1 - c).length, Attributes())
  }

  def apply(p1 : Vector2D, p2 : Vector2D, p3 : Vector2D, attributes : Attributes) : CircleShape = {
    val c = Circle2D.findCenterPoint(p1, p2, p3)
    new CircleShape(c, (p1 - c).length, attributes)
  }

  def apply(center : Vector2D, radius : Double) =
    new CircleShape(center, radius, Attributes())
}