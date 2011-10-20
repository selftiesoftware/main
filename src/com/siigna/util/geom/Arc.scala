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
package com.siigna.util.geom

/**
 * A mathematical representation of an arc from three vectors, representing
 * any three points on the circumference of a circle - start, middle and end.
 * 
 * <b>Important:</b> Assumes that (start != middle != end).
 */
case class Arc(start : Vector, middle : Vector, end : Vector) extends BasicGeometry {
  
  // Test that (start != middle != end)
  require(start != middle, "Arc: Failed to create arc - start and middle vectors are equal.")
  require(middle != end, "Arc: Failed to create arc - middle and end vectors are equal.")
  require(start != end, "Arc: Failed to create arc - start and end vectors are equal.")

  /**
   * A value used to signal arcs defined counter-clockwise.
   */
  val cw, CW, clockwise = 0
  
  /**
   * A value used to signal arcs defined clockwise.
   */
  val ccw, CCW, counterclockwise = 1

  /**
   * Calculates the entire angle of the circle section.
   */
  lazy val angle = circleDegrees((endAngle - startAngle) % 360)

  /**
   * Calculates the center-coordinate, by producing two normal-vectors from the
   * three points (middle to start and middle to end). The center is found by
   * calculating the intersection between these two lines.
   */
  lazy val center = {
    val m1 = (middle + start)/2
    val m2 = (end + middle)/2
    val p1 = m1 + (middle - start).normal
    val p2 = m2 + (end - middle).normal

    // Get a list of intersections and retrieve the first coordinate of the list.
    // If there is no intersection (the three points are on a line) there can
    // not be found any center, and the function returns null.
    Line(m1, p1).intersects(Line(m2, p2))(0)
  }

  /**
   * Determines the direction the arc is drawn (clockwise or counter-clockwise).
   */
  lazy val direction = {
    // If the the angle from start to middle is smaller than the angle from
    // start to end, when translated to the start vector, then calculate the
    // arc from the endAngle to the startAngle (counter-clockwise). Otherwise
    // the arc is drawn clockwise.
    // TODO: Also swap the start/end-point?!
    val startToMiddleAngle = circleDegrees((middle - start).angle - (start - center).angle)
    val startToEndAngle    = circleDegrees((end - start).angle - (start - center).angle)
    if (startToMiddleAngle < startToEndAngle)
      CCW
    else
      CW
  }
  
  /**
   * Calculates the end angle. The end angle is always calculated so the
   * arc is drawn counter-clockwise (ccw). Firstly this is done because there's
   * a common mathematical convention that every circle is drawn ccw and
   * secondly because java's graphical engine follows this convention.
   */
  lazy val endAngle   = {
    if (direction == counterclockwise) (end - center) angle
    else (start - center) angle
  }

  def handles = List(start, middle, end)

  /**
   * Calculates the entire angle of the circle section.
   */
  lazy val length = angle/180 * scala.math.Pi * radius

  /**
   * Calculates the radius.
   */
  lazy val radius = (start - center) length

  /**
   * Calculates the start angle. The start angle is always calculated so the
   * arc is drawn counter-clockwise (ccw). Firstly this is done because there's
   * a common mathematical convention that every circle is drawn ccw and
   * secondly because java's graphical engine follows this convention.
   */
  lazy val startAngle = {
    if (direction == counterclockwise) (start - center) angle
    else (end - center) angle
  }

  /**
   * TODO: Det' en ommer! In english: Redo this!
   */
  def boundary = {
    Circle(center, start).boundary
  }

  /**
   * Rounds a given degree to a number between 0 and 360.
   */
  private def circleDegrees(degrees : Double) = {
      if (degrees > 360) degrees - 360
      else if (degrees < 0) degrees + 360
      else degrees
    }

  /**
   * Calculates the distance from the arc to a given point.
   * TODO: Refactor to distanceTo(point : Vector)
   */
  def distanceTo(p : Vector) =
  {
    // The angle from 0 degrees (3 o'clock) to the point
    val angleToPoint = (p - center) angle

    // If the angle is lesser than the start and greater than the end angle
    // it must be 'inside' the arc, and the shortest distance must then be
    // the distance to the circle-section.
    if (angleToPoint >= startAngle && angleToPoint <= endAngle)
      scala.math.abs(radius - (center - p).length)

    // If the arc goes through 0 degrees (i. e. 360 degrees so the angle of
    // the start-point is lesser then the start-point), then we must test for
    // a case where the point can be 'inside' the circle-section when the
    // angle from the point to the X-axis is greater than the endAngle
    // or lesser than the startAngle.
    else if (endAngle < startAngle && (angleToPoint > startAngle || angleToPoint < endAngle))
        scala.math.abs(radius - (center - p).length)

    // If the anglePoint is closest to endAngle
    else if (angleToPoint - endAngle <= angleToPoint - startAngle)
        (p - end).length

    // Else the point must be closest to startAngle
    else
        (p - start).length
  }

  /**
   * Determines whether a given angle is included in the arc's periphery.
   *
   * @param angle  The angle to examine in degrees.
   * TODO: Why is the angle not drawn counter-clockwise? Should be:
   *  ((direction == CCW && circleDegrees(angle - startAngle) <= circleDegrees(endAngle - startAngle)) ||
        direction == CW && circleDegrees(angle - startAngle) >= circleDegrees(endAngle - startAngle))
   */
  def insideArcDegrees(angle : Double) : Boolean =
    if (angle == startAngle || angle == endAngle)
      true
    else
      (circleDegrees(angle - startAngle) <= circleDegrees(endAngle - startAngle))

  /**
   * Examines whether a segment and this arc intersects.
   */
  def intersect(segment : Segment) =
    segment.intersect(this)

  /**
   * Determines whether the arc and a given rectangle intersects.
   */
  def intersect(rect : Rectangle) : Boolean =
  {
    // Check that there exist at least one rectangle segment that intersects
    // with the given line.
    Segment.segmentsOnClosedPathOfPoints(rect.points) map(
      rectangleSegment => intersect(rectangleSegment)
    ) reduceLeft( _ || _ )
  }

  /**
   * Returns a list of points, defined as the intersection(s) between the
   * arc and another arc.
   */
  def intersects(arc : Arc) : Seq[Vector] = {
    val circleIntersections = Circle(center, start).intersects(Circle(arc.center, arc.start))
    if (circleIntersections.isEmpty) {
      Seq() // No intersections
    } else { // Check whether both points are inside the respective arcs
      circleIntersections.filter(p => {
        val angle1 = (p - center).angle
        val angle2 = (p - arc.center).angle
        (insideArcDegrees(angle1) && arc.insideArcDegrees(angle2))
      })
    }
  }

  /**
   * Returns a list of points, defined as the intersection(s) between the
   * arc and another segment.
   */
  def intersects(segment : Segment) : List[Vector] =
    segment.intersects(this)

}
