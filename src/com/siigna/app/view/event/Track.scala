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
import com.siigna.app.view.Graphics
import com.siigna.util.collection.Attributes
import com.siigna.util.geom.{Line, TransformationMatrix, Vector}

object Track extends EventTrack {

  var isTracking = true
  var line : Option[LineShape] = None

  def parse(events : List[Event], model : Iterable[ImmutableShape]) = {

    // Filter out lines from the model
    val lines : Iterable[LineShape] = model.filter(_.isInstanceOf[LineShape]).map(_.asInstanceOf[LineShape])

    // Get the coordinates of the last 5 events
    val lastEvents = events.slice(0,5)
    val moveEvents : List[MouseEvent] = lastEvents.filter(_.isInstanceOf[MouseMove]).map(_.asInstanceOf[MouseMove])
    val coordinates = moveEvents.map(_.position)

    if (!lines.isEmpty && !coordinates.isEmpty) {
      line = Some(lines.reduceLeft((a, b) => if (a.geometry.distanceTo(coordinates.head) <= b.geometry.distanceTo(coordinates.head)) a else b))
      val newCoordinate = Line(line.get.p1, line.get.p2).closestPoint(coordinates(0))
      MouseMove(newCoordinate, moveEvents(0).button, moveEvents(0).keys)  // Return a snapped coordinate
    } else {
      line = None
      events.head // Return the same coordinate
    }
  }

  override def paint(graphics : Graphics, transformation : TransformationMatrix) {
    if (line.isDefined) {
      val transformedLine = line.get.transform(transformation).attributes_+=("infinite" -> true)
      graphics draw transformedLine
    }
  }

}
