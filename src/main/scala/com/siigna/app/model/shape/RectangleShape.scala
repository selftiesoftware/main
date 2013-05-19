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

case class RectangleShape(center : Vector2D, width : Double, height : Double, rotation : Double, val attributes : Attributes) extends ClosedShape {

  type T = RectangleShape

  val geometry = ComplexRectangle2D(center,width,height,rotation)

  def delete(selector: _root_.com.siigna.app.model.selection.ShapeSelector): scala.Seq[_root_.com.siigna.app.model.shape.Shape] = throw new UnsupportedOperationException("Not yet implemented")

  def getPart(selector: ShapeSelector): Option[PartialShape] = throw new UnsupportedOperationException("Not yet implemented")

  //def getSelector(point: Vector2D): ShapeSelector = throw new UnsupportedOperationException("Not yet implemented")
  def getSelector(p : Vector2D) = {
    val selectionDistance = Siigna.selectionDistance
    val p1 = geometry.bottomLeft //BitSet 3
    val p2 = geometry.topRight  //BitSet 2
    val p3 = geometry.bottomRight //BitSet 1
    val p4 = geometry.topLeft //BitSet 0


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
      val l = List(Segment2D(p1,p2),Segment2D(p2,p3),Segment2D(p3,p4), Segment2D(p4,p1))
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
          println("point bit: "+BitSetShapeSelector(isCloseToPoint(segment, segmentBitSet)._2))
          BitSetShapeSelector(isCloseToPoint(segment, segmentBitSet)._2)
        }

        //if no point is close, return the bitset of the segment:
        else BitSetShapeSelector(isCloseToSegment._3)
      //if no point is close, return the segment:
      } else {
        //If shape is within selection distance of selection point, but none of the line's endpoints are,
        //The segment closest to the point should be selected
        //BitSetShapeSelector(isCloseToSegment._3.get)
        EmptyShapeSelector
      }
    }
  }

  def getSelector(rect: SimpleRectangle2D): ShapeSelector = throw new UnsupportedOperationException("Not yet implemented")
  def getSelector(r : ComplexRectangle2D) = {
    if (r.intersects(boundary)) {
      val p1 = r.bottomLeft
      val p2 = r.bottomRight
      val p3 = r.topLeft
      val p4 = r.topRight
      val cond1 = r.contains(p1)
      val cond2 = r.contains(p2)
      val cond3 = r.contains(p3)
      val cond4 = r.contains(p4)
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

  //def getShape(selector: ShapeSelector): Option[Shape] =
  //throw new UnsupportedOperationException("Not yet implemented")
  def getShape(s : ShapeSelector) = s match {
    case FullShapeSelector => Some(this)
    case _ => None
  }



  def getVertices(selector: ShapeSelector): Seq[Vector2D] = throw new UnsupportedOperationException("Not yet implemented")

  def setAttributes(attributes: Attributes): RectangleShape#T = throw new UnsupportedOperationException("Not yet implemented")

  def transform(t : TransformationMatrix) =
    RectangleShape(center transform(t), width * t.scaleFactor, height * t.scaleFactor, rotation, attributes)

}
