/*
 * Copyright (c) 2011. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.model.shape

import com.siigna.app.model.action.{Action, TransformShape}
import com.siigna.util.collection.Attributes
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.util.Implicits._

/**
* A dynamic shape is a mutable wrapper for a regular Shape.
* When altered, the dynamic shape saves the action required to alter the shape in the static layer, so the changes
* can be made to the static version later on, when the shape is "demoted" back into the static layer.
*
* @param id  The id of the wrapped shape.
* @param immutableShape  The original immutable shape.
*/
case class DynamicShape(id : String, immutableShape : ImmutableShape) extends Shape {

  /**
   * The action performed on the shape.
   * TODO: Refactor to a Option[Action] and use merge.
   */
  private var underlyingActions = Seq[Action]()

  /**
   * The underlying shape used to represent the current state of the dynamic shape.
   */
  private var underlyingShape = immutableShape

  /**
   * The actions performed on the DynamicShape.
   */
  def actions = underlyingActions

  def attributes = underlyingShape.attributes

  def boundary = underlyingShape.boundary

  def distanceTo(point : Vector2D, scale : Double) = underlyingShape.distanceTo(point, scale)

  /**
   * Returns a new shape with a new set of attributes.
   */
  def setAttributes(attributes : Attributes) : ImmutableShape =
    underlyingShape.setAttributes(attributes)

  /**
   * Save the attributes to the underlying static shape and return this.
   */
  def setDynamicAttributes(attributes : Attributes) = {
    underlyingShape = underlyingShape.setAttributes(attributes)
    //TODO: underlyingActions = underlyingActions :+ UpdateShape(id, attributes)
    this
  }

  /**
   * Returns the underlying static shape.
   */
  def shape : ImmutableShape = underlyingShape

  /**
   * Applies a transformation to the shape.
   */
  def transform(transformation : TransformationMatrix) : ImmutableShape =
    underlyingShape.transform(transformation)

  /**
   * Transforms the dynamic shape with a <code>TransformationMatrix</code>.
   */
  def transformDynamic(t : TransformationMatrix) = {
    underlyingShape = underlyingShape.transform(t)
    underlyingActions = underlyingActions :+ TransformShape(id, t)
    this
  }

}