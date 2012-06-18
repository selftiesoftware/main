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

import com.siigna.app.model.HasAttributes
import com.siigna.util.geom.{TransformationMatrix, Vector2D, Rectangle2D}

/**
 * <p>The highest trait for objects that are shape-like in Siigna.
  * Shape-like objects have attributes, a Minimum-Bounding Rectangle, information
  * about its distance to other objects and an ability to be transformed with
  * a [[com.siigna.util.geom.TransformationMatrix]].</p>
  *
  * <p>HasAttributes has been made to describe items that contains spatial information
  * like Shapes, but should not be stored or otherwise treated like a regular Shape.
  * As an example a [[com.siigna.app.model.Selection]] extends HasAttributes.</p>
 */
trait ShapeLike extends HasAttributes {

  type T <: ShapeLike

  /**
   * Returns a rectangle that includes the entire shape.
   */
  def boundary : Rectangle2D

  /**
   * Calculates the closest distance to the shape in the given scale.
   */
  def distanceTo(point : Vector2D, scale : Double) : Double

  /**
   * Applies a transformation to the shape.
   */
  def transform(transformation: TransformationMatrix): T
}