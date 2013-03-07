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
package com.siigna.app.model.action

import com.siigna.util.geom.TransformationMatrix
import com.siigna.app.model.shape.{PartialShape, ShapePart, Shape}
import com.siigna.app.model.{Drawing, Model}

/**
 * Transforms one or more shape by a given [[com.siigna.util.geom.TransformationMatrix]].
 */
object Transform {

  /**
   * Transform one shape with the given id with the given TransformationMatrix.
   * @param id  The id of the shape to transform
   * @param transformation  The transformation to apply
   */
  def apply(id : Int, transformation : TransformationMatrix) {
    Drawing execute TransformShape(id, transformation)
  }

  /**
   * Transforms several shapes with the given TransformationMatrix.
   * @param ids  The ids of the shapes
   * @param transformation The transformation to apply
   */
  def apply(ids : Traversable[Int], transformation : TransformationMatrix) {
    Drawing execute TransformShapes(ids, transformation)
  }

  /**
   * Transforms several shapes or shape-parts with the given TransformationMatrix. Due to erasure the method
   * accepts types of Int -> Any, but if anything else than [[com.siigna.app.model.shape.Shape]]s or
   * [[com.siigna.app.model.shape.ShapePart]]s are given, an exception will be thrown.
   * @param xs  The ids of the shapes or shape parts paired with a matrix
   * @param transformation  The matrix to apply on the shapes
   */
  def apply(xs : Map[Int, Any], transformation : TransformationMatrix) { if (xs.nonEmpty) {
    // Try to treat them as shapes
    try {
      val shapes = xs.asInstanceOf[Map[Int, Shape]]
      shapes.head._2.geometry // If this works we are certain to deal with shapes
      Drawing execute TransformShapes(shapes.asInstanceOf[Map[Int, Shape]].keys, transformation)
    } catch {
      case _ : Throwable => {
        Drawing execute TransformShapeParts(xs.asInstanceOf[Map[Int, ShapePart]], transformation)
      }
    }
  } }
}

/**
 * Transforms a shape with the given [[com.siigna.util.geom.TransformationMatrix]].
 * @param id The id of the shape
 * @param transformation  The TransformationMatrix containing the transformation
 */
case class TransformShape(id : Int, transformation : TransformationMatrix) extends Action {

  def execute(model : Model) = model add(id, model.shapes(id).transform(transformation))

  def ids = Traversable(id)

  // TODO: Implement and optimize
  // def merge(that : Action)

  def undo(model : Model) = model add (id, model.shapes(id).transform(transformation))
  
  def update(map : Map[Int, Int]) = copy(map.getOrElse(id, id))

}

/**
 * Transforms parts of several shapes with the same [[com.siigna.util.geom.TransformationMatrix]]
 * by mapping their id to a [[com.siigna.app.model.shape.ShapePart]].
 *
 * @param shapes  The id paired with a selector that selects the desired shape part.
 * @param transformation  The transformation with which all shape-parts should be applied.
 */
case class TransformShapeParts(shapes : Map[Int, ShapePart], transformation : TransformationMatrix) extends Action {

  def execute(model : Model) = {
    model add shapes.map(e => (e._1 -> model.shapes(e._1).apply(e._2))).collect{case (i : Int, Some(p : PartialShape)) => i -> p(transformation)}
  }

  def ids = shapes.keys
  
  // TODO: Implement and optimize
  //def merge(that : Action) = SequenceAction(this, that)

  def undo(model : Model) = {
    model add shapes.map(e => (e._1 -> model.shapes(e._1).apply(e._2))).collect{case (i : Int, Some(p : PartialShape)) => i -> p(transformation.inverse)}
  }
  
  def update(map : Map[Int, Int]) = copy(shapes.map(t => map.getOrElse(t._1, t._1) -> t._2))

}

/**
 * Transforms entire shapes with the given [[com.siigna.util.geom.TransformationMatrix]].
 *
 * @param ids  The ids of the shapes to transform.
 * @param transformation  The transformation matrix to apply.
 */
case class TransformShapes(ids : Traversable[Int], transformation : TransformationMatrix) extends Action {

  def execute(model : Model) = {
    val map : Map[Int, Shape] = ids.map(i => i -> model.shapes(i).transform(transformation)).toMap
    model add map
  }

  // TODO: Implement and optimize
  //def merge(that : Action) = SequenceAction(this, that)

  def undo(model : Model) = {
    val map : Map[Int, Shape] = ids.map(i => (i -> model.shapes(i).transform(transformation.inverse))).toMap
    model add map
  }

  def update(map : Map[Int, Int]) = copy(ids.map(t => map.getOrElse(t, t)))

}

