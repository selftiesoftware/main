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

import com.siigna.app.model.{Selection, Model}
import com.siigna.util.geom.{Rectangle2D, Vector2D}
import com.siigna.app.model.shape.{EmptyShapeSelector, Shape, ShapeLike}

/**
 * Select objects. This action is crucial to
 * manipulating shapes, since this is the link between [[com.siigna.app.model.shape.Shape]]
 * and [[com.siigna.app.model.shape.DynamicShape]] that allows shapes to enter the
 * [[com.siigna.app.model.MutableModel]]. DynamicShapes does not just duplicate shapes but also
 * contain information about smaller parts of a shape - if the user chose to only getPart a point for
 * instance - along with the transformation-matrix of the selection.
 *
 * TODO: Implement a class that selects only one shape.
 */
object Select {

  /**
   * Selects one [[com.siigna.app.model.shape.Shape]].
   * @param id  The ID of the shape to getPart.
   */
  def apply(id : Int) {
    Model select id
  }
  
  def apply(id : Int, point : Vector2D) {
    Model.select(id, Model(id).getPart(point))
  }
  
  def apply(id : Int, r : Rectangle2D) {
    Model.select(id, Model(id).getPart(r))
  }
  
  def apply(r : Rectangle2D) {
    // TODO: Find everything close to the rectangle
    val selection = Model(r).map(t => t._1 -> t._2.getPart(r))
    println("Selected: " + selection)
    val filtered = selection.filter(_._2 != EmptyShapeSelector)
    println("Selected: " + filtered)
    Model.select(Selection(filtered))
  }

  /**
   * Selects several [[com.siigna.app.model.shape.Shape]]s.
   * @param shapes  The shapes to getPart.
   */
  def apply(shapes : Shape*) {
    throw new UnsupportedOperationException("Not implemented yet")
  }

  /**
   * Selects several [[com.siigna.app.model.shape.Shape]]s.
   * @param shapes  The shapes to getPart.
   */
  def apply(shapes : Traversable[Shape]) {
    throw new UnsupportedOperationException("Not implemented yet")
    //Model execute new Select(shapes.map(s => Model.indexWhere(_ == s)).filter(_ >= 0))
  }

}