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
import com.siigna.util.geom.{Arc2D, TransformationMatrix, Vector2D}
import com.siigna.util.collection.Attributes

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
 * TODO: Middle point should be middle of arc, not random point on circumference.
 */
case class ArcShape(center : Vector2D, radius : Double, startAngle : Double, angle : Double, attributes : Attributes) extends BasicShape
{

  val geometry = Arc2D(center, radius, startAngle, angle)

  def setAttributes(attributes : Attributes) = new ArcShape(center, radius, startAngle, angle, attributes)

  // TODO: Export arcs.
  def toDXF = DXFSection(List())

  def transform(t : TransformationMatrix) =
      ArcShape(t.transform(center),
               radius * t.scaleFactor,
               startAngle, angle,
               attributes)

}

object ArcShape
{

  /**
   * @param start  The vector where the arc starts (CCW).
   * @param middle  The middle vector of the arc.
   * @param end  The vector where the arc stops (CCW).
   * TODO: Deprecate?
   */
  def apply(start : Vector2D, middle : Vector2D, end : Vector2D) : ArcShape = {
    val a = Arc2D(start, middle, end)
    apply(a.center, a.radius, a.startAngle, a.angle)
  }

  /**
   * @param center  The center of the arc.
   * @param radius  The radius of the arc.
   * @param startAngle  The start angle in degrees given from 3 o'clock and CCW.
   * @param endAngle  The end angle in degrees CCW from 3 o'clock.
   * TODO: Revise and test the middle coordinate.
   */
  def apply(center : Vector2D, radius : Double, startAngle : Double, endAngle : Double) : ArcShape = {
    new ArcShape(center, radius, startAngle, endAngle, Attributes())
  }

}