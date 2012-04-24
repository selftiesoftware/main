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

import action.{TransformShapes, Action}
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
      val t = selection.get.getTransformation
      val s = selection.get
      var action : Option[Action] = if (!t.isEmpty) {
        Some(TransformShapes(s.shapes, t))
      } else None
      
      if (selection.get.action.isDefined && action.isDefined) {
        action = Some(action.get.merge(s.action.get))
      } else if (selection.get.action.isDefined) {
        action = Some(s.action.get)
      }

      // Execute action
      if (action.isDefined) Model execute action.get
      
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
   * ways to convert a DynamicShape to one or more [[com.siigna.app.model.shape.Shape]] depending on
   * the number of shapes included in the selection..
   */
  var selection : Option[DynamicShape] = None

}
