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

import scala.collection.mutable.Stack
import com.siigna._

/**
 * Saved actions associated with a given model. Ordered by LIFO (Last In First Out).
 */
trait ActionModel {

  protected def model : ImmutableModel

  /**
   * Every executed action.
   */
  val executed = new Stack[Action]()

  /**
   * Every undone action.
   */
  val undone = new Stack[Action]()

  /**
   * Execute an action, list it as executed and clear the undone stack to make way for a new actions.
   */
  def execute(action : Action) { try {
    executed push action
    action.execute(model)
    undone.clear
  } catch {
    case e => Log.warning("Model failed to execute action: "+action.getClass.getName+" with error: "+e)
  } }

  /**
   * Redo an action, by executing the last function that's been undone.
   */
  def redo : Unit = try {
    val action = undone.pop
    action.execute(model)
    executed push action
  } catch {
    case e => Log.warning("Model failed to redo action: Did not exist in the action-list.")
  }

  /**
   * Undo an action and put it in the list of undone actions.
   */
  def undo : Unit = try {
    val action = executed.pop
    action.undo(model)
    undone push action
  } catch {
    case e => Log.warning("Model failed to undo action: Did not exist in the action-list.")
  }

}