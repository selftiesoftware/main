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
 * The highest trait for objects that are shape-like in the hierarchy.
 * <br />
 * Shapes are what Siigna is made of. Every drawing is executed upon shapes, and the "database of Siigna" - the [[com.siigna.app.model.Model]]
 * - is made of Shapes, which is used to draw upon the [[com.siigna.app.view.View]] so the users can see what they're editing.
 * <br />
 * The shape hierarchy looks like this:
 * <pre>
 *
 *               Shape
 *                 |
 *      /----------+-----------\
 *  ImmutableShape       DynamicShape
 *   (immutable)          (mutable)
 *   |    |    |
 *   |    |  CollectionShape (contains several shapes)
 *   |    |      |
 *   |    |      +--- GroupShape
 *   |    |      |
 *   |    |      +--- PolylineShape
 *   |    |
 *   |  BasicShape (used in PolylineShape)
 *   |      |
 *   |      +----- ArcShape
 *   |      |
 *   |      +----- LineShape
 *   |
 *  EnclosedShape (encapsulates a given area)
 *     |
 *     +--- CircleShape
 *     |
 *     +--- RectangleShape
 *            |
 *            +--- ImageShape
 *            |
 *            +--- TextShape
 * </pre>
 */
trait Shape {

  type T <: Shape

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
  def addAttributes(attributes : (String, Any)*) = setAttributes(this.attributes ++ attributes)

  /**
   * Returns a rectangle that includes the entire shape.
   */
  def boundary : Rectangle2D

  /**
   * Calculates the closest distance to the shape using the views current zoom
   * scale.
   */
  def distanceTo(point : Vector2D) : Double = distanceTo(point, View.zoom)

  /**
   * Calculates the closest distance to the shape in the given scale.
   */
  def distanceTo(point : Vector2D, scale : Double) : Double

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