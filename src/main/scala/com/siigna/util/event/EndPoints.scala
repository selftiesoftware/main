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

import com.siigna.app.model.shape._
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.app.model.shape.{PolylineShape}
import com.siigna.app.view.{Graphics, View}
import collection.parallel.immutable.{ParMap, ParIterable}
import java.awt.Color
import com.siigna.app.Siigna

/**
 * A hook for parsing points that snaps to end-points of objects.
 */
case object EndPoints extends EventSnap {
  var snapPoint : Option[Vector2D] = None

  def snapBox (p : Vector2D) = {
    val a = 0.3 * Siigna.selectionDistance
    Array(Vector2D(p.x-a,p.y-a),Vector2D(p.x-a,p.y+a),Vector2D(p.x+a,p.y+a),Vector2D(p.x+a,p.y-a),Vector2D(p.x-a,p.y-a))
  }
  def parse(event : Event, model : Traversable[Shape]) = event match {
    case MouseDown(point, a, b)  => MouseDown(snap(point, model), a, b)
    case MouseDrag(point, a, b)  => MouseDrag(snap(point, model), a, b)
    case MouseMove(point, a, b)  => MouseMove(snap(point, model), a, b)
    case MouseUp(point, a, b)    => MouseUp(snap(point, model), a, b)
    case some => some
  }

  def snap(q : Vector2D, model : Traversable[Shape]) : Vector2D = {
    val point = q.transform(View.deviceTransformation)
    def closestTwo(p1 : Vector2D, p2 : Vector2D) = if (p1.distanceTo(point) < p2.distanceTo(point)) p1 else p2
    def closestPoints(points : Seq[Vector2D]) = points.reduceLeft(closestTwo)

    if (!model.isEmpty) {
      val res = model.map(_ match {
        case s : ArcShape => closestPoints(s.geometry.vertices)
        case s : CircleShape => closestPoints(s.geometry.vertices)
        //case s : ImageShape => closestPoints(s.geometry.vertices)
        case LineShape(start, end, _) => closestTwo(start, end)
        case s : PolylineShape => closestPoints(s.geometry.vertices)
        case s : TextShape => closestPoints(s.geometry.vertices)
        case _ => point
      })
      val closestPoint = res.reduceLeft(closestTwo)
      if (closestPoint.distanceTo(point) < Siigna.selectionDistance) {
        snapPoint = Some(closestPoint)
        closestPoint.transform(View.drawingTransformation)
      } else {
        snapPoint = None
        q
      }
    } else {
      snapPoint = None
      q
    }
  }

  override def paint(g : Graphics, t : TransformationMatrix) {
    def drawFill (fillShape: Array[Vector2D], color : Color, transformation : TransformationMatrix) {
      val fillVector2Ds = fillShape.map(_.transform(transformation))
      val fillScreenX = fillVector2Ds.map(_.x.toInt).toArray
      val fillScreenY = fillVector2Ds.map(_.y.toInt).toArray
      g setColor color
      g.AWTGraphics.fillPolygon(fillScreenX,fillScreenY, fillVector2Ds.size)
    }
    //show the snappoints
    if(snapPoint.isDefined) snapPoint.foreach(p => drawFill(snapBox(p), new Color(0.10f, 0.95f, 0.95f, 0.40f),t))
  }

}
