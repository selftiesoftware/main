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
import com.siigna.util.collection.Attributes
import com.siigna.app.Siigna
import com.siigna._
import app.model.shape.ArcShape.ArcShapeSelector
import scala.Some

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
 * TODO: Refactor so shape-parts include handles
 */
@SerialVersionUID(1561246469)
case class ArcShape(center : Vector2D, radius : Double, startAngle : Double, angle : Double, attributes : Attributes) extends BasicShape {

  type T = ArcShape

  val geometry = Arc2D(center, radius, startAngle, angle)

  def apply(part : ShapeSelector) = part match {
    case FullSelector => Some(new PartialShape(this, transform))
    case ArcShapeSelector(x : Byte) => {
      // Calculate the mathematical arc as a function to a given transformation matrix
      val arc = (t : TransformationMatrix) => x match {
        case 1 => Arc2D(geometry.startPoint.transform(t), geometry.midPoint, geometry.endPoint)
        case 2 => Arc2D(geometry.startPoint, geometry.midPoint.transform(t), geometry.endPoint)
        case 3 => Arc2D(geometry.startPoint.transform(t), geometry.midPoint.transform(t), geometry.endPoint)
        case 4 => Arc2D(geometry.startPoint, geometry.midPoint, geometry.endPoint.transform(t))
        case 5 => Arc2D(geometry.startPoint.transform(t), geometry.midPoint, geometry.endPoint.transform(t))
        case 6 => Arc2D(geometry.startPoint, geometry.midPoint.transform(t), geometry.endPoint.transform(t))
        case _ => geometry
      }
      // return the partial shape
      Some(new PartialShape(this, (t : TransformationMatrix) => ArcShape(arc(t))))
    }
    case _ => None
  }

  def delete(part: ShapeSelector) = part match {
    case ArcShapeSelector(_) | FullSelector => Nil
    case _ => Seq(this)
  }

  def getPart(rect: Rectangle2D) = if (rect.intersects(geometry)) FullSelector else EmptySelector

  def getPart(point: Vector2D) = if (distanceTo(point) < Siigna.double("selectionDistance").get) FullSelector else EmptySelector
  
  def getShape(s : ShapeSelector) = s match {
    case FullSelector => Some(this)
    case _ => None
  }

  def getVertices(selector: ShapeSelector) = selector match {
    case FullSelector        => geometry.vertices
    case ArcShapeSelector(1) => Seq(geometry.startPoint)
    case ArcShapeSelector(2) => Seq(geometry.midPoint)
    case ArcShapeSelector(3) => Seq(geometry.startPoint + geometry.midPoint)
    case ArcShapeSelector(4) => Seq(geometry.endPoint)
    case ArcShapeSelector(5) => Seq(geometry.startPoint + geometry.endPoint)
    case ArcShapeSelector(6) => Seq(geometry.midPoint + geometry.endPoint)
    case _ => Seq()
  }

  def setAttributes(attributes : Attributes) = new ArcShape(center, radius, startAngle, angle, attributes)

  def transform(t : TransformationMatrix) =
      ArcShape(t.transform(center),
               radius * t.scaleFactor,
               startAngle, angle,
               attributes)

}

/**
 * Companion object to ArcShape.
 */
object ArcShape
{

  /**
   * A [[com.siigna.app.model.shape.ShapeSelector]] for ArcShapes.
   * Arcs can be selected in the following way:
   * <pre>
   *  1         :  a single handle - the handle with the lowest degree (from 3 o'clock counter clockwise)
   *  2         :  a single handle - the second handle
   *  4         :  a single handle - the third (and last) handle (with the highest degree)
   *  1 + 2 = 3 :  both the first and second handle combined
   *  1 + 4 = 5 :  both the first and last handle
   *  2 + 4 = 6 :  both the second and last handle
   * @param byte  The handles to select
   */
  sealed case class ArcShapeSelector(byte : Byte) extends ShapeSelector

  /**
   * Creates an arc from three given points by calculating the center and setting the right radius and angles.
   * @param start  The vector where the arc starts (CCW).
   * @param middle  The middle vector of the arc.
   * @param end  The vector where the arc stops (CCW).
   * @return  An ArcShape with empty attributes
   */
  def apply(start : Vector2D, middle : Vector2D, end : Vector2D) : ArcShape = {
    val a = Arc2D(start, middle, end)
    new ArcShape(a.center, a.radius, a.startAngle, a.angle, Attributes())
  }

  /**
   * Creates an arc with the given center, radius and angles.
   * @param center  The center of the arc.
   * @param radius  The radius of the arc.
   * @param startAngle  The start angle in degrees given from 3 o'clock and CCW.
   * @param endAngle  The end angle in degrees CCW from 3 o'clock.
   * @return  An ArcShape with empty attributes
   */
  def apply(center : Vector2D, radius : Double, startAngle : Double, endAngle : Double) : ArcShape = {
    new ArcShape(center, radius, startAngle, endAngle, Attributes())
  }

  /**
   * Creates an arc from the given geometry.
   * @param geometry  The [[com.siigna.util.geom.Arc2D]] geometry containing the information to create the arc
   * @return  An ArcShape with empty attributes
   */
  def apply(geometry : Arc2D) = {
    new ArcShape(geometry.center, geometry.radius, geometry.startAngle, geometry.endAngle, Attributes())
  }

}