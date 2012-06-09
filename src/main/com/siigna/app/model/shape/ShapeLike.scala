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

import com.siigna.app.Siigna
import com.siigna.util.collection.Attributes
import com.siigna.util.geom.{TransformationMatrix, Rectangle2D, Vector2D}
import com.siigna.app.view.View

/**
 * <p>The highest trait for objects that are shape-like in Siigna.
 * Shape-like objects have attributes, a Minimum-Bounding Rectangle, information
 * about its distance to other objects and an ability to be transformed with
 * a [[com.siigna.util.geom.TransformationMatrix]].</p>
 *
 * <p>ShapeLike has been made to describe items that contains spatial information
 * like Shapes, but should not be stored or otherwise treated like a regular Shape.
 * As an example a [[com.siigna.app.model.Selection]] extends ShapeLike.</p>
 */
trait ShapeLike {

  type T <: ShapeLike

  /**
   * The attributes of the shape.
   */
  def attributes : Attributes

  /**
   * Merge the new attributes in with the existing ones, eventually overwriting
   * attributes with new values.
   *
   * @param  attribute  the new attribute to merge in.
   * @return  a shape with the updated attributes.
   */
  def addAttribute(attribute : (String, Any)) = setAttribute(attribute)

  /**
   * Merge the new attributes in with the existing ones, eventually overwriting
   * attributes with new values.
   *
   * @param  attributes  the new attributes to merge in.
   * @return  a shape with the updated attributes.
   */
  def addAttributes(attributes : Attributes) = setAttributes(this.attributes ++ attributes)

  /**
   * Returns a rectangle that includes the entire shape.
   */
  def boundary : Rectangle2D


  /**
   * Calculates the closest distance to the shape in the given scale.
   */
  def distanceTo(point : Vector2D, scale : Double) : Double

  /**
   * Removes one attribute from the set of current attributes, if it exists.
   *
   * @param attribute  The attribute to remove.
   * @return  A ShapeLike with the attribute removed.
   */
  def removeAttribute(attribute : String) = setAttributes(attributes - attribute)

  /**
   * Remotes a set of attributes from the current attributes, if they exist.
   * @param attributes  The attributes to remove
   * @return  A ShapeLike with the attributes removed.
   */
  def removeAttributes(attributes : Traversable[String]) = setAttributes(this.attributes.--(attributes))

  /**
   * Merge the new attributes in with the existing ones, eventually overwriting
   * attributes with new values.
   *
   * @param  attribute  the new attribute to merge in.
   * @return  a shape with the updated attributes.
   */
  def setAttribute(attribute : (String, Any)) = setAttributes(attributes + attribute)

  /**
   * Returns a new shape with a new set of attributes.
   */
  def setAttributes(attributes : Attributes) : T

  /**
   * Applies a transformation to the shape.
   */
  def transform(transformation : TransformationMatrix) : T
  
}