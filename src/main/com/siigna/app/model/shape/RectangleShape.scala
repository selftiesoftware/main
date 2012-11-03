package com.siigna.app.model.shape

import com.siigna.util.geom.{Rectangle2D, TransformationMatrix, Vector2D}
import com.siigna.util.collection.Attributes
import com.siigna.app.model.shape.RectangleShape.Selector

/**
 * A rectangle that can be rotated.
 */
case class RectangleShape(center : Vector2D, width : Double, height : Double, rotation : Double, attributes : Attributes) extends Shape {

  type T = RectangleShape

  def apply(part : ShapeSelector) = None
  /*part match {
    case Selector(b) => {
      Some(new PartialShape(this, (t : TransformationMatrix) => RectangleShape(

      )))
    }
    case FullSelector => Some(new PartialShape(this, transform))
    case _ => None
  }*/

  def delete(part : ShapeSelector) = Nil

  val geometry = Rectangle2D(center.x - width / 2, center.y - height / 2, center.x + width / 2, center.y + height / 2)

  def getPart(rect : Rectangle2D) = Selector(0)
  def getPart(point : Vector2D) = Selector(0)
  def getShape(selector : ShapeSelector) = None
  def getVertices(selector : ShapeSelector) = Nil

  def setAttributes(attributes : Attributes) = copy(attributes = attributes)

  def transform(matrix : TransformationMatrix) = {
    new RectangleShape(
    center.transform(matrix),
      width * matrix.scaleFactor,
      height * matrix.scaleFactor,
      rotation + matrix.rotation,
      attributes
    )
  }

}

/**
 * Companion object to RectangleShape.
 */
object RectangleShape {

  case class Selector(b : Byte) extends ShapeSelector

  /**
   * Creates a Rectangle from two points.
   * @param p1  The upper left point
   * @param p2  The lower right point
   * @return  A [[com.siigna.app.model.shape.RectangleShape]].
   */
  def apply(p1 : Vector2D, p2 : Vector2D) =
    new RectangleShape((p1 + p2) / 2, math.abs(p2.x - p1.x), math.abs(p2.y - p1.y), 0, Attributes())

  /**
   * Creates a Rectangle from two coordinates (upper left and lover right) given as x and y pairs.
   * @param x1  Smallest x-coordinate.
   * @param y1  Largest y-coordinate.
   * @param x2  Largest x-coordinate.
   * @param y2  Smallest y-coordinate.
   */
  def apply(x1 : Double, y1 : Double, x2 : Double, y2 : Double) =
    new RectangleShape(Vector2D((x1 + x2) / 2, (y1 + y2) / 2), math.abs(x2 - x1), math.abs(y2 - y1), 0, Attributes())

}
