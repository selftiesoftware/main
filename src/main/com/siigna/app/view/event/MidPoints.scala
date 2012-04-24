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

package com.siigna.app.view.event

import com.siigna.util.geom._
import com.siigna.app.model.shape.Shape
import collection.parallel.immutable.{ParMap, ParIterable}

/**
 * A hook for parsing points that snap to mid-points on objects.
 */
case object MidPoints extends EventSnap {

  def parse(event : Event, model : Map[Int, Shape]) = event match {
    case MouseMove(point, a, b) => MouseMove(snap(point, model), a, b)
    case some => some
  }

  // TODO: Finish this
  def snap(point : Vector2D, model : Map[Int, Shape]) : Vector2D = {
    if (!model.isEmpty) {
      //val res = model.map(_ geometry match {
      //  case _ =>
      //})
      point
    } else point
  }

}
