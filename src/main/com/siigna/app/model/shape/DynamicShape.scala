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

import com.siigna.util.collection.Attributes
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.app.model.Model
import com.siigna.app.model.action.{TransformShape, Action}

/**
 * A dynamic shape is a mutable wrapper for a regular Shape.
 * When altered, the dynamic shape saves the action required to alter the shape in the static layer, so the changes
 * can be made to the static version later on - when the shape is "demoted" back into the static layer.
 *
 * @param id  The id of the wrapped shape.
 * @param f  The predicate that transforms the selected shape - or parts of it - into a new immutable shape
 * @see [[com.siigna.app.model.DynamicModel]]
 */
case class DynamicShape(id : Int, f : TransformationMatrix => ImmutableShape) extends Shape
                                            with (TransformationMatrix => ImmutableShape) {

  type T = DynamicShape

  /**
   * The underlying action with which this DynamicShape has been changed since creation, if any. 
   */
  var action : Option[Action] = None

  /**
   * Applies a given transformation on the DynamicShape
   * @param t  The transformation to apply.
   * @return An ImmutableShape as the result of the applied transformation.
   */
  def apply(t : TransformationMatrix) = f(t)
  
  /**
   * The attributes of the underlying ImmutableShape.
   */
  def attributes = Model(id).attributes

  /**
   * The boundary of the underlying ImmutableShape.
   * @return A Rectangle2D.
   */
  def boundary = Model(id).boundary

  /**
   * Calculates the distance from the vector and to the underlying ImmutableShape.
   * @param point  The point to calculate the distance to.
   * @param scale  The scale in which we are calculating.
   * @return  The length from the closest point of this shape to the point.
   */
  def distanceTo(point: Vector2D, scale: Double) = Model(id).distanceTo(point)

  def setAttributes(attributes: Attributes) = this // TODO: Create some kind of (set/create/update)attribute action

  /**
   * Transforms the underlying ImmutableShape by adding a TransformShape action to the list of actions
   * applied to this DynamicShape
   * @param transformation  The TransformmationMatrix to apply to the shape.
   */
  def transform(transformation: TransformationMatrix) = {
    action = if (action.isDefined) {
      Some(action.get.merge(TransformShape(id, transformation)))
    } else {
      Some(TransformShape(id, transformation))
    }
    this
  }
}