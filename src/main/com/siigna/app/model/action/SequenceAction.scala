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
 * A SequenceAction can execute a sequence of actions.
 */
case class SequenceAction(actions : Seq[Action]) extends Action {

  def execute(model : Model) : Model = actions.foldLeft(model)((m, action) => action.execute(m))
  
  def ids = actions.flatMap(_.ids)

  override def merge(that : Action) = that match {
    case SequenceAction(acts : Seq[Action]) => SequenceAction(actions ++ acts)
    case _ => SequenceAction(actions :+ that)
  }

  def undo(model : Model) : Model = actions.foldLeft(model)((m, action) => action.undo(m))
  
  def update(map : Map[Int, Int]) = copy(actions.map(_.update(map)))

}

/**
 * Factory object to [[com.siigna.app.model.action.SequenceAction]].
 */
object SequenceAction {

  def apply(action1 : Action, action2 : Action) = new SequenceAction(Seq(action1, action2))

}