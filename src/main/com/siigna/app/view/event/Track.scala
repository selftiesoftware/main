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
import com.siigna.app.view.Graphics
import collection.parallel.immutable.{ParMap, ParIterable}
import com.siigna.util.geom.{Line2D, Vector2D, Line, TransformationMatrix}
import com.siigna.util.collection.Attributes
import com.siigna.app.model.Drawing
import com.siigna.app.Siigna
import com.siigna.util.Implicits._

object Track extends EventTrack {

  // Horizontal guides
  protected var horizontalGuide1 : Option[LineShape] = None
  protected var horizontalGuide2 : Option[LineShape] = None

  // Points to track from
  protected var pointOne : Option[Vector2D] = None
  protected var pointTwo : Option[Vector2D] = None

  // Vertical guides
  protected var verticalGuide1 : Option[LineShape] = None
  protected var verticalGuide2 : Option[LineShape] = None

  // Are we tracking?
  protected var isEnabled = false

  def isTracking = isEnabled


  //functions to determine if a vertical or horizontal guide should be drawn:
  protected def horizontalActive(p : Vector2D, m : Vector2D) : Boolean = {
    var guide = Line2D(p, Vector2D(p.x - 1, p.y))
    if (guide.distanceTo(m) < 5) true
    else false
  } 
  
  protected def verticalActive(p : Vector2D, m : Vector2D) : Boolean = {
    var guide = Line2D(p, Vector2D(p.x, p.y-1))
    if (guide.distanceTo(m) < 5) true
    else false
  }

  def parse(events : List[Event], model : Map[Int, Shape]) : Event = { 
    // Get mouse event
    val (m, eventFun) = events match {
      case MouseMove(p, a, b) => (p, MouseMove(_, a, b))
      case MouseDrag(p, a, b) => (p, MouseDrag(_, a, b))
      case e => (Siigna.mousePosition, e)
    }        
    
    //get the nearest shape if it is defined
    if (Drawing(m).size > 0) {
      //println("pt one: "+pointOne)
      //println("pt two: "+pointTwo)

      //if a tracking point is defined, and the mouse is placed on top of a second point
      if (pointOne.isDefined){
        val nearest = Drawing(m).reduceLeft((a, b) => if (a._2.geometry.distanceTo(m) < b._2.geometry.distanceTo(m)) a else b)
        val nearestPoint = nearest._2.geometry.vertices.reduceLeft((a : Vector2D, b : Vector2D) => if (a.distanceTo(m) < b.distanceTo(m)) a else b)
        if (nearestPoint.distanceTo(m) < 2 ){
          if  (!(pointOne.get.distanceTo(m) < 10)) pointTwo = pointOne
          pointOne = Some(nearestPoint)
        }
        horizontalGuide1 = Some(LineShape(Vector2D(pointOne.get.x-10000,pointOne.get.y),Vector2D(pointOne.get.x+10000,pointOne.get.y)))
        verticalGuide1 = Some(LineShape(Vector2D(pointOne.get.x,pointOne.get.y-10000),Vector2D(pointOne.get.x,pointOne.get.y+10000)))
        if(pointTwo.isDefined){
          horizontalGuide2 = Some(LineShape(Vector2D(pointTwo.get.x-10000,pointTwo.get.y),Vector2D(pointTwo.get.x+10000,pointTwo.get.y)))
          verticalGuide2 = Some(LineShape(Vector2D(pointTwo.get.x,pointTwo.get.y-10000),Vector2D(pointTwo.get.x,pointTwo.get.y+10000)))
        }
      }
      //if no tracking point is defined, set the first point.
      else {
        val nearest = Drawing(m).reduceLeft((a, b) => if (a._2.geometry.distanceTo(m) < b._2.geometry.distanceTo(m)) a else b)
        val nearestPoint = nearest._2.geometry.vertices.reduceLeft((a : Vector2D, b : Vector2D) => if (a.distanceTo(m) < b.distanceTo(m)) a else b)
        pointOne = if (nearestPoint.distanceTo(m) < 2) Some(nearestPoint) else None

        if (pointOne.isDefined && verticalActive(pointOne.get,m) == true) {
          horizontalGuide1 = Some(LineShape(Vector2D(pointOne.get.x-10000,pointOne.get.y),Vector2D(pointOne.get.x+10000,pointOne.get.y)))
          verticalGuide1 = Some(LineShape(Vector2D(pointOne.get.x,pointOne.get.y-10000),Vector2D(pointOne.get.x,pointOne.get.y+10000)))
        }
      }
    }
  }

  override def paint(graphics : Graphics, transformation : TransformationMatrix) {   
    //track on the basis of a maximum of two tracking points.
    val color = Siigna.color("trackGuideColor").getOrElse("#00FFFF".color)
    
    //PAINT TRACKING POINT ONE
    if(pointOne.isDefined){
      //draw the vertical tracking guide
      if(verticalGuide1.isDefined && verticalActive (pointOne.get, m) == true) {
        //g draw verticalGuide1.get.setAttributes("Infinite" -> true, trackGuide).transform(t)
        g draw verticalGuide1.get.setAttributes(trackGuide).transform(t)
      }

      //draw the horizontal tracking guide
      if(horizontalGuide1.isDefined && horizontalActive (pointOne.get, m) == true) {
        //g draw verticalGuide.get.setAttributes("Infinite" -> true, trackGuide).transform(t)
        g draw horizontalGuide1.get.setAttributes(trackGuide).transform(t)
      }
    }

    //PAINT TRACKING POINT TWO
    if (pointTwo.isDefined) {
      //draw the vertical tracking guide
      if(verticalGuide2.isDefined && verticalActive (pointTwo.get, m) == true) {
        //g draw verticalGuide2.get.setAttributes("Infinite" -> true, trackGuide).transform(t)
        g draw verticalGuide2.get.setAttributes(trackGuide).transform(t)
      }
  
      //draw the horizontal tracking guide
      if(horizontalGuide2.isDefined && horizontalActive (pointTwo.get, m) == true) {
        //g draw verticalGuide.get.setAttributes("Infinite" -> true, trackGuide).transform(t)
        g draw horizontalGuide2.get.setAttributes(trackGuide).transform(t)
      }
    }
  }

}
