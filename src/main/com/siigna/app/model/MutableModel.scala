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

import shape.{DynamicShape}

/**
 * A selectable model containing one single [[com.siigna.app.model.shape.DynamicShape]], representing one or more
 * selected shapes.
 *
 * @see [[com.siigna.app.model.Model]]
 */
trait MutableModel extends SelectableModel {

  def model = this

  /**
   * Deselect the [[com.siigna.app.model.shape.DynamicShape]] in the Model and apply the action(s)
   * executed on the shape since it was selected.
   * [[com.siigna.app.model.Model]].
   */
  override def deselect() {
    if (selection.isDefined) {
      if (selection.get.action.isDefined) {
        Model execute selection.get.action.get
      }
      selection = None
    }
  }

  /**
   * Selects an entire shape based on its id.
   * @param id  The id of the shape.
   */
  override def select(id : Int) {
    select(DynamicShape(id, Model(id).select()))
  }
  
  /**
   * Select a single shape with the given DynamicShape information.
   * @param shape  The DynamicShape representing the selection.
   */
  override def select(shape : DynamicShape) {
    if (selection.isDefined) deselect()
    
    selection = Some(shape)
  }

  /**
   * The current selection, represented by a [[com.siigna.app.model.shape.DynamicShape]] containing
   * ways to convert a DynamicShape to one or more [[com.siigna.app.model.shape.ImmutableShape]] depending on
   * the number of shapes included in the selection..
   */
  var selection : Option[DynamicShape] = None

}
