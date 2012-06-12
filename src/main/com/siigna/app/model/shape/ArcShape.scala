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

//import com.siigna.util.dxf.DXFSection
import com.siigna.util.geom.{Rectangle2D, Arc2D, TransformationMatrix, Vector2D}
import com.siigna.util.collection.{Attributes}
import com.siigna.app.Siigna

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
 * TODO: Refactor so shape-parts include handles
 */
case class ArcShape(center : Vector2D, radius : Double, startAngle : Double, angle : Double, attributes : Attributes) extends BasicShape {

  type T = ArcShape

  val geometry = Arc2D(center, radius, startAngle, angle)

  // TODO: What about selection "arc-points"??
  def apply(part : ShapeSelector) = part match {
    case FullSelector => Some(new PartialShape(transform))
    //case PartialSelector(1) => Some(new PartialShape((t : TransformationMatrix) => ArcShape(t.transform(center), radius * t.scaleFactor, startAngle, angle, attributes)))
    case _ => None
  }

  def delete(part: ShapeSelector) = part match {
    //case PartialSelector(_) | FullSelector => None
    case _ => Seq(this)
  }

  def getPart(rect: Rectangle2D) = if (rect.intersects(geometry)) FullSelector else EmptySelector

  def getPart(point: Vector2D) = if (distanceTo(point) < Siigna.double("selectionDistance").getOrElse(5.0)) FullSelector else EmptySelector

  def getVertices(selector: ShapeSelector) = selector match {
    case FullSelector => geometry.vertices
    //case PartialSelector(1) => Seq(center)
    case _ => Seq()
  }

  def setAttributes(attributes : Attributes) = new ArcShape(center, radius, startAngle, angle, attributes)

  // TODO: Export arcs.
  //def toDXF = DXFSection(List())

  def transform(t : TransformationMatrix) =
      ArcShape(t.transform(center),
               radius * t.scaleFactor,
               startAngle, angle,
               attributes)
  
  //protected sealed case class ArcShapeSelector()

}

object ArcShape
{

  /**
   * @param start  The vector where the arc starts (CCW).
   * @param middle  The middle vector of the arc.
   * @param end  The vector where the arc stops (CCW).
   */
  def apply(start : Vector2D, middle : Vector2D, end : Vector2D) : ArcShape = {
    val a = Arc2D(start, middle, end)
    new ArcShape(a.center, a.radius, a.startAngle, a.angle, Attributes())
  }

  /**
   * @param center  The center of the arc.
   * @param radius  The radius of the arc.
   * @param startAngle  The start angle in degrees given from 3 o'clock and CCW.
   * @param endAngle  The end angle in degrees CCW from 3 o'clock.
   */
  def apply(center : Vector2D, radius : Double, startAngle : Double, endAngle : Double) : ArcShape = {
    new ArcShape(center, radius, startAngle, endAngle, Attributes())
  }

}