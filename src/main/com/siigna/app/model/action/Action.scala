/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
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
import com.siigna.util.SerializableProxy

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
   * The identifiers (ids) used by the action.
   * @return  Any number of id's used in the action.
   */
  def ids : Traversable[Int]

  /**
   * Defines if the action contains local (negative) ids and thus should not be sent to the server.
   * <br>
   * This definition is used to avoid clashes with remote and local ids.
   */
  def isLocal : Boolean = ids.exists(_ < 0)

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

  /**
   * Updates the identifiers (ids) in the action with the given map.
   * If the id exists in the collection it is replaced with the mapped value.
   * @param map  A map containing links from the old keys (keys) to the new keys (values).
   * @return  A copy of the existing action with the new ids replaced (if any matches exist).
   */
  def update(map : Map[Int, Int]) : Action

}

/**
 * An [[com.siigna.app.model.action.Action]] that is not stored in the [[com.siigna.app.model.Model]]
 * after execution and thus cannot be undone or redone.
 */
trait VolatileAction extends Action {

  /**
   * It makes no sense for a volatile action to store its ids - it won't be evaluated later on!
   * @return  An empty Traversable.
   */
  def ids = Traversable.empty[Int]
  
  /**
   * A Volatile Action cannot undo.
   */
  override final def undo(model : Model) : Model = {
    throw new UnsupportedOperationException("A VolatileAction can not be undone.")
  }

  /**
   * A VolatileAction is not stored, hence it makes no sense to update it!
   * @return  The exact same action as you had before.
   */
  def update(map : Map[Int, Int]) = this

}