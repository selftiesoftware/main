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


import action.{VolatileAction, Action}
import com.siigna.util.logging.Log
import shape.ImmutableShape
import collection.parallel.immutable.{ParSeq, ParVector}

/**
 * A model that can store shapes, select and deselect shapes and group shapes.
 *
 * TODO: Examine possibility to implement an actor. Thread-less please.
 * 
 * @param shapes  The [[com.siigna.app.model.ImmutableModel]] where all the [[com.siigna.app.model.shape.Shape]]s are stored.
 */
sealed class Model(shapes : ImmutableModel) extends DynamicModel with GroupableModel

/**
 * The model of Siigna.
 */
object Model extends ParSeq[ImmutableShape] {

  /**
   * The [[com.siigna.app.model.action.Action]]s that have been executed on this model.
   */
  private var executed = Seq[Action]()

  /**
   * The underlying immutable model of Siigna.
   */
  private var model = new Model(new ImmutableModel(ParVector[ImmutableShape]()))

  /**
   * The [[com.siigna.app.model.action.Action]]s that have been undone on this model. 
   */
  private var undone = Seq[Action]()

  /**
   * Execute an action, list it as executed and clear the undone stack to make way for a new actions
   * (if it is not a [[com.siigna.app.model.action.VolatileAction]]).
   */
  def execute(action : Action) {
    model = action.execute(model)

    // Only store the action if it is not volatile
    if (!action.isInstanceOf[VolatileAction]) {
      executed +:= action
      undone = Seq()
    }
  }

  /**
   * Redo an action, by executing the last function that's been undone.
   */
  def redo() {
    if (undone.size > 0) {
      // Retrieve the event
      val action = undone.head
      undone = undone.tail

      // Execute the event and add it to the executed list
      model = action.execute(model)
      executed +:= action
    } else {
      Log.warning("Model: No more actions to redo.")
    }
  }

  /**
   * Undo an action and put it in the list of undone actions.
   */
  def undo() {
    if (executed.size > 0) {
      // Retrieve the action
      val action = executed.head
      executed = executed.tail

      // Undo it and add it to the undone list
      model = action.undo(model)
      undone +:= action
    } else {
      Log.warning("Model: No more actions to undo.")
    }
  }

  // Required by the ParIterable trait
  def apply(idx : Int) = model.shapes.apply(idx)
  def seq = model.shapes.seq
  def splitter = model.shapes.splitter

}