/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.model.shape

import com.siigna.util.geom._
import com.siigna.util.collection.Attributes
import com.siigna.app.model.selection._
import com.siigna.app.Siigna
import com.siigna.util.geom.ComplexRectangle2D
import com.siigna.util.geom

/**
 * A Rectangle shape.
 *
 * You can use the following attributes:
 * {{{
 *  - Color        Color                 The color of the line.
 *  - StrokeWidth  Double                The width of the line used to draw.
 * }}}
 *
 * @param center  The center point of the rectangle.
 * @param width  The width of the rectangle, from the left-most border to the right-most.
 * @param height The height of the rectangle, from the top-most border to the lowest border.
 * @param rotation  The rotation of the rectangle, defined counter-clockwise where 0 degrees equals 3 o'clock.
 * @param attributes  The [[com.siigna.util.collection.Attributes]] of the shape.
 */
case class RectangleShape(center : Vector2D, width : Double, height : Double, rotation : Double, attributes : Attributes) extends ClosedShape {

  type T = RectangleShape

  val geometry : ComplexRectangle2D = ComplexRectangle2D(center,width,height,rotation)

  /**
   * In a rectangle with zero rotation this is the upper right vertex. The point will be rotated around the center
   * starting at 3 o'clock moving counter-clockwise.
   */
  val p0 = geometry.p0 //BitSet 0

  /**
   * In a rectangle with zero rotation this is the upper left vertex. The point will be rotated around the center
   * starting at 3 o'clock moving counter-clockwise.
   */
  val p1 = geometry.p1 //BitSet 1

  /**
   * In a rectangle with zero rotation this is the lower left vertex. The point will be rotated around the center
   * starting at 3 o'clock moving counter-clockwise.
   */
  val p2 = geometry.p2 //BitSet 2

  /**
   * In a rectangle with zero rotation this is the lower right vertex. The point will be rotated around the center
   * starting at 3 o'clock moving counter-clockwise.
   */
  val p3 = geometry.p3 //BitSet 3

  def delete(part : ShapeSelector) = part match {
    // Three or more points
    case FullShapeSelector => Nil
    case s : BitSetShapeSelector if s.size >= 3 => Nil
    // Two opposite points
    case ShapeSelector(0, 2) | ShapeSelector(1, 3) => Nil
    // Two adjacent points
    case BitSetShapeSelector(xs) if xs.size == 2 => {
      val seg = geometry.vertices.zipWithIndex.filter(t => !xs.contains(t._2))
      val p0 = seg.head._1
      val p1 = seg.tail.head._1
      Seq(LineShape(p0, p1))
    }
    // One point
    case ShapeSelector(x) => {
      val (head, tail) = geometry.vertices.splitAt(x)
      Seq(PolylineShape(tail.tail ++ head))
    }
    case _ => Seq(this)
  }

  /**
   * Creates a [[com.siigna.app.model.shape.PartialShape]] where the selected point and the two adjacent points are transformed.
   * @param x  The adjacent point CCW
   * @param y  The selected point
   * @param z  The adjacent point CW
   * @return A Some[PartialShape] that transforms three points.
   */
  protected def onePointPartialShape(x : Vector2D, y: Vector2D, z: Vector2D) =
    Some(new PartialShape(this, (f : TransformationMatrix) => {

      //evaluate the first line to be moved
      val line1 = Line(z, y)
      val translation1 = f.translation + line1.center
      val closestPoint1 = line1.closestPoint(translation1)
      val deltaX = translation1.x - closestPoint1.x
      val movedLine1 = line1.transform(f)

      //evaluate the second line to be moved
      val line2 = Line(x, y)
      val translation2 = f.translation + line2.center
      val closestPoint2 = line2.closestPoint(translation2)
      val deltaY = translation2.y - closestPoint2.y
      val movedLine2 = line2.transform(f)

      //find the center for the new rectangle
      val movedCenter = this.center + Vector2D(deltaX, deltaY) / 2

      new RectangleShape(movedCenter,
          movedCenter.distanceTo(movedLine1) * 2,
          movedCenter.distanceTo(movedLine2) * 2,
        rotation + f.rotation, attributes)
    }))

  /**
   * Creates a [[com.siigna.app.model.shape.PartialShape]] where only the two selected points are transformed.
   * @param x  The first selected point
   * @param y  The second selected point
   * @param isWidth  A boolean value indicating whether it is the width (true) or  height (false) that should be
   *                 manipulated.
   * @return A Some[PartialShape] that only transforms the two selected points.
   */
  protected def twoPointPartialShape(x : Vector2D, y : Vector2D, isWidth : Boolean) =
    Some(new PartialShape(this, (f : TransformationMatrix) => {
      val line = Line(x, y)
      val translation = f.translation + line.center
      val closestPoint = line.closestPoint(translation)
      val delta = translation - closestPoint
      val movedLine = line.transform(f)
      val movedCenter = this.center + delta / 2

      new RectangleShape(movedCenter,
        if (isWidth) movedCenter.distanceTo(movedLine) * 2 else width,
        if (isWidth) height else movedCenter.distanceTo(movedLine) * 2,
        rotation + f.rotation, attributes)
    }))

  def getPart(part : ShapeSelector) : Option[PartialShape] = part match {
    case FullShapeSelector => Some(new PartialShape(this, transform))
    case x : BitSetShapeSelector if x.size >= 3 => Some(new PartialShape(this, transform))
    case ShapeSelector(0) => onePointPartialShape(p1, p0, p3)
    case ShapeSelector(1) => onePointPartialShape(p0, p1, p2)
    case ShapeSelector(2) => onePointPartialShape(p3, p2, p1)
    case ShapeSelector(3) => onePointPartialShape(p2, p3, p0)
    case ShapeSelector(0, 1) => twoPointPartialShape(p0, p1, isWidth = false)
    case ShapeSelector(0, 3) => twoPointPartialShape(p0, p3, isWidth = true)
    case ShapeSelector(1, 2) => twoPointPartialShape(p1, p2, isWidth = true)
    case ShapeSelector(2, 3) => twoPointPartialShape(p2, p3, isWidth = false)
    case ShapeSelector(0, 2) | ShapeSelector(1, 3) => None
    case _ => None
  }

  def getSelector(p : Vector2D) = {
    // Confine a number to a range between 0 - 3
    def confineToIndices(i : Int) = if (i > 3) 0 else if (i < 0) 3 else i
    def selectorFromSegmentId(id : Int) = ShapeSelector(id, confineToIndices(id + 1))

    val selectionDistance = Siigna.selectionDistance
    geometry.vertices.zipWithIndex.filter(_._1.distanceTo(p) < selectionDistance) match {
      case Seq(t) => ShapeSelector(t._2)
      case Seq(t1, t2) => ShapeSelector(t1._2, t2._2)
      case _ => { // Test for segments
        geometry.segments.zipWithIndex.filter(_._1.distanceTo(p) < selectionDistance) match {
          case Seq(t) => selectorFromSegmentId(t._2)
          case s : Seq[(Segment2D, Int)] if s.size > 1 => {
            selectorFromSegmentId(s.reduceLeft((a, b) => if (a._1.distanceTo(p) < b._1.distanceTo(p)) a else b)._2)
          }
          case _ => EmptyShapeSelector
        }
      }
    }
  }

  def getSelector(r : SimpleRectangle2D) : ShapeSelector = {
    val xs = geometry.vertices.zipWithIndex.filter(t => r.contains(t._1)).map(_._2)
    if (xs.size >= 4) FullShapeSelector else if (xs.isEmpty) EmptyShapeSelector else ShapeSelector(xs:_*)
  }

  def getShape(s : ShapeSelector) = s match {
    case FullShapeSelector => Some(this)
    case s : BitSetShapeSelector if s.size >= 4 => Some(this)
    case s : BitSetShapeSelector if s.size == 3 => {
      // First find the index that is missing
      var i = 0
      for (n <- 0 to 3) {
        if (!s(n)) i = n
      }
      // Then use it to create two lists of vertices and connect the dots
      val (head, tail) = geometry.vertices.splitAt(i)
      Some(PolylineShape(tail.tail ++ head))
    }
    case ShapeSelector(0, 2) => None
    case ShapeSelector(1, 3) => None
    case s : BitSetShapeSelector if s.size == 2 => {
      val xs = s.map(geometry.vertices.apply)
      Some(LineShape(xs.head, xs.last))
    }
    case _ => None
  }

  def getVertices(selector: ShapeSelector) = {
    selector match {
      case FullShapeSelector => geometry.vertices
      case s : BitSetShapeSelector if s.size == 4 => geometry.vertices
      case BitSetShapeSelector(xs) => geometry.vertices.zipWithIndex.filter(t => xs(t._2)).map(_._1).toSeq
      case _ => Seq()
    }
  }

  def setAttributes(attributes : Attributes) = RectangleShape(center, width,height,rotation, attributes)

  def transform(t : TransformationMatrix) = {
    RectangleShape(center transform t, width * t.scaleX, height * t.scaleY,
      geom.normalizeDegrees(rotation + t.rotation), attributes)
  }
}

/**
 * Companion object to [[com.siigna.app.model.shape.RectangleShape]].
 */
object RectangleShape {

  /**
   * Creates a rectangle from a center, width, height and rotation but with an empty set of attributes.
   * @param center  The center point of the rectangle.
   * @param width  The width of the rectangle, from the left-most border to the right-most.
   * @param height The height of the rectangle, from the top-most border to the lowest border.
   * @param rotation  The rotation of the rectangle, defined counter-clockwise where 0 degrees equals 3 o'clock.
   * @return  An instance of a [[com.siigna.app.model.shape.RectangleShape]]
   */
  def apply(center : Vector2D, width : Double, height : Double, rotation : Double) : RectangleShape =
    new RectangleShape(center, width, height, geom.normalizeDegrees(rotation) ,Attributes())

  /**
   * Creates a rectangle with 0 (zero) rotation spanning between the two given points.
   * @param p1 One of the corners of the rectangle.
   * @param p2 The other corner of the rectangle.
   * @return  An instance of a [[com.siigna.app.model.shape.RectangleShape]]
   */
  def apply(p1 : Vector2D, p2 : Vector2D) : RectangleShape =
    new RectangleShape((p1+p2)/2, (p2-p1).x.abs, (p2-p1).y.abs, 0, Attributes())

  /**
   * Creates a rectangle with 0 (zero) rotation spanning between two points, described by the four coordinates.
   * @param x1 The first x-coordinate.
   * @param y1 The first y-coordinate.
   * @param x2 The second x-coordinate.
   * @param y2 The second y-coordinate.
   * @return  An instance of a [[com.siigna.app.model.shape.RectangleShape]]
   */
  def apply(x1 : Double, y1 : Double, x2 : Double, y2 : Double) : RectangleShape =
    apply(Vector2D(math.min(x1, x2),math.min(y1, y2)),Vector2D(math.max(x1, x2),math.max(y1, y2)))
}