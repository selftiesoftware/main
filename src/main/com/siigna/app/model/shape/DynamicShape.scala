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
import com.siigna.app.model.action.{TransformShapes, TransformShape, Action}

/**
 * A dynamic shape is a mutable wrapper for a regular ImmutableShape(s).
 * When altered, the dynamic shape saves the action required to alter the shape(s) in the static layer, so the changes
 * can be made to the static version later on - when the shape(s) are "demoted" back into the static layer.
 *
 * @param ids  The ids of the wrapped shape(s).
 * @param f  The predicate that transforms the selected shape(s) - or parts of them - into new immutable shape(s)
 * @see [[com.siigna.app.model.DynamicModel]]
 */
case class DynamicShape(ids : Seq[Int], f : TransformationMatrix => ImmutableShape) extends Shape
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
   * The attributes of the underlying ImmutableShapes.
   */
  def attributes = Attributes()

  /**
   * The boundary of the underlying ImmutableShapes.
   * @return A Rectangle2D.
   */
  def boundary = ids.map(Model(_)).foldLeft(Model(ids(0)).boundary)((a, b) => a.expand(b.boundary))

  /**
   * Calculates the distance from the vector and to the underlying ImmutableShape.
   * @param point  The point to calculate the distance to.
   * @param scale  The scale in which we are calculating.
   * @return  The length from the closest point of this shape to the point.
   */
  def distanceTo(point: Vector2D, scale: Double) = ids.map(Model(_).distanceTo(point)).reduceLeft((a, b) => if (a < b) a else b) * scale

  def setAttributes(attributes: Attributes) = this // TODO: Create some kind of (set/create/update)attribute action

  /**
   * Transforms the underlying ImmutableShape by adding a TransformShape action to the list of actions
   * applied to this DynamicShape
   * @param transformation  The TransformmationMatrix to apply to the shape.
   */
  def transform(transformation: TransformationMatrix) = {
    action = if (action.isDefined) {
      Some(action.get.merge(TransformShapes(ids, transformation)))
    } else {
      Some(TransformShapes(ids, transformation))
    }
    this
  }
}

/**
 * Companion-object for the DynamicShape class.
 */
object DynamicShape {

  /**
   * A method to create a DynamicShape with only one id.
   */
  def apply(id : Int, f : TransformationMatrix => ImmutableShape) = {
    new DynamicShape(Seq(id), f)
  }
  
}