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

import com.siigna.app.model.shape._
import com.siigna.util.geom.{Geometry, Vector}
import com.siigna.app.model.shape.{PolylineShape, Shape}
import com.siigna.util.event._
import com.siigna.app.Siigna

/**
 * A hook for parsing points that snaps to end-points of objects.
 */
case object EndPoints extends EventSnap {

  def parse(event : Event, model : Iterable[ImmutableShape]) = event match {
    case MouseDown(point, a, b)  => MouseDown(snap(point, model), a, b)
    case MouseDrag(point, a, b)  => MouseDrag(snap(point, model), a, b)
    case MouseMove(point, a, b)  => MouseMove(snap(point, model), a, b)
    case MouseUp(point, a, b)    => MouseUp(snap(point, model), a, b)
    case some => some
  }

  def snap(point : Vector, model : Iterable[ImmutableShape]) : Vector = {
    def closestTwo(p1 : Vector, p2 : Vector) = if (p1.distanceTo(point) < p2.distanceTo(point)) p1 else p2
    def closestPoints(points : Seq[Vector]) = points.reduceLeft((p1, p2) => if (p1.distanceTo(point) < p2.distanceTo(point)) p1 else p2)

    if (!model.isEmpty) {
      val res = model.map(_ match {
        case ArcShape(start, middle, end, _) => closestTwo(start, end)
        case s : CircleShape => closestPoints(s.handles)
        case s : ImageShape => closestPoints(s.geometry.points)
        case LineShape(start, end, _) => closestTwo(start, end)
        case PointShape(p, _) => p
        case s : PolylineShape => closestPoints(s.geometry.handles)
        case s : TextShape => closestPoints(s.geometry.points)
      })
      val closestPoint = res.reduceLeft(closestTwo)

      if (closestPoint.distanceTo(point) * Siigna.zoom <= 10) {
        closestPoint
      }
      else point
    } else point
  }

}
