package com.siigna.app.model

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

import com.siigna.app.model.action.Action
import shape.Shape

/**
 * A Model capable of executing, undoing and redoing [[com.siigna.app.model.action.Action]]s.
 */
trait ActionModel {

  /**
   * The underlying immutable model of Siigna.
   */
  @volatile protected var model = new Model(Map[Int, Shape]())

  /**
   * The [[com.siigna.app.model.action.Action]]s that have been executed on this model.
   */
  protected var executed = Seq[Action]()

  /**
   * The [[com.siigna.app.model.action.Action]]s that have been undone on this model.
   */
  protected var undone = Seq[Action]()

  /**
   * <p>Execute an action, lists it as executed and clears the undone stack to make way for new actions
   * (if they are not instances of [[com.siigna.app.model.action.VolatileAction]]).</p>
   *
   * @param action  The action to execute.
   */
  def execute(action: Action)

  /**
   * <p>Redo an action, by executing the last function that's been undone.</p>
   */
  def redo()

  /**
   * <p>Undo an action and put it in the list of undone actions.</p>
   */
  def undo()

}