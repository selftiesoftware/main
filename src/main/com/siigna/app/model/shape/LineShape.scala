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

import com.siigna.util.dxf.{DXFSection, DXFValue}
import com.siigna.util.geom.{Rectangle2D, TransformationMatrix, Vector2D, Segment2D}
import com.siigna.util.collection.{Preferences, Attributes}

/**
 * This class draws a line segment.
 *
 * You can use the following attributes:
 * <pre>
 *  - Color        Color                 The color of the line.
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
  
  def select() = DynamicShape(attributes.int("id").get, transform)

  def select(r : Rectangle2D) = if (r.contains(boundary)) Some(DynamicShape(attributes.int("id").get, transform)) else None
  
  def select(p : Vector2D) = {
    if (distanceTo(p) > Preferences.double("selection-distance")) {
      None
    } else {
      val f = (t : TransformationMatrix) => if (p.distanceTo(p1) < p.distanceTo(p2)) {
        LineShape(p1.transform(t), p2, attributes)
      } else {
        LineShape(p1, p2.transform(t), attributes)
      }
      Some(DynamicShape(attributes.int("id").get, f))
    }
  }

  val start = p1

  def setAttributes(attributes : Attributes) = LineShape(p1, p2, attributes)

  def toDXF = DXFSection(DXFValue(0, "LWPOLYLINE"),
                         DXFValue(5, scala.util.Random.nextString(4)),
                         DXFValue(100, "AcDbEntity"),
                         DXFValue(100, "AcDbPolyline"),
                         DXFValue(62, 1)) +
              DXFSection.fromVector(p1) +
              DXFSection.fromVector(p2)

  def transform(transformation : TransformationMatrix) : LineShape = {
    LineShape(p1 transform(transformation),
              p2 transform(transformation),
              attributes)
  }
}

object LineShape
{
  def apply(p1 : Vector2D, p2 : Vector2D) = new LineShape(p1, p2, Attributes())
  def apply(line : Segment2D) : LineShape = LineShape(line.p1, line.p2)

}
