/*
 * Copyright (c) 2011. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.view.event

import com.siigna.app.model.shape.{ImmutableShape}
import com.siigna.util.geom.Vector
import com.siigna.app.Siigna

/**
 * A hook for parsing points that snaps to center-points on objects.
 */
case object CenterPoints extends EventSnap {

  def parse(event : Event, model : Iterable[ImmutableShape]) = event match {
    case MouseMove(point, a, b) => MouseMove(snap(point, model), a, b)
    case some => some
  }

  def snap(point : Vector, model : Iterable[ImmutableShape]) : Vector = {
    if (!model.isEmpty) {
      val res = model.map(_.geometry.center).reduceLeft((a, b) => if (a.distanceTo(point) < b.distanceTo(point)) a else b)
      if (res.distanceTo(point) * Siigna.zoom <= 10) {
        res
      }
      else point
    } else point
  }

}
