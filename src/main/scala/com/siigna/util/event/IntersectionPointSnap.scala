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

import com.siigna.app.model.shape._
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import com.siigna.app.view.{Graphics, View}
import com.siigna.app.Siigna
import java.awt.Color
import com.siigna.app.model.{Model, Drawing}

/**
 * A hook for parsing points that snaps to intersections between objects.
 */
case object IntersectionPointSnap extends EventSnap {

  val colorAttr = "Color" -> new Color(150, 150, 150, 150)
  var snapPoint : Option[Vector2D] = None
  def snapCross (p : Vector2D) : List[LineShape] = {
    val a = 0.8 * Siigna.selectionDistance
    val line1 = LineShape(Vector2D(p.x-a,p.y-a),Vector2D(p.x+a,p.y+a))
    val line2 = LineShape(Vector2D(p.x+a,p.y-a),Vector2D(p.x-a,p.y+a))
    List(line1,line2)
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

    def intersections(s : Shape) : Option[Vector2D] = {
      val evalDist = (s.geometry.boundary.topLeft - point).length
      //get all shapes which potentially intersect with the current nearest shape
      val shapes = Drawing(point,evalDist)
      var l : Option[Vector2D] = None
      if(!shapes.isEmpty) {
        //add potential intersections to a list
        shapes.foreach(e => {
          l = if(!e._2.geometry.intersections(s.geometry).isEmpty) Some(e._2.geometry.intersections(s.geometry).head) else None
          //ints ++ l does not work??
        })
      }
      l //return
    }

    if (!model.isEmpty) {
      val res = model.map(_ match {

        //case s : ArcShape       => s.geometry.midPoint
        //case s : CircleShape    => s.geometry.center
        case s : LineShape      => intersections(s)
        //case s : PolylineShape  => closestPoints(s.shapes.map(_.geometry.center))
        //case s : RectangleShape => closestPoints(s.geometry.segments.map(_.center))
        //case s : TextShape      => closestPoints(s.geometry.segments.map(_.center))
        case _ => None
      })

      if (res.head.isDefined && res.head.get.distanceTo(point) < Siigna.selectionDistance) {
        //the snapPoint variable is set, so that it can be used to draw visual feedback:
        snapPoint = Some(res.head.get)
        //RETURN: the snapped (moved) point, transformed back to the drawing coordinates is returned:
        res.head.get.transform(View.drawingTransformation)
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
    //show the snappoints
    if(snapPoint.isDefined) snapPoint.foreach(p => {
      g.draw(snapCross(p).head.transform(t).addAttributes(colorAttr))
      g.draw(snapCross(p).last.transform(t).addAttributes(colorAttr))
    })
  }
}