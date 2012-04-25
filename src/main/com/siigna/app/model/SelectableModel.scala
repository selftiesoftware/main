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

package com.siigna.app.model

/**
 * A model that can be selected and deselected.
 */
trait SelectableModel {

  /**
   * The MutableModel on which the selections can be performed.
   */
  protected def model : MutableModel

  /**
   * Deselects selected shapes in the Model and apply the action(s) executed on the shapes since selection.
   */
  def deselect() { model.deselect() }

  /**
   * Selects an entire shape based on its id.
   * @param id  The id of the shape.
   */
  def select(id : Int) { model select id }

  /**
  * Select a single shape as written in the given Selection.
  * @param shape  The Selection representing the selection.
  */
  def select(shape : Selection) { model select shape }

}
