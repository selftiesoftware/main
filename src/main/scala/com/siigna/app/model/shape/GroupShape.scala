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

import com.siigna.util.collection.Attributes
import com.siigna.app.model.selection.{BitSetShapeSelector, FullShapeSelector, EmptyShapeSelector, ShapeSelector}
import scala.collection.immutable.BitSet

//import com.siigna.util.dxf.{DXFValue, DXFSection}
import com.siigna.util.geom.{SimpleRectangle2D, TransformationMatrix, Vector2D}

/**
 * A Group that contains <b>references</b> to other shape and is thus only used as a container.
 *
 * @param shapes  The shapes stored in the GroupShape.
 * @param attributes  Attributes to be applied on the shapes in the collection.
 *
 * TODO: Implement this.
 */
@SerialVersionUID(111678680)
case class GroupShape(shapes : Seq[Shape], attributes : Attributes) extends CollectionShape[Shape] {

  type T = GroupShape

  def delete(part : ShapeSelector) = {
    part match {
      case BitSetShapeSelector(xs) => {
        Seq(copy(shapes = for (i <- 0 until shapes.size; if (xs(i))) yield shapes(i)))
      }
      case EmptyShapeSelector => Seq(this)
      case _ => Nil
    }
  }

  def getPart(s : ShapeSelector) = s match {
    case FullShapeSelector => Some(new PartialShape(this, transform))
    case BitSetShapeSelector(xs) => {
      val active, inactive = Seq.newBuilder[Shape]
      for (i <- 0 until shapes.size) (if (xs(i)) active else inactive) += shapes(i)
      Some(new PartialShape(this, (t : TransformationMatrix) => {
        copy(shapes = inactive.result() ++ active.result().map(_.transform(t)))
      }))
    }
    case _ => None
  }

  def getSelector(rect: SimpleRectangle2D) = {
    var selector = BitSet()

    for (i <- 0 until shapes.size) {
      shapes(i).getSelector(rect) match {
        case EmptyShapeSelector =>
        case s : ShapeSelector => selector += i
      }
    }

    BitSetShapeSelector(selector)
  }

  def getSelector(point: Vector2D) = {
    shapes.reduceLeft((a : Shape, b : Shape) => if (a.distanceTo(point) <= b.distanceTo(point)) a else b).getSelector(point)
  }

  def getShape(s : ShapeSelector) = s match {
    case FullShapeSelector => Some(this)
    case BitSetShapeSelector(xs) => Some(copy(shapes = for (i <- 0 until shapes.size; if (xs(i))) yield shapes(i)))
    case _ => None
  }

  def getVertices(selector: ShapeSelector) = selector match {
    case BitSetShapeSelector(xs) => {
      (for (i <- 0 until shapes.size; if (xs(i))) yield shapes(i).geometry.vertices).flatten
    }
    case FullShapeSelector => shapes.flatMap(_.geometry.vertices)
    case _ => Nil
  }

  def join(shape : Shape) = copy(shapes = shapes :+ shape)

  def join(shapes: Traversable[Shape]) = copy(shapes = this.shapes ++ shapes)

  /**
   * Returns a new collection with a new set of attributes. In other words return a collection with a new id,
   * but otherwise the same attributes.
   */
  def setAttributes(attributes : Attributes) : GroupShape = copy(attributes = attributes)

  /**
   * Applies a transformation to the shape.
   */
  def transform(transformation : TransformationMatrix) = copy(shapes = shapes.map(_.transform(transformation)))
}

/**
 * Object used for quick access to and constructor overloading for the <code>GroupShape</code> class.
 */
object GroupShape {

  def apply(shapes : Traversable[Shape]) = new GroupShape(shapes.toSeq, Attributes())

}