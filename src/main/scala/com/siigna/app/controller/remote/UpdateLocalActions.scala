/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */

package com.siigna.app.controller.remote

import com.siigna.app.model.Model
import com.siigna.app.model.action.{Action, VolatileAction}

/**
 * An action that updates all the local actions and replaces local ids with remote ids.
 * @param map  A mapping of local ids to remote ids.
 */
protected[remote] sealed case class UpdateLocalActions(map : Map[Int, Int]) extends VolatileAction {

  def execute(model: Model) = {
    def replaceActionSeq(s : Seq[Action]) = if (s.exists(_.isLocal)) {
      s.map(a => if (a.isLocal) a.update(map) else a) } else s

    // Replace the shapes
    val shapes = if (model.shapes.exists(_._1 < 0)) {
      model.shapes.par.map(t => map.getOrElse(t._1, t._1) -> t._2)
    } else model.shapes

    // Find and replace all local executed actions
    val executed = replaceActionSeq(model.executed)

    // Find and replace all local undone action
    val undone = replaceActionSeq(model.undone)

    // Return the new model
    new Model(shapes.seq.toMap, executed, undone, model.attributes)
  }

}
