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

//import com.siigna.util.dxf.{DXFSection, DXFValue}
import com.siigna.util.geom.{Rectangle2D, TransformationMatrix, Vector2D, Segment2D}
import com.siigna.util.collection.{Preferences, Attributes}
import com.siigna.app.model.shape.LineShape.Selector

/**
 * This class draws a line segment.
 *
 * You can use the following attributes:
 * <pre>
 *  - Color        Color                 The color of the line.
 *  - Infinite     Boolean               If true, draws an endless line with the same equation as the given line.
 *  - StrokeWidth  Double                The width of the linestroke used to draw.
 *  - Transform    TransformationMatrix  A matrix to dynamically transform the shape when drawing.
 * </pre>
 * TODO: Update useable attributes.
 */
case class LineShape(p1 : Vector2D, p2 : Vector2D, attributes : Attributes) extends BasicShape {

  type T = LineShape

  val end = p2

  val geometry = new Segment2D(p1, p2)

  val points = Iterable(p1, p2)

  val start = p1

  def apply(part : ShapeSelector) = part match {
    case Selector(xs) => {
      Some(new PartialShape((t : TransformationMatrix) => LineShape(
        if(xs)  p1.transform(t) else p1,
        if(!xs) p2.transform(t) else p2,
        attributes)))
    }
    case FullSelector => Some(new PartialShape(transform))
    case _ => None
  }

  def delete(part : ShapeSelector) : Option[Shape] = part match {
    case Selector(_) | FullSelector => None
    case _ => Some(this)
  }

  def getPart(r : Rectangle2D) = {
    if (r.intersects(boundary)) {
      val cond1 = r.contains(p1)
      val cond2 = r.contains(p2)
      if (cond1 && cond2) {
        FullSelector
      } else if (cond1) {
        Selector(true)
      } else if (cond2) {
        Selector(false)
      } else EmptySelector
    } else EmptySelector
  }

  def getPart(p : Vector2D) = {
    val selectionDistance = Preferences.double("selectionDistance")
    if (distanceTo(p) > selectionDistance) {
      EmptySelector
    } else {
      if (p.distanceTo(p1) < p.distanceTo(p2) && p.distanceTo(p1) <= selectionDistance) {
        Selector(true)
      } else if (p.distanceTo(p2) < p.distanceTo(p1) && p.distanceTo(p2) <= selectionDistance) {
        Selector(false)
      } else {
        FullSelector
      }
    }
  }

  def getVertices(selector: ShapeSelector) = selector match {
    case FullSelector => geometry.vertices
    case Selector(x) => Seq(if(x) p1 else p2)
    case _ => Seq()
  }

  def setAttributes(attributes : Attributes) = LineShape(p1, p2, attributes)

  /*
  def toDXF = DXFSection(DXFValue(0, "LWPOLYLINE"),
                         DXFValue(5, scala.util.Random.nextString(4)),
                         DXFValue(100, "AcDbEntity"),
                         DXFValue(100, "AcDbPolyline"),
                         DXFValue(62, 1)) +
              DXFSection.fromVector(p1) +
              DXFSection.fromVector(p2)
  */

  override def toString() = "LineShape[" + p1 + ", " + p2 + "]"

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
   * The selector specific for LineShapes.
   * @param part  A boolean flag indicating if the first point is a part of the selection (true) or the second point (false).
   */
  sealed case class Selector(part : Boolean) extends ShapeSelector

  def apply(p1 : Vector2D, p2 : Vector2D) : LineShape =
    new LineShape(p1, p2, Attributes())

  def apply(x1 : Double, y1 : Double, x2 : Double, y2 : Double) : LineShape =
    apply(Vector2D(x1, y2), Vector2D(x2, y2))

  def apply(line : Segment2D) : LineShape =
    LineShape(line.p1, line.p2)

}
