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

package com.siigna.app.model.action

import com.siigna.app.model.shape.{ImmutableShape, Shape}
import com.siigna.app.model.Model
import com.siigna.util.geom.Vector2D

/**
 * Select objects. This action is crucial to
 * manipulating shapes, since this is the link between [[com.siigna.app.model.shape.ImmutableShape]]
 * and [[com.siigna.app.model.shape.DynamicShape]] that allows shapes to enter the
 * [[com.siigna.app.model.DynamicModel]]. DynamicShapes does not just duplicate shapes but also
 * contain information about smaller parts of a shape - if the user chose to only select a point for
 * instance - along with the transformation-matrix of the selection.
 *
 * TODO: Implement a class that selects only one shape.
 */
object Select {

  /**
   * Selects one [[com.siigna.app.model.shape.ImmutableShape]].
   * @param id  The ID of the shape to select.
   */
  def apply(id : Int) {
    Model select id
  }

  /**
   * Selects several [[com.siigna.app.model.shape.ImmutableShape]]s.
   * @param shapes  The shapes to select.
   */
  def apply(shapes : ImmutableShape*) {
    throw new UnsupportedOperationException("Not implemented yet")
  }

  /**
   * Selects several [[com.siigna.app.model.shape.ImmutableShape]]s.
   * @param shapes  The shapes to select.
   */
  def apply(shapes : Traversable[ImmutableShape]) {
    throw new UnsupportedOperationException("Not implemented yet")
    //Model execute new Select(shapes.map(s => Model.indexWhere(_ == s)).filter(_ >= 0))
  }

}