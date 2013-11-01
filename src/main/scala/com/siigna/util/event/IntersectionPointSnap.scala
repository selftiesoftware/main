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

  val colorAttr = "Color" -> new Color(0.10f, 0.95f, 0.95f, 0.40f)
  var snapPoint : Option[Vector2D] = None
  def snapCircle (p : Vector2D) = {
    val r = 0.6 * Siigna.selectionDistance
    CircleShape(p,r)
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

    //find the intersection closest to the mouse
    def nearestInt (v : List[Vector2D], p : Vector2D) : Vector2D = {
      val list = v.sortBy(_.distanceTo(p))
      list.head //return closest point
    }

    def intersections(s : Shape, p : Vector2D) : Seq[Vector2D] = {
      val evalDist = (s.geometry.boundary.topLeft - point).length
      //get all shapes which potentially intersect with the current nearest shape
      val shapes = Drawing(point,evalDist)
      var ints : List[Vector2D] = List()

      if(!shapes.isEmpty) {
        //add potential intersections to a list
        shapes.foreach(e => {
          val l = if(!e._2.geometry.intersections(s.geometry).isEmpty) Some(e._2.geometry.intersections(s.geometry).head) else None
          if(l.isDefined) ints = ints :+ l.get
        })
      }
      ints.toSeq //return
    }

    if (!model.isEmpty) {

      val res = model.map(_ match {

        case s : ArcShape       => {
          val ints = intersections(s, point)
          if (ints.length > 1) nearestInt(ints.toList, point)
          else if(ints.length == 1) ints.head
          else q
        }
        case s : CircleShape    => {
          val ints = intersections(s, point)
          if (ints.length > 1) nearestInt(ints.toList, point)
          else if(ints.length == 1) ints.head
          else q
        }
        //TODO: redo closestPoint evaluation so that all points in the list are evaluated.
        case s : LineShape        => {
          val ints = intersections(s, point)
          if (ints.length > 1) nearestInt(ints.toList, point)
          else if(ints.length == 1) ints.head
          else q
        }
        //TODO: sometimes vertices are treated as intersections!!
        case s : PolylineShape  => {
          val ints = intersections(s, point)
          if (ints.length > 1) nearestInt(ints.toList, point)
          else if(ints.length == 1) ints.head
          else q
        }

        //TODO: no intersections are found?
        case s : RectangleShape => {
          val ints = intersections(s, point)
          if (ints.length > 1) nearestInt(ints.toList, point)
          else if(ints.length == 1) ints.head
          else q
        }
        case _ => point
      })
      //
      val closestPoint = res.toVector.head

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
    //show the snappoints
    if(snapPoint.isDefined) snapPoint.foreach(p => {
      g.draw(snapCircle(p).transform(t).addAttributes(colorAttr))
    })
  }
}