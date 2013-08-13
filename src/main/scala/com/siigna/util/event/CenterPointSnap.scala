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

package com.siigna.util.event

import com.siigna.app.model.shape.{ArcShape, ClosedShape, Shape}
import com.siigna.util.geom.Vector2D
import com.siigna.app.view.View
import com.siigna.app.Siigna

/**
 * A hook for parsing points that snaps to center-points on objects.
 */
case object CenterPointSnap extends EventSnap {

  def parse(event : Event, model : Traversable[Shape]) = event match {
    case MouseMove(point, a, b) => {
      MouseMove(snap(point.transform(View.deviceTransformation), model).transform(View.drawingTransformation), a, b)
    }
    case some => some
  }

  def snap(point : Vector2D, model : Traversable[Shape]) : Vector2D = {
    model.collect {
        case s : ClosedShape => s.geometry.center
        case a : ArcShape => a.geometry.center
    } match {
      case m : Traversable[Vector2D] if m.isEmpty => point
      case m => {
        val newPoint = m.reduceLeft((a, b) => if (a.distanceTo(point) < b.distanceTo(point)) a else b)
        if (newPoint.distanceTo(point) <= Siigna.selectionDistance) newPoint else point
      }
    }
  }

}
