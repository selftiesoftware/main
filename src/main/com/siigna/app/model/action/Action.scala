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

import com.siigna.app.model.Model

/**
 * An action is an immutable representation of a state-change in the model and <b>the only way</b> to alter shapes and groups in the Model.
 */
trait Action {

  /**
   * Executes the action on a given model.
   */
  def execute(model : Model) : Model

  /**
   * The method merges this action in with another action, so this action is performed first.
   */
  def merge(that : Action) : Action

  /**
   * Undo the action on a given model.
   */
  def undo(model : Model) : Model

}


