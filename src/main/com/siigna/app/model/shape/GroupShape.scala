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

import com.siigna.app.model.Model
import com.siigna.util.collection.Attributes
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.util.dxf.{DXFValue, DXFSection}

/**
 * A Group that contains <b>references</b> to other shape and is thus only used as a container.
 *
 * @param shapes  The shapes stored in the GroupShape.
 * @param attributes  Attributes to be applied on the shapes in the collection.
 * TODO: Implement additions and subtractions
 */
case class GroupShape(shapes : Seq[ImmutableShape], attributes : Attributes) extends CollectionShape[ImmutableShape] {

  type T = GroupShape

  /**
   * Returns a new collection with a new set of attributes. In other words return a collection with a new id,
   * but otherwise the same attributes.
   */
  def setAttributes(attributes : Attributes) : GroupShape = copy(attributes = attributes)

  def toDXF = new DXFSection(Seq())

  /**
   * Applies a transformation to the shape.
   * TODO: Make a pivotal point for the groupshape?
   */
  def transform(transformation : TransformationMatrix) = this

}

/**
 * Object used for quick access to and constructor overloading for the <code>GroupShape</code> class.
 */
object GroupShape {

  def apply(shapes : Traversable[ImmutableShape]) = new GroupShape(shapes.toSeq, Attributes())

}