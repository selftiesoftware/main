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

import com.siigna.util.geom._
import com.siigna.app.model.shape._
import com.siigna.app.view.{Graphics, View}
import com.siigna.app.Siigna
import scala.collection.mutable.ArrayBuffer
import java.awt.Color

/**
 * A hook for parsing points that snap to mid-points on objects.
 */
case object MidPointSnap extends EventSnap {

  var closestMid : Option[Vector2D] = None
  var snapPoint : Option[Vector2D] = None

  def snapBox (p : Vector2D) = {
    val a = 0.5 * Siigna.selectionDistance
    Array(Vector2D(p.x-a,p.y-a),Vector2D(p.x,p.y+a),Vector2D(p.x+a,p.y-a),Vector2D(p.x-a,p.y-a))
  }
  def parse(event : Event, model : Traversable[Shape]) = event match {
    case MouseDown(point, a, b)  => MouseDown(snap(point, model), a, b)
    case MouseDrag(point, a, b)  => MouseDrag(snap(point, model), a, b)
    case MouseMove(point, a, b)  => MouseMove(snap(point, model), a, b)
    case MouseUp(point, a, b)    => MouseUp(snap(point, model), a, b)
    case some => some
  }

  /**
   *
   * @param q the point entering Snap is in DeviceCoordinates. (See View.deviceTransformation for explanation)
   *          it is changed to drawingCoordinates in order to be able to evaluate its position
   *          relative to shapes in the drawing
   * @param model If snap is in range, the mouse is moved to to sit on top of the closest end point
   * @return the new mouse point,in deviceCoordinates.

   */
  def snap(q : Vector2D, model : Traversable[Shape]) : Vector2D = {

    //the point is transformed to match the coordinate system of the drawing
    val point = q.transform(View.deviceTransformation)

    def closestTwo(p1 : Vector2D, p2 : Vector2D) = if (p1.distanceTo(point) < p2.distanceTo(point)) p1 else p2
    def closestPoints(points : Seq[Vector2D]) = points.reduceLeft(closestTwo)

    if (!model.isEmpty) {
      val res = model.map(_ match {

        case s : ArcShape       => s.geometry.midPoint
        case s : CircleShape    => s.geometry.center
        case s : LineShape      => s.geometry.center
        case s : PolylineShape  => closestPoints(s.shapes.map(_.geometry.center))
        case s : RectangleShape => closestPoints(s.geometry.segments.map(_.center))
        case s : TextShape      => closestPoints(s.geometry.segments.map(_.center))
        case _ => point
      })
      val closestPoint = res.reduceLeft(closestTwo)
      //update a var used in Track
      closestMid = Some(closestPoint)

      if (closestPoint.distanceTo(point) < Siigna.selectionDistance) {
        //the snapPoint variable is set, so that it can be used to draw visual feedback:
        snapPoint = Some(closestPoint)
        //RETURN: the snapped (moved) point, transformed back to the drawing coordinates is returned:
        closestPoint.transform(View.drawingTransformation)
      } else {
        //no snap in range, return the point

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
    if(snapPoint.isDefined) {
      snapPoint.foreach(p => drawFill(snapBox(p), new Color(0.10f, 0.95f, 0.95f, 0.40f),t))
    }
  }
}