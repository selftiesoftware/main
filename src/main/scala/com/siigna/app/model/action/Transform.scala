/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */
package com.siigna.app.model.action

import com.siigna.util.geom.TransformationMatrix
import com.siigna.app.model.shape.{PartialShape, Shape}
import com.siigna.app.model.{Drawing, Model}
import com.siigna.app.model.selection.ShapeSelector

/**
 * <p>
 *   Transforms one or more [[com.siigna.app.model.shape.Shape]]s or [[com.siigna.app.model.selection.ShapeSelector]]s
 *   by a given [[com.siigna.util.geom.TransformationMatrix]].
 * </p>
 * <p>
 *   The Transform object basically just informs the Drawing to execute an [[com.siigna.app.model.action.Action]]
 *   that fits the type of the input data. But it exists to simplify the entry-point to shapes, their ids and
 *   the undo/execute mechanisms of the [[com.siigna.app.model.Drawing]].
 * </p>
 *
 * <h2>Examples</h2>
 * {{{
 *   // Get some sample shapes - in this case shapes close to (0, 0)
 *   val shapes = Drawing( Vector2D(0, 0) )
 *
*    // Create a TranformationMatrix that rotates the shapes 20 degrees counter-clockwise from (0, 0)
 *   val transformation = TransformationMatrix().rotate(20)
 *
 *   // Apply the transformation to the Drawing using the Transform object
 *   Transform(shapes, transformation)
 * }}}
 * <p>
 *   Besides extracting shapes (and ids) from the Drawing it is also possible to transform the current selection.
 * </p>
 * {{{
 *   // Retrieve the current selection
 *   val selection = Drawing.selection
 *
 *   // Create a TransformationMatrix that scales the shapes by a factor of 2
 *   val transformation = TransformationMatrix().scale(2)
 *
 *   // Apply the transformation to the current selection
 *   Transform(selection, transformation)
 * }}}
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
   * [[com.siigna.app.model.selection.ShapeSelector]]s are given, an exception will be thrown.
   * @param xs  The ids of the shapes or shape parts paired with a matrix
   * @param transformation  The matrix to apply on the shapes
   */
  def apply(xs : Map[Int, Any], transformation : TransformationMatrix) { if (xs.nonEmpty) {
    // Try to treat them as shapes
    try {
      val shapes = xs.asInstanceOf[Map[Int, Shape]]
      shapes.head._2.geometry // If this works we are certain to deal with shapes
      Drawing execute TransformShapes(shapes.keys, transformation)
    } catch {
      case _ : Throwable => {
        Drawing execute TransformShapeParts(xs.asInstanceOf[Map[Int, ShapeSelector]], transformation)
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
 * by mapping their id to a [[com.siigna.app.model.selection.ShapeSelector]].
 *
 * @param shapes  The id paired with a selector that selects the desired shape part.
 * @param transformation  The transformation with which all shape-parts should be applied.
 */
case class TransformShapeParts(shapes : Map[Int, ShapeSelector], transformation : TransformationMatrix) extends Action {

  def execute(model : Model) = {
    model add shapes.map(e => (e._1 -> model.shapes(e._1).getPart(e._2))).collect{case (i : Int, Some(p : PartialShape)) => i -> p(transformation)}
  }

  def ids = shapes.keys
  
  // TODO: Implement and optimize
  //def merge(that : Action) = SequenceAction(this, that)

  def undo(model : Model) = {
    model add shapes.map(e => (e._1 -> model.shapes(e._1).getPart(e._2))).collect{case (i : Int, Some(p : PartialShape)) => i -> p(transformation.inverse)}
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

