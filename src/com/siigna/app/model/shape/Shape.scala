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

import com.siigna.app.Siigna
import com.siigna.util.collection.Attributes
import com.siigna.util.geom.{TransformationMatrix, Rectangle, Vector}

/**
 * The highest trait for objects that are shape-like in the hierarchy. This includes having a boundary, a distance to a
 * point in a 2D-space and a set of attributes. 
 */
trait Shape {

  /**
   * The attributes of the shape.
   */
  def attributes : Attributes

  /**
   * Replace all attributes in this shape with new ones.
   *
   * @param  attributes  the new attributes to replace the old ones.
   * @return  a shape with the new attributes.
   */
  def attributes_=(attributes : Attributes) = setAttributes(attributes)

  /**
   * Merge the new attributes in with the existing ones, eventually overwriting
   * attributes with new values.
   *
   * @param  attributes  the new attributes to merge in.
   * @return  a shape with the updated attributes.
   *
   * TODO: Refactor to addAttribute
   */
  def attributes_+=(attribute : (String, Any)) = attributes += attribute

  /**
   * Merge the new attributes in with the existing ones, eventually overwriting
   * attributes with new values.
   *
   * @param  attributes  the new attributes to merge in.
   * @return  a shape with the updated attributes.
   *
   * TODO: Refactor to addAttributes
   */
  def attributes_+=(attributes : (String, Any)*) = attributes_=(this.attributes ++ attributes)

  /**
   * Returns a rectangle that includes the entire shape.
   */
  def boundary : Rectangle

  /**
   * Calculates the closest distance to the shape using the views current zoom
   * scale.
   */
  def distanceTo(point : Vector) : Double = distanceTo(point, Siigna.zoom)

  /**
   * Calculates the closest distance to the shape in the given scale.
   */
  def distanceTo(point : Vector, scale : Double) : Double

  /**
   * Returns a new shape with a new set of attributes.
   */
  def setAttributes(attributes : Attributes) : Shape

  /**
   * Applies a transformation to the shape.
   */
  def transform(transformation : TransformationMatrix) : Shape
  
}