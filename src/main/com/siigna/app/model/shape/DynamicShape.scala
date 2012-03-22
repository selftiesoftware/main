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

/**
* A dynamic shape is a mutable wrapper for a regular Shape.
* When altered, the dynamic shape saves the action required to alter the shape in the static layer, so the changes
* can be made to the static version later on, when the shape is "demoted" back into the static layer.
*
* @param id  The id of the wrapped shape.
* @param shape  The original immutable shape.
*/
case class DynamicShape(id : Int, shape : ImmutableShape) extends Shape
                                                             with (TransformationMatrix => ImmutableShape) {

  type T = ImmutableShape

  def apply(t : TransformationMatrix) = shape.transform(t)
  
  def attributes = shape.attributes

  def boundary = null

  def distanceTo(point: Vector2D, scale: Double) = 0

  def setAttributes(attributes: Attributes) = null

  def transform(transformation: TransformationMatrix) = null
}