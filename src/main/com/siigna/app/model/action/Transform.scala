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
package com.siigna.app.model.action

import com.siigna.util.geom.TransformationMatrix
import com.siigna.app.model.shape.{PartialShape, ShapeSelector, Shape}
import com.siigna.app.model.{Drawing, Model}
import serialization.TransformShapePartsProxy
import com.siigna.util.SerializableProxy

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
   * Transform one shape with the given id with the given function, returning a Shape.
   * @param id  The id of the shape
   * @param transformation  The transformation matrix applied on the shape
   * @param f  The function that returns the new shape, given a transformation matrix. 
   */
  def apply(id : Int, transformation : TransformationMatrix, f : TransformationMatrix => Shape) {
    Drawing execute TransformShape(id, transformation, Some(f))
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
   * Transforms several shapes with the given TransformationMatrix and the given function.
   * @param shapes  The ids of the shapes paired with the function to apply on each individual shape, given a matrix
   * @param transformation  The matrix to apply on the shapes
   */
  def apply[T](shapes : Map[Int, T], transformation : TransformationMatrix)(implicit m : Manifest[T]) {
    if (m <:< manifest[ShapeSelector]) { // Match shape selectors
      Drawing execute TransformShapeParts(shapes.asInstanceOf[Map[Int, ShapeSelector]], transformation)
    } else if (m <:< manifest[Shape]) { // Match shapes
      Drawing execute TransformShapes(shapes.asInstanceOf[Map[Int, Shape]].keys, transformation)
    } // If no match, do nothing
  }
}

/**
 * Transforms a shape with the given [[com.siigna.util.geom.TransformationMatrix]].
 * @param id The id of the shape
 * @param transformation  The TransformationMatrix containing the transformation
 * @param f  The function to apply on the shape
 */
@SerialVersionUID(292701996)
case class TransformShape(id : Int, transformation : TransformationMatrix, f : Option[TransformationMatrix => Shape] = None) extends Action {

  def execute(model : Model) = if (f.isDefined) {
    model add(id, f.get.apply(transformation))
  } else {
    model add(id, model.shapes(id).transform(transformation))
  }

  def ids = Traversable(id)

  // TODO: Implement and optimize
  // def merge(that : Action)

  def undo(model : Model) = if (f.isDefined) {
    model add (id, f.get.apply(transformation.inverse))
  } else {
    model add (id, model.shapes(id).transform(transformation))
  }
  
  def update(map : Map[Int, Int]) = copy(map.getOrElse(id, id))

}

/**
 * Transforms parts of several shapes with the same [[com.siigna.util.geom.TransformationMatrix]]
 * by mapping their id to a [[com.siigna.app.model.shape.ShapeSelector]].
 *
 * @param shapes  The id paired with a selector that selects the desired shape part.
 * @param transformation  The transformation with which all shape-parts should be applied.
 */
@SerialVersionUID(-1080215160)
case class TransformShapeParts(shapes : Map[Int, ShapeSelector], transformation : TransformationMatrix)
  extends SerializableProxy(() => new TransformShapePartsProxy(shapes, transformation)) with Action {

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
@SerialVersionUID(478072287)
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

