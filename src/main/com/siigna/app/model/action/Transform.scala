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

import com.siigna.app.model.Model
import com.siigna.util.geom.TransformationMatrix
import com.siigna.app.model.shape.{PartialShape, ShapeSelector, Shape}

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
    Model execute TransformShape(id, transformation)
  }

  /**
   * Transform one shape with the given id with the given function, returning a Shape.
   * @param id  The id of the shape
   * @param transformation  The transformation matrix applied on the shape
   * @param f  The function that returns the new shape, given a transformation matrix. 
   */
  def apply(id : Int, transformation : TransformationMatrix, f : TransformationMatrix => Shape) {
    Model execute TransformShape(id, transformation, Some(f))
  }

  /**
   * Transforms several shapes with the given TransformationMatrix.
   * @param ids  The ids of the shapes
   * @param transformation The transformation to apply
   */
  def apply(ids : Traversable[Int], transformation : TransformationMatrix) {
    val m = ids.map(id => (id -> Model(id))).toMap
    Model execute TransformShapes(m, transformation)
  }

  /**
   * Transforms several shapes with the given TransformationMatrix and the given function.
   * @param shapes  The ids of the shapes paired with the function to apply on each individual shape, given a matrix
   * @param transformation  The matrix to apply on the shapes
   */
  def apply[T : Manifest](shapes : Map[Int, Shape], transformation : TransformationMatrix) {
    Model execute TransformShapes(shapes, transformation)
  }
}

/**
 * Transforms a shape with the given [[com.siigna.util.geom.TransformationMatrix]].
 * @param id The id of the shape
 * @param transformation  The TransformationMatrix containing the transformation
 * @param f  The function to apply on the shape
 */
case class TransformShape(id : Int, transformation : TransformationMatrix, f : Option[TransformationMatrix => Shape] = None) extends Action {

  def execute(model : Model) = if (f.isDefined) {
    model add(id, f.get.apply(transformation))
  } else {
    model add(id, model.shapes(id).transform(transformation))
  }
  
  // TODO: Optimize
  def merge(action : Action) = SequenceAction(this, action)

  def undo(model : Model) = if (f.isDefined) {
    model add (id, f.get.apply(transformation.inverse))
  } else {
    model add (id, model.shapes(id).transform(transformation))
  }

}

case class TransformShapeParts(shapes : Map[Int, ShapeSelector], transformation : TransformationMatrix) extends Action {

  def execute(model : Model) = {
    model add shapes.map(e => (e._1 -> Model(e._1).apply(e._2))).collect{case (i : Int, Some(p : PartialShape)) => i -> p(transformation)}
  }

  def merge(that : Action) = SequenceAction(this, that)

  def undo(model : Model) = {
    model add shapes.map(e => (e._1 -> Model(e._1).apply(e._2))).collect{case (i : Int, Some(p : PartialShape)) => i -> p(transformation.inverse)}
  }
  
}

/**
 * Transforms a number of shapes with the given [[com.siigna.util.geom.TransformationMatrix]].
 */
case class TransformShapes(shapes : Map[Int, Shape], transformation : TransformationMatrix) extends Action {

  def execute(model : Model) = {
    val map : Map[Int, Shape] = shapes.map(s => (s._1 -> s._2.transform(transformation))).toMap
    model add map
  }

  // TODO: Optimize
  def merge(that : Action) = SequenceAction(this, that)

  def undo(model : Model) = {
    val map : Map[Int, Shape] = shapes.map(s => (s._1 -> s._2.transform(transformation.inverse))).toMap
    model add map
  }
}

