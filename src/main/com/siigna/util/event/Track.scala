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

package com.siigna.util.event

import com.siigna.app.view.{View, Graphics}
import com.siigna.util.geom.{Line2D, Vector2D, TransformationMatrix}
import com.siigna.util.collection.Attributes
import com.siigna.app.model.Drawing
import com.siigna._
import app.model.shape.LineShape
import app.model.shape.Shape
import scala.Some

object Track extends EventTrack {

  //evaluate if the shape exists (used to clear the track points if the shape is deleted):
  var activeShape : Map[Int, Shape] = Map()

  /**
   * A flag to toggle track on or off.
   */  
  var trackEnabled : Boolean = true

  //A flag to see, if horizontal or vertical guides are active:
  var horizontalGuideActive: Boolean = false
  var verticalGuideActive: Boolean = false
  var trackedPoint: Option[Vector2D] = None
  
  // Get the track color
  protected val color = "Color" -> Siigna.color("trackGuideColor").getOrElse("#00FFFF".color)

  // Define the attributes of the track lines
  protected val attributes = Attributes("Infinite" -> true, color)

  // Get method
  var isTracking = false
  
  // The up-to-date mouse position
  protected var mousePosition = View.mousePosition
    
  // Get the track distance
  val trackDistance = Siigna.double("trackDistance").getOrElse(9.0)

  // Code to get the horizontal guide from a point
  def horizontalGuide(p : Vector2D) : Line2D = Line2D(p, Vector2D(p.x + 1, p.y))

  // Code to get the vertical guide from a point
  def verticalGuide(p : Vector2D) : Line2D = Line2D(p, Vector2D(p.x, p.y + 1))

  // Points to track from
  var pointOne : Option[Vector2D] = None
  var pointTwo : Option[Vector2D] = None


  /**
   * Find a point from a distance, assuming there's a track active.  
   * @param dist The distance to go in the line of the track.
   * @return  Some[Vector2D] if track is active, otherwise None.
   */
  def getPointFromDistance(dist : Double) : Option[Vector2D] = {

    /** Get the best fitting line (horizontal or vertical)
     * @return A line and a boolean indicating if the line is horizonal (false) or vertical (true)
     */ 
    def getTrackPoint(p : Vector2D) : Vector2D = {
      val horiz = horizontalGuide(p)
      val vert  = verticalGuide(p)

      // Horizontal!
      if (horiz.distanceTo(mousePosition) < vert.distanceTo(mousePosition)) {
        val closestPoint = horiz.closestPoint(mousePosition)
        if (closestPoint.x < p.x) Vector2D(p.x - dist, p.y)
        else                      Vector2D(p.x + dist, p.y)
      // Vertical!
      } else {
        val closestPoint = vert.closestPoint(mousePosition)
        if (closestPoint.y < p.y) Vector2D(p.x, p.y - dist)
        else                      Vector2D(p.x, p.y + dist)
      } 
    }
    
    if (pointOne.isDefined) {
      Some(getTrackPoint(pointOne.get))
    } else None
  }

  // Track on the basis of a maximum of two tracking points.
  def parse(events : List[Event], model : Map[Int, Shape]) : Event = {

    if(trackEnabled) {
      // Set isTracking to false
      //isTracking = false

      // Get mouse event
      // The events has been unchecked since this match cannot occur if the event-list is empty
      val (x : Vector2D, eventFunction : (Vector2D => Event)) = (events : @unchecked) match {
        case MouseEnter(p, a, b) :: tail => (p, (v : Vector2D) => MouseEnter(v, a, b))
        case MouseExit (p, a, b) :: tail => (p, (v : Vector2D) => MouseExit(v, a, b))
        case MouseMove (p, a, b) :: tail => (p, (v : Vector2D) => MouseMove(v, a, b))
        case MouseDrag (p, a, b) :: tail => (p, (v : Vector2D) => MouseDrag(v, a, b))
        case MouseDown (p, a, b) :: tail => (p, (v : Vector2D) => MouseDown(v, a, b))
        case MouseUp   (p, a, b) :: tail => (p, (v : Vector2D) => MouseUp  (v, a, b))
        case e :: tail => (this.mousePosition, (v : Vector2D) => e)
      }

      // Update mousePosition
      val m = x.transform(View.deviceTransformation)
      this.mousePosition = m
      var shape : Option[Int] = None

      // Get the nearest shape if it is defined
      val model = Drawing(m)

      //if a shape is in the process of being made (not in the model yet), use it too.

      if (model.size > 0) {
        //if a tracking point is defined, and the mouse is placed on top of a second point
        if (pointOne.isDefined) {

          val nearest = model.reduceLeft((a, b) => if (a._2.geometry.distanceTo(m) < b._2.geometry.distanceTo(m)) a else b)
          shape = Some(nearest._1)
          val nearestPoint = nearest._2.geometry.vertices.reduceLeft((a : Vector2D, b : Vector2D) => if (a.distanceTo(m) < b.distanceTo(m)) a else b)
          if (nearestPoint.distanceTo(m) < trackDistance) {
            if  (!(pointOne.get.distanceTo(m) < 10)) pointTwo = pointOne
            pointOne = Some(nearestPoint)
          }
        }
        //if no tracking point is defined, set the first point.
        else {
          val nearest = model.reduceLeft((a, b) => if (a._2.geometry.distanceTo(m) < b._2.geometry.distanceTo(m)) a else b)
          shape = Some(nearest._1)
          val nearestPoint = nearest._2.geometry.vertices.reduceLeft((a : Vector2D, b : Vector2D) => if (a.distanceTo(m) < b.distanceTo(m)) a else b)
          pointOne = if (nearestPoint.distanceTo(m) < trackDistance) Some(nearestPoint) else None
        }
      }

      //evaluate if the shape exists (used to clear the track points if the shape is deleted:
      if(pointOne.isDefined) {
        activeShape = Drawing(pointOne.get,1)
        if(activeShape == Map()) {
          pointOne = None
          if(pointTwo.isDefined) pointTwo = None
        }
      }

      //Snap the event
      val mousePosition = (pointOne :: pointTwo :: Nil).foldLeft(m)((p : Vector2D, opt : Option[Vector2D]) => {
        opt match {
          case Some(snapPoint : Vector2D) => {
            val horizontal = horizontalGuide(snapPoint)
            val vertical = verticalGuide(snapPoint)
            val distHori = horizontal.distanceTo(p)
            val distVert = vertical.distanceTo(p)

            if (distHori <= distVert && distHori < trackDistance) {
              //print("distHori <= distVert")
              //print("distHori < trackDistance")
              isTracking = true
              horizontal.closestPoint(p)
            } else if (distVert < distHori && distVert < trackDistance) {
              isTracking = true
              vertical.closestPoint(p)
            } else {
              p
            }
          }
          case None => p
        }
      })

      // Return snapped coordinate
      eventFunction(mousePosition.transform(View.drawingTransformation))
    } else events.head
  }




  override def paint(g : Graphics, t : TransformationMatrix) {

    println("T: "+isTracking)


    def paintPoint(p : Vector2D) {
      val horizontal = horizontalGuide(p)
      val vertical   = verticalGuide(p)

      //draw the vertical tracking guide
      if (vertical.distanceTo(mousePosition) < trackDistance && verticalGuideActive == false ) {
        g draw LineShape(vertical.p1, vertical.p2, attributes).transform(t)
        verticalGuideActive = true
        trackedPoint = Some(p)
      } else {
        verticalGuideActive = false
      }

      //draw the horizontal tracking guide
      if (horizontal.distanceTo(mousePosition) < trackDistance && horizontalGuideActive == false ) {
        g draw LineShape(horizontal.p1, horizontal.p2, attributes).transform(t)
        horizontalGuideActive = true
        trackedPoint = Some(p)
      } else {
        horizontalGuideActive = false
      }

      if (horizontalGuideActive == false && verticalGuideActive == false) trackedPoint = None
    }
    
    //PAINT TRACKING POINT ONE
    if (pointOne.isDefined) paintPoint(pointOne.get)

    //PAINT TRACKING POINT TWO
    if (pointTwo.isDefined) paintPoint(pointTwo.get)
  }

}
