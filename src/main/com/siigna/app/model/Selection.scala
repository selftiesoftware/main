package com.siigna.app.model

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

import com.siigna.util.collection.Attributes
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.app.model.Model
import com.siigna.app.model.action.{TransformShapes, Action}
import com.siigna.app.view.View
import shape.{ShapeLike, Shape}

/**
 * A dynamic shape is a mutable wrapper for a regular Shape(s).
 * When altered, the dynamic shape saves the action required to alter the shape(s) in the static layer, so the changes
 * can be made to the static version later on - when the shape(s) are "demoted" back into the static layer.
 *
 * @param shapes  The ids of the wrapped shape(s).
 * @see [[com.siigna.app.model.MutableModel]]
 */
case class Selection(shapes: Map[Int, TransformationMatrix => Shape]) extends ShapeLike with (TransformationMatrix => Traversable[Shape]) {

  type T = Selection

  /**
   * The underlying action with which this Selection has been changed since creation, if any.
   */
  var action: Option[Action] = None

  /**
   * Stores a private transformation matrix that indicates the translation applied to the
   * Dynamic ShapeLike since creation.
   */
  private var transformation: TransformationMatrix = TransformationMatrix()

  /**
   * Applies a given transformation on the Selection
   * @param t  The transformation to apply.
   * @return An Shape as the result of the applied transformation.
   */
  def apply(t: TransformationMatrix) = shapes.values.map(_(transformation) transform t)

  /**
   * The attributes of the underlying ImmutableShapes.
   */
  def attributes = Attributes()

  /**
   * The boundary of the underlying ImmutableShapes.
   * @return A Rectangle2D.
   */
  def boundary = shapes.map(s => Model(s._1)).foldLeft(Model(shapes.head._1).boundary)((a, b) => a.expand(b.boundary))

  /**
   * Calculates the distance from the vector and to the underlying Shape.
   * @param point  The point to calculate the distance to.
   * @param scale  The scale in which we are calculating.
   * @return  The length from the closest point of this shape to the point.
   */
  def distanceTo(point: Vector2D, scale: Double) = shapes.map(s => Model(s._1).distanceTo(point)).reduceLeft((a, b) => if (a < b) a else b) * scale

  /**
   * Returns the current transformation applied to the shape.
   * @return  The transformation as a [[com.siigna.util.geom.TransformationMatrix]].
   */
  def getTransformation = transformation

  def setAttributes(attributes: Attributes) = this // TODO: Create some kind of (set/create/update)attribute action

  /**
   * Transforms the underlying Shape by adding a TransformShape action to the list of actions
   * applied to this Selection
   * @param transformation  The TransformationMatrix to apply to the shape.
   */
  def transform(transformation: TransformationMatrix) = {
    // Store the transformation
    this.transformation = transformation
    // Return this
    this
  }
}

/**
 * Companion-object for the Selection class.
 */
object Selection {

  /**
   * A method to create a Selection with only one id.
   */
  def apply(id: Int, f: TransformationMatrix => Shape) = {
    new Selection(Map(id -> f))
  }

}