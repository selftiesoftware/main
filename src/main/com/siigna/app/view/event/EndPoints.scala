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

import com.siigna.app.model.shape._
import com.siigna.util.geom.{Vector2D}
import com.siigna.app.model.shape.{PolylineShape}
import com.siigna.app.view.View
import collection.parallel.immutable.ParIterable

/**
 * A hook for parsing points that snaps to end-points of objects.
 */
case object EndPoints extends EventSnap {

  def parse(event : Event, model : ParIterable[ImmutableShape]) = event match {
    case MouseDown(point, a, b)  => MouseDown(snap(point, model), a, b)
    case MouseDrag(point, a, b)  => MouseDrag(snap(point, model), a, b)
    case MouseMove(point, a, b)  => MouseMove(snap(point, model), a, b)
    case MouseUp(point, a, b)    => MouseUp(snap(point, model), a, b)
    case some => some
  }

  def snap(point : Vector2D, model : ParIterable[ImmutableShape]) : Vector2D = {
    def closestTwo(p1 : Vector2D, p2 : Vector2D) = if (p1.distanceTo(point) < p2.distanceTo(point)) p1 else p2
    def closestPoints(points : Seq[Vector2D]) = points.reduceLeft((p1, p2) => if (p1.distanceTo(point) < p2.distanceTo(point)) p1 else p2)

    if (!model.isEmpty) {
      val res = model.map(_ match {
        case s : ArcShape => closestPoints(s.geometry.vertices)
        case s : CircleShape => closestPoints(s.handles)
        //case s : ImageShape => closestPoints(s.geometry.vertices)
        case LineShape(start, end, _) => closestTwo(start, end)
        case s : PolylineShape => closestPoints(s.geometry.vertices)
        case s : TextShape => closestPoints(s.geometry.vertices)
      })
      val closestPoint = res.reduceLeft(closestTwo)

      if (closestPoint.distanceTo(point) * View.zoom <= 10) {
        closestPoint
      }
      else point
    } else point
  }

}
