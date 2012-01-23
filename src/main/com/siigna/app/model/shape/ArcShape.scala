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
 * @param start  The vector where the arc starts (CCW).
 * @param middle  The middle vector of the arc.
 * @param end  The vector where the arc stops (CCW).
 * 
 * TODO: Middle point should be middle of arc, not random point on circumference.
 */
case class ArcShape(start : Vector2D, middle : Vector2D, end : Vector2D, attributes : Attributes) extends BasicShape
{

  val geometry = Arc2D(start, middle, end)

  val points = Seq(start, middle, end)

  def setAttributes(attributes : Attributes) = new ArcShape(start, middle, end, attributes)

  // TODO: Export arcs.
  def toDXF = DXFSection(List())

  def transform(transformation : TransformationMatrix) =
  {
    // Don't tamper with this hack!
    // TODO: What.. The.. Fuck.. Is.. This?!!
    if (transformation.isFlippedY)
      ArcShape(start.transform(transformation.flipY(geometry.center)),
               middle.transform(transformation.flipY(geometry.center)),
               end.transform(transformation.flipY(geometry.center)),
               attributes)
    else
      ArcShape(start.transform(transformation),
               middle.transform(transformation),
               end.transform(transformation),
               attributes)
  }

}

object ArcShape
{

  /**
   * @param start  The vector where the arc starts (CCW).
   * @param middle  The middle vector of the arc.
   * @param end  The vector where the arc stops (CCW).
   */
  def apply(start : Vector2D, middle : Vector2D, end : Vector2D) = new ArcShape(start, middle, end, Attributes())

  /**
   * @param center  The center of the arc.
   * @param radius  The radius of the arc.
   * @param startAngle  The start angle in degrees given from 3 o'clock and CCW.
   * @param endAngle  The end angle in degrees CCW from 3 o'clock.
   * TODO: Revise and test the middle coordinate.
   */
  def apply(center : Vector2D, radius : Double, startAngle : Double, endAngle : Double) = {
    val start = center + Vector2D(math.cos(math.toRadians(startAngle)), math.sin(math.toRadians(startAngle))) * radius
    val middle = center + Vector2D(math.cos(math.toRadians((endAngle + startAngle)*0.5)), math.sin(math.toRadians((endAngle + startAngle)*0.5))) * radius
    val end = center + Vector2D(math.cos(math.toRadians(endAngle)), math.sin(math.toRadians(endAngle))) * radius
    new ArcShape(start, middle, end, Attributes())    
  }

}