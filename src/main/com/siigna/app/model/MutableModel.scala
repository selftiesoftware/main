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

import action.{AddAttributes, SetAttributes, TransformShapeParts, Action}
import com.siigna.util.logging.Log


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
      val a = selection.get.attributes
      val t = selection.get.transformation
      val s = selection.get

      // Find the transformation
      val transformAction : Option[Action] = if (!t.isEmpty) {
        Some(TransformShapeParts(s, t))
      } else None

      val attributeAction : Option[Action] = if (!a.isEmpty) {
        Some(AddAttributes(s.keys, a))
      } else None

      // Remove the selection before executing actions to avoid recursion
      selection = None

      // Execute action
      if (transformAction.isDefined && attributeAction.isDefined) {
        Drawing execute transformAction.get.merge(attributeAction.get)
      } else if (transformAction.isDefined) {
        Drawing execute transformAction.get
      } else if (attributeAction.isDefined) {
        Drawing execute attributeAction.get
      }
    }
  }

  /**
   * Selects an entire shape based on its id.
   * @param id  The id of the shape.
   */
  override def select(id : Int) {
    select(Selection(id, Drawing(id).getPart))
  }
  
  override def select(ids : Traversable[Int]) {
    select(Selection(ids.map(id => id -> Drawing(id).getPart).toMap))
  }
  
  /**
   * Select a single shape with the given Selection information.
   * @param selection  The Selection representing the selection.
   */
  override def select(selection : Selection) {
    if (this.selection.isDefined) deselect()
    
    this.selection = Some(selection)

    Log.success("Model: Selected " + selection)
  }

}
