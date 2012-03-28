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
   * @param shape  The shape to select.
   */
  def apply(id : Int) {
    Model execute SelectShape(id)
  }

  /**
   * Selects several [[com.siigna.app.model.shape.ImmutableShape]]s.
   * @param shapes  The shapes to select.
   */
  def apply(shapes : ImmutableShape*) {
    //Model execute new Select(shapes.map(s => Model.indexWhere(_ == s)).filter(_ >= 0))
  }

  /**
   * Selects several [[com.siigna.app.model.shape.ImmutableShape]]s.
   * @param shapes  The shapes to select.
   */
  def apply(shapes : Traversable[ImmutableShape]) {
    //Model execute new Select(shapes.map(s => Model.indexWhere(_ == s)).filter(_ >= 0))
  }

}

/**
 * Selects a single shape.
 * @param id  The id of the shape as the key in the [[com.siigna.app.model.Model]].
 */
case class SelectShape(id : Int) extends VolatileAction {
  def execute(model: Model) = null

  def merge(that: Action) = null

  def undo(model: Model) = null
}

case class SelectShapeByPoint(id : Int, p : Vector2D) extends VolatileAction {
  def execute(model: Model) = null

  def merge(that: Action) = null

  def undo(model: Model) = null
}

/*case class SelectShapes(ids : Traversable[Int]) extends VolatileAction {

  def execute(model : Model) = {
    throw new UnsupportedOperationException("Not yet implemented.")
  }

  def merge(action : Action) = {
    throw new UnsupportedOperationException("Not yet implemented.")
  }

  def undo(model : Model) = {
    throw new UnsupportedOperationException("Not yet implemented.")
  }

} */