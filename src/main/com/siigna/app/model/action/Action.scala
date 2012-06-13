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
import com.siigna.app.model.shape.Shape

/**
 * <p>An action is an immutable representation of a state-change in the model and <b>the only way</b>
 * to alter shapes and groups in the [[com.siigna.app.model.Model]].</p>
 *
 *
 * @define actionFactory associated action
 * @define actionDescription
 * <p>Actions should not be called directly, but created through the corresponding $actionFactory. The
 * $actionFactory object ensures the action is instantiated properly and sent to the [[com.siigna.app.model.Model]]
 * for execution.</p>
 * <p>The use of actions are then very simple. The $actionFactory provides a lot of syntactic sugar. As an example
 * a call to Create with a shape can look like this:
 * {{{
 *   Create(shape)
 *   Create(id, shape)
 *   Create(shapes)
 * }}}
 * These calls will be forwarded to the appropriate Action and then sent on to the Model (specifically the
 * [[com.siigna.app.model.Model]]). In a single line of code the action is created and executed. Handy right?
 * </p>
 *
 * @see [[com.siigna.app.model.Model]]
 */
trait Action extends Serializable {

  /**
   * Executes the action on a given model.
   */
  def execute(model : Model) : Model

  /**
   * The method merges this action in with another action, so this action is performed first.
   */
  def merge(that : Action) = that match {
    case SequenceAction(acts : Seq[Action]) => SequenceAction(Seq(this) ++ acts)
    case _ => SequenceAction(this, that)
  }

  /**
   * Undo the action on a given model.
   */
  def undo(model : Model) : Model

}

/**
 * An [[com.siigna.app.model.action.Action]] that is not stored in the [[com.siigna.app.model.Model]]
 * after execution and thus cannot be undone or redone.
 */
trait VolatileAction extends Action {

  /**
   * A Volatile Action cannot undo.
   */
  override final def undo(model : Model) : Model = {
    throw new UnsupportedOperationException("A VolatileAction can not be undone.")
  }
  
}