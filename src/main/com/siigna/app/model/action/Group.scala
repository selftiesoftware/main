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

import com.siigna.util.logging.Log
import com.siigna.app.model.Model
import com.siigna.app.model.shape.{GroupShape, ImmutableShape}

/**
 * Groups objects or merges objects into existing groups.
 */
object Group {

  /**
   * Group a number of shapes.
   */
  def apply(shapes : Traversable[ImmutableShape]) {
    if (shapes.size > 1) {
      val ids = Model.findIds(shapes)
      if (ids.size > 1) {
        Model(GroupAction(ids))
      } else {
        Log.info("Group: The objects attempted to group did not exist in the model. Returning None.")
      }
    } else Log.info("Group: You cannot group less than 2 objects.")
  }

}

/**
 * Groups a number of shapes.
 */
case class GroupAction(ids : Traversable[String]) extends Action {

  def execute(model : Model) = {
    model.group(ids)
    model
  }

  def merge(that : Action) = SequenceAction(this, that)

  def undo(model : Model) = {
    model.ungroup(GroupShape(ids))
    model
  }

}
