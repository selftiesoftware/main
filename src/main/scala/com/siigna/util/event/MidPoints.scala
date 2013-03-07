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

package com.siigna.util.event

import com.siigna.util.geom._
import com.siigna.app.model.shape.Shape
import collection.parallel.immutable.{ParMap, ParIterable}
import com.siigna.app.view.View

/**
 * A hook for parsing points that snap to mid-points on objects.
 */
case object MidPoints extends EventSnap {

  def parse(event : Event, model : Traversable[Shape]) = event match {
    case MouseMove(point, a, b) => MouseMove(snap(point, model), a, b)
    case some => some
  }

  // TODO: Finish this
  def snap(q : Vector2D, model : Traversable[Shape]) : Vector2D = {
    //val point = q.transform(View.deviceTransformation)
    if (!model.isEmpty) {
      //val res = model.map(_ geometry match {
      //  case _ =>
      //})
      q
    } else q
  }

}
