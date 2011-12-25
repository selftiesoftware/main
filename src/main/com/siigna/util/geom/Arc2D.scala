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
 * A mathematical representation of a 2-dimensional arc i. e. a circle piece.
 *
 * @param center  The center of the circle representing the arc.
 * @param radius  The radius of the circle representing the arc.
 * @param startAngle  The starting angle of the arc. In degrees between 0 and 360.
 * @param angle The number of degrees the arc is spanning.
 */
case class Arc2D(center : Vector2D, radius : Double, startAngle : Double, angle : Double) extends Arc with GeometryBasic2D {

  type T = Arc2D

  /**
   * The end angle of the arc.
   */
  lazy val endAngle = (startAngle + angle) % 360

  /**
   * The point where the arc ends.
   */
  private lazy val endPoint = Vector(math.cos(endAngle) * radius, math.sin(endAngle) * radius) + center

  /**
   * Calculates the length of the arc.
   */
  lazy val length = radius * radius * math.Pi * (angle / 360.0)

  /**
   * The point where the arc starts.
   */
  private lazy val startPoint = Vector(math.cos(startAngle) * radius, math.sin(startAngle) * radius) + center

  /**
   * Calculates the boundary of the arc.
   */
  def boundary = {
    // First we find the angles in radians for use later on
    val startAngleCos = math.cos(math.toRadians(startAngle))
    val startAngleSin = math.sin(math.toRadians(startAngle))
    val endAngleCos = math.cos(math.toRadians(endAngle))
    val endAngleSin = math.sin(math.toRadians(endAngle))
    // Then we discover which angle have the least and the largest bounds and then we multiply that to the radius and
    // finally add the value to the center point to get two coordinates we can parse as a rectangle.
    val minX = math.min(startAngleCos, endAngleCos)
    val maxX = math.max(startAngleCos, endAngleCos)
    val minY = math.min(startAngleSin, endAngleSin)
    val maxY = math.max(startAngleSin, endAngleSin)
    Rectangle(minX, minY, maxX, maxY)
  }

  def closestPoint(vector : Vector2D) = (vector - center).unit * radius

  def distanceTo(arc : Arc2D) = java.lang.Double.POSITIVE_INFINITY
  def distanceTo(circle : Circle2D) = java.lang.Double.POSITIVE_INFINITY
  def distanceTo(ellipse : Ellipse2D) = java.lang.Double.POSITIVE_INFINITY
  def distanceTo(line : Line2D) = java.lang.Double.POSITIVE_INFINITY
  def distanceTo(rectangle : Rectangle2D) = java.lang.Double.POSITIVE_INFINITY
  def distanceTo(segment : Segment2D) = java.lang.Double.POSITIVE_INFINITY

  def distanceTo(p : Vector2D) =
  {
    // The angle from 0 degrees (3 o'clock) to the point
    val angleToPoint = (p - center).angle

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
      (p - endPoint).length

    // Else the point must be closest to startAngle
    else
      (p - startPoint).length
  }

  /**
   * Determines whether a given angle is included in the arc's periphery.
   *
   * @param angle  The angle to examine in degrees.
   */
  def insideArcDegrees(angle : Double) : Boolean = {
    def bindTo360(a : Double) = if (a < 0) a + 360 else if (a > 360) a - 360 else a
    bindTo360(angle - startAngle) <= this.angle
  }

  def intersects(arc : Arc2D) = false
  def intersects(circle : Circle2D) = false
  def intersects(ellipse : Ellipse2D) = false
  def intersects(line : Line2D) = false
  def intersects(rectangle : Rectangle2D) = false
  def intersects(segment : Segment2D) = false
  def intersects(vector : Vector2D) = false

  /**
   * Returns a list of points, defined as the intersection(s) between the
   * arc and another arc.
   */
  def intersections(arc : Arc2D) : Set[Vector2D] = {
    val circleIntersections = Circle(center, radius).intersections(Circle(arc.center, arc.radius))
    if (circleIntersections.isEmpty) {
      Set() // No intersections
    } else { // Check whether both points are inside the respective arcs
      circleIntersections.filter(p => {
        val angle1 = (p - center).angle
        val angle2 = (p - arc.center).angle
        (insideArcDegrees(angle1) && arc.insideArcDegrees(angle2))
      })
    }
  }

  def intersections(circle : Circle2D) = Set.empty
  def intersections(ellipse : Ellipse2D) = Set.empty
  def intersections(line : Line2D) = Set.empty
  def intersections(rectangle : Rectangle2D) = Set.empty
  def intersections(segment : Segment2D) = Set.empty
  def intersections(vector : Vector2D) = Set.empty

  def transform(t : TransformationMatrix) =
    new Arc2D(t.transform(center), radius * t.scaleFactor, startAngle, angle)

  // TODO: Add middlepoint
  lazy val vertices = Set(startPoint, endPoint)
}

/**
 * The companion object for Arc2D.
 */
object Arc2D {

  import java.lang.Double.NaN

  /**
   * Creates an arc from three points.
   */
  def apply(start : Vector2D, middle : Vector2D, end : Vector2D) : Arc2D = {
    // TODO: Write this
    empty
  }

  /**
   * Creates an empty arc.
   */
  def empty() = {
    new Arc2D(Vector2D.empty, NaN, NaN, NaN)
  }

}
