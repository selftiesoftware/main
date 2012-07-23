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

import com.siigna.app.model.Model
import com.siigna.util.collection.Attributes
import collection.immutable.BitSet

//import com.siigna.util.dxf.{DXFValue, DXFSection}
import com.siigna.util.geom.{Rectangle2D, TransformationMatrix, Vector2D}

/**
 * A Group that contains <b>references</b> to other shape and is thus only used as a container.
 *
 * @param shapes  The shapes stored in the GroupShape.
 * @param attributes  Attributes to be applied on the shapes in the collection.
 *
 * TODO: Implement this.
 */
case class GroupShape(shapes : Seq[Shape], attributes : Attributes) extends CollectionShape[Shape] {

  throw new UnsupportedOperationException("Not implemented yet")

  type T = GroupShape

  def apply(part : ShapeSelector) = None

  def delete(part : ShapeSelector) = {
    part match {
      case GroupShape.Selector(parts) => {
        Seq(copy(shapes = parts.foldLeft(shapes)((shapes, part) => shapes.updated(part._1))
      }
      case _ => Nil
    }
  }

  def getPart(rect: Rectangle2D) = {
    var parts = Map[Int, ShapeSelector]()

    for (i <- 0 until shapes.size) {
      shapes(i).getPart(rect) match {
        case EmptySelector =>
        case s : ShapeSelector => parts = parts + (i -> s)
      }
    }

    GroupShape.Selector(parts)
  }

  def getPart(point: Vector2D) = {
    shapes.reduceLeft((a : Shape, b : Shape) => if (a.distanceTo(point) <= b.distanceTo(point)) a else b).getPart(point)
  }

  def getVertices(selector: ShapeSelector) = shapes.flatMap(_.getVertices(selector))

  def join(shape: Shape) = throw new UnsupportedOperationException("Not yet implemented")

  def join(shapes: Traversable[Shape]) = throw new UnsupportedOperationException("Not yet implemented")

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

  sealed case class Selector(selectors : Map[Int, ShapeSelector]) extends ShapeSelector

  def apply(shapes : Traversable[Shape]) = new GroupShape(shapes.toSeq, Attributes())

}