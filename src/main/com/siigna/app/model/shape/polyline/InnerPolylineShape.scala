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

package com.siigna.app.model.shape.polyline

import com.siigna.app.model.shape.ImmutableShape
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import collection.LinearSeqLike

/**
 * A shape type used in the PolylineShape. This shape is dependent on the later shape in the PolylineShape,
 * so we (1) ensure that the shapes are connected and (2) avoids any duplicated points.
 * TODO: Read up on any possible scala standard abstractions.
 */
protected[shape] trait InnerPolylineShape {

  /**
   * The head of the InnerPolylineShape.
   */
  def head : Vector2D

  /**
   * Examines if the element is empty.
   */
  def isEmpty : Boolean

  /**
   * The tail of the InnerPolylineShape.
   */
  def tail : InnerPolylineShape

  /**
   * Returns this PolylineShape as an ImmutableShape.
   */
  def toShapes : Seq[ImmutableShape]

  /**
   * Transforms the polyline shape <b>and the entire tail</b> with the given TransformationMatrix.
   * @param t  The transformation to apply
   * @return  A new PolylineShape
   */
  def transform(t : TransformationMatrix) : InnerPolylineShape

}