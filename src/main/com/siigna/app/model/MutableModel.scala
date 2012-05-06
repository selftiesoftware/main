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

import action.{TransformShapeParts, Transform, TransformShapes, Action}
import shape.{PartialShape, Shape}


/**
 * A model with a mutable "layer" that, if not empty, consists of a single [[com.siigna.app.model.Selection]],
 * representing parts of one or more selected shapes.
 *
 * <br />
 * The MutableModel is a way to dynamically manipulate the underlying [[com.siigna.app.model.ImmutableModel]]
 * through a temporary "dynamic layer" that can be changed and updated without any effect on the actual
 * shapes. This can be very useful (and give enourmous performance benefits) when you need to alter shapes
 * many times before storing any final changes.
 *
 * <br />
 * The actual selection consists of a map of Ints and [[com.siigna.app.model.shape.ShapeSelector]]s.
 *
 * @see Model
 * @see Selection
 * @see ShapeSelector
 */
trait MutableModel extends SelectableModel {

  /**
   * The current selection, represented by a [[com.siigna.app.model.Selection]] containing
   * ways to convert a Selection to one or more [[com.siigna.app.model.shape.Shape]] depending on
   * the number of shapes included in the selection..
   */
  var selection : Option[Selection] = None

  def model = this

  /**
   * Deselect the [[com.siigna.app.model.Selection]] in the Model and apply the action(s)
   * executed on the shape since it was selected.
   * [[com.siigna.app.model.Model]].
   */
  override def deselect() {
    if (selection.isDefined) {
      val t = selection.get.getTransformation
      val s = selection.get
      var action : Option[Action] = if (!t.isEmpty) {
        // TODO: Do this in the Transform action instead...
        val parts = s.map(e => e._1 -> e._2)
        Some(TransformShapeParts(parts, t))
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
    select(Selection(id, Model(id).getPart))
  }
  
  /**
   * Select a single shape with the given Selection information.
   * @param shape  The Selection representing the selection.
   */
  override def select(shape : Selection) {
    if (selection.isDefined) deselect()
    
    selection = Some(shape)
  }

}
