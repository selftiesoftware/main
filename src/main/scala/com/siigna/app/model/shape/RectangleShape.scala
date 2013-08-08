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
import scala.Some
import collection.immutable.BitSet

/**
 * A Rectangle shape.
 *
 * TODO: Attributes
 *
 * @param center  The center point of the rectangle.
 * @param width  The width of the rectangle, from the left-most border to the right-most.
 * @param height The height of the rectangle, from the top-most border to the lowest border.
 * @param rotation  The rotation of the rectangle, defined counter-clockwise where 0 degrees equals 3 o'clock.
 * @param attributes  The [[com.siigna.util.collection.Attributes]] of the shape.
 */
case class RectangleShape(center : Vector2D, width : Double, height : Double, rotation : Double, attributes : Attributes) extends ClosedShape {

  type T = RectangleShape

  //rotation needs to be negative to make for clockwise rotation of the rectangle
  val geometry : ComplexRectangle2D = ComplexRectangle2D(center,width,height,rotation)
  val p0 = geometry.p0 //BitSet 0
  val p1 = geometry.p1 //BitSet 1
  val p2 = geometry.p2 //BitSet 2
  val p3 = geometry.p3 //BitSet 3

  def delete(part : ShapeSelector) = part match {
    case BitSetShapeSelector(_) | FullShapeSelector => Nil
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

  //TODO: allow returning selected segments, not just points.
  def getSelector(p : Vector2D) = {
    val selectionDistance = Siigna.selectionDistance
    //find out if a point in the rectangle is close. if so, return true and the point's bit value
    def isCloseToPoint(s : Segment2D, b : BitSet) : (Boolean, BitSet) = {
      val points = List(s.p1, s.p2)
      var hasClosePoint = false
      var bit = BitSet()
      //evaluate is one of the two points of a segment is close
      for(i <- 0 until points.size) {
        if(points(0).distanceTo(p) <= selectionDistance) {
          hasClosePoint = true
          bit = BitSet(b.head)
        }
        else if (points(1).distanceTo(p) <= selectionDistance) {
          hasClosePoint = true
          bit = BitSet(b.last)
        }
      }
      (hasClosePoint, bit)
    }

    //find out if a segment of the rectangle is close. if so, return true, the segment, and the segment's bit value
    def isCloseToSegment : (Boolean, Option[Segment2D], BitSet) = {
      val l = List(Segment2D(p0,p1),Segment2D(p1,p2),Segment2D(p2,p3), Segment2D(p0,p3))
      var closeSegment : Option[Segment2D] = None
      var hasCloseSegment = false
      var bit = BitSet()
      for(i <- 0 until l.size) {
        if(p.distanceTo(l(i)) <= selectionDistance) {
          hasCloseSegment = true
          closeSegment = Some(l(i))
          bit = BitSet(i, if (i == 3) 0 else i + 1)
        }
      }
      (hasCloseSegment, closeSegment, bit)
    }
    //if the distance to the rectangle is more than the selection distance:
    if (geometry.distanceTo(p) > selectionDistance) {
      //If shape is not within selection distance of point, return Empty selector
      EmptyShapeSelector
      //if not either the whole rectangle, a segment, or a point should be selected:
    } else {
      //if the point is in range of one of the segments of the rectangle... :
      if(isCloseToSegment._1 == true){
        val segment = isCloseToSegment._2.get
        val segmentBitSet = isCloseToSegment._3
        //ok, the point is close to a segment. IF one of the endpoints are close, return its bit value:
        if(isCloseToPoint(segment, segmentBitSet)._1 == true) {
          BitSetShapeSelector(isCloseToPoint(segment, segmentBitSet)._2)
        }
        //if no point is close, return the bitset of the segment:
        else {
          println("return segment here!")
          BitSetShapeSelector(isCloseToSegment._3)
        }
        //if no point is close, return the segment:
      } else {
        EmptyShapeSelector
      }
    }
  }
  //needed for box selections?
  //TODO: is this right?
  def getSelector(r : SimpleRectangle2D) : ShapeSelector = {
    if (r.intersects(boundary)) {
      val cond1 = r.contains(p0)
      val cond2 = r.contains(p1)
      val cond3 = r.contains(p2)
      val cond4 = r.contains(p3)
      if (cond1 && cond2 && cond3 && cond4) {
        FullShapeSelector
      } else if (cond1) {
        ShapeSelector(0)
      } else if (cond2) {
        ShapeSelector(1)
      } else if (cond3) {
        ShapeSelector(2)
      } else if (cond4) {
        ShapeSelector(3)
      } else EmptyShapeSelector
    } else EmptyShapeSelector
  }

  //TODO: is this right? and when is a complex rectangle ever used to make a selection??
  def getSelector(r : ComplexRectangle2D) : ShapeSelector = {
    if (r.intersects(boundary)) {
      val cond1 = r.contains(p0)
      val cond2 = r.contains(p1)
      val cond3 = r.contains(p2)
      val cond4 = r.contains(p3)
      if (cond1 && cond2 && cond3 && cond4) {
        FullShapeSelector
      } else if (cond1) {
        ShapeSelector(0)
      } else if (cond2) {
        ShapeSelector(1)
      } else if (cond3) {
        ShapeSelector(2)
      } else if (cond4) {
        ShapeSelector(3)
      } else EmptyShapeSelector
    } else EmptyShapeSelector
  }

  //select all segments of the rectangle (shown as blue lines)
  //TODO: add part shapex
  def getShape(s : ShapeSelector) = s match {
    case FullShapeSelector => Some(this)
    case _ => None
  }

  //TODO: expand to allow for all combinations of selections of the four vertices.
  def getVertices(selector: ShapeSelector) = {

    selector match {
      case FullShapeSelector => geometry.vertices
      case ShapeSelector(0) => Seq(p0)
      case ShapeSelector(1) => Seq(p1)
      case ShapeSelector(2) => Seq(p2)
      case ShapeSelector(3) => Seq(p3)
      case _ => Seq()
    }
  }

  def setAttributes(attributes : Attributes) = RectangleShape(center, width,height,rotation, attributes)

  def transform(t : TransformationMatrix) =
    RectangleShape(center transform(t), width * t.scale, height * t.scale, rotation + t.rotation, attributes)
}

/**
 * Companion object to [[com.siigna.app.model.shape.RectangleShape]]
 */
object RectangleShape {

  def apply(center : Vector2D, width : Double, height : Double, rotation : Double) : RectangleShape =
    new RectangleShape(center, width, height, rotation,Attributes())

  def apply(p1 : Vector2D, p2 : Vector2D) : RectangleShape =
    new RectangleShape((p1+p2)/2, (p2-p1).x.abs, (p2-p1).y.abs, 0,Attributes())

  def apply(x1 : Double, y1 : Double, x2 : Double, y2 : Double) : RectangleShape =
    apply(Vector2D(x1,y1),Vector2D(x2,y2))
}