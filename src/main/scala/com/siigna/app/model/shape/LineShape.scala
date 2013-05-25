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

import com.siigna.util.geom.{SimpleRectangle2D, TransformationMatrix, Segment2D, Vector2D}
import com.siigna.util.collection.Attributes
import com.siigna.app.Siigna
import com.siigna.app.model.selection._

/**
 * This class draws a line segment.
 *
 * You can use the following attributes:
 * <pre>
 *  - Color        Color                 The color of the line.
 *  - Infinite     Boolean               If true, draws an endless line with the same equation as the given line.
 *  - StrokeWidth  Double                The width of the linestroke used to draw.
 *  - Transform    TransformationMatrix  A matrix to dynamically transform the shape when server.
 * </pre>
 * TODO: Update useable attributes.
 */
@SerialVersionUID(-477646412)
case class LineShape(p1 : Vector2D, p2 : Vector2D, attributes : Attributes) extends BasicShape {

  type T = LineShape

  val end = p2

  val geometry = new Segment2D(p1, p2)

  val points = Iterable(p1, p2)

  val start = p1

  def delete(part : ShapeSelector) = part match {
    case BitSetShapeSelector(_) | FullShapeSelector => Nil
    case _ => Seq(this)
  }

  def getPart(part : ShapeSelector) = part match {
    case ShapeSelector(0) => Some(new PartialShape(this, (t : TransformationMatrix) => LineShape(p1.transform(t), p2, attributes)))
    case ShapeSelector(1) => Some(new PartialShape(this, (t : TransformationMatrix) => LineShape(p1, p2.transform(t), attributes)))
    case FullShapeSelector => Some(new PartialShape(this, transform))
    case _ => None
  }

  def getSelector(r : SimpleRectangle2D) = {
    if (r.intersects(boundary)) {
      val cond1 = r.contains(p1)
      val cond2 = r.contains(p2)
      if (cond1 && cond2) {
        FullShapeSelector
      } else if (cond1) {
        ShapeSelector(0)
      } else if (cond2) {
        ShapeSelector(1)
      } else EmptyShapeSelector
    } else EmptyShapeSelector
  }

  def getSelector(p : Vector2D) = {
    val selectionDistance = Siigna.selectionDistance
    if (distanceTo(p) > selectionDistance) {
      //If shape is not within selection distance of point, return Empty selector
      EmptyShapeSelector
    } else {
      //If both points are within selection distance, select the whole shape:
      //TODO: In much later version: Make it possible to choose which point to select.
      if (p1.distanceTo(p) <= selectionDistance && p2.distanceTo(p) <= selectionDistance) {
        FullShapeSelector
      } else if (p1.distanceTo(p) < p2.distanceTo(p) && p1.distanceTo(p) <= selectionDistance) {
      //If lineshape's point one is closer to selection point than point two, and within selection distance,
      //Return true - if point two is closest to selection point, and within selection distance, return false
        ShapeSelector(0)
      } else if (p2.distanceTo(p) < p1.distanceTo(p) && p2.distanceTo(p) <= selectionDistance) {

        ShapeSelector(1)
      } else {
        //If shape is within selection distance of selection point, but none of the line's endpoints are,
        //The line should be selected - that means, both the points.
        FullShapeSelector

      }
    }
  }
  
  def getShape(s : ShapeSelector) = s match {
    case FullShapeSelector => Some(this)
    case _ => None
  }

  def getVertices(selector: ShapeSelector) = selector match {
    case FullShapeSelector => geometry.vertices
    case ShapeSelector(0) => Seq(p1)
    case ShapeSelector(1) => Seq(p2)
    case _ => Seq()
  }

  def setAttributes(attributes : Attributes) = LineShape(p1, p2, attributes)

  override def toString = "LineShape[" + p1 + ", " + p2 + " (" + attributes + ")]"

  def transform(transformation : TransformationMatrix) : LineShape = {
    LineShape(p1 transform(transformation),
              p2 transform(transformation),
              attributes)
  }
  
}

/**
 * Companion object for the LineShape. Contains shortcuts for instantiating LineShapes.
 */
object LineShape {

  /**
   * Creates a line shape defined by the two points.
   * @param p1  One of the end-points of the line.
   * @param p2  The other end-point of the line.
   * @return  A LineShape with no attributes
   */
  def apply(p1 : Vector2D, p2 : Vector2D) : LineShape =
    new LineShape(p1, p2, Attributes())

  /**
   * Creates a LineShape with the given four coordinate values in pairs: x1, y1 and x2, y2.
   * @param x1  The x-value of the first end-point of the Line
   * @param y1  The y-value of the first end-point of the Line
   * @param x2  The x-value of the second end-point of the Line
   * @param y2  The y-value of the second end-point of the Line
   * @return  A LineShape with no attributes
   */
  def apply(x1 : Double, y1 : Double, x2 : Double, y2 : Double) : LineShape =
    apply(Vector2D(x1, y1), Vector2D(x2, y2))

  /**
   * Creates a LineShape with the given four coordinate values in pairs (x1, y1 and x2, y2) and the given set
   * of attributes.
   * @param x1  The x-value of the first end-point of the Line
   * @param y1  The y-value of the first end-point of the Line
   * @param x2  The x-value of the second end-point of the Line
   * @param y2  The y-value of the second end-point of the Line
   * @param attributes  The attributes of the LineShape.
   * @return  A LineShape with the given attributes.
   */
  def apply(x1 : Double, y1 : Double, x2 : Double, y2 : Double, attributes : Attributes) : LineShape =
    apply(Vector2D(x1, y1), Vector2D(x2, y2), attributes)

  /**
   * Creates a LineShape from its geometrical archetype - the [[com.siigna.util.geom.Segment2D]].
   * @param segment  The segment that describes the LineShape.
   * @return  A LineShape with no attributes.
   */
  def apply(segment : Segment2D) : LineShape = LineShape(segment.p1, segment.p2)

}
