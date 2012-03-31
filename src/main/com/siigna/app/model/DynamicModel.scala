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
 * A mutable model containing one single [[com.siigna.app.model.shape.DynamicShape]], representing one or more
 * selected shapes.
 *
 * @see [[com.siigna.app.model.Model]]
 */
trait DynamicModel {

  /**
   * The Dynamic Shape.
   */
  private var dynamic : Option[DynamicShape] = None

  /**
   * Deselect the [[com.siigna.app.model.shape.DynamicShape]] in the Model and apply the action(s)
   * executed on the shape since it was selected.
   * [[com.siigna.app.model.Model]].
   */
  def deselect() {
    if (dynamic.isDefined) {
      if (dynamic.get.action.isDefined) {
        Model execute dynamic.get.action.get
      }
      dynamic = None
    }
  }

  /**
   * Returns the current selection if any.
   */
  def getSelection = dynamic

  /**
   * Select a single shape. If previous shapes has been selected, deselect it and select the given shape instead.
   * @param shape  The shape to select
   */
  def select(shape : DynamicShape) {
    if (dynamic.isDefined) deselect()
    
    dynamic = Some(shape)
  }

}
