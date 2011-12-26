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
 * A circle. Represented by a center and a radius.
 */
case class Circle2D(center : Vector2D, radius : Double) extends GeometryEnclosed2D {

  import java.lang.Double.POSITIVE_INFINITY
  
  type T = Circle2D

  def area = radius * radius * math.Pi

  def boundary = Rectangle(Vector(center.x - radius, center.y - radius), Vector(center.x + radius, center.y + radius))

  def contains(arc : Arc2D) = false
  def contains(circle : Circle2D) = (radius >= circle.radius && (center - circle.center).length < (radius - circle.radius).abs)
  def contains(ellipse : Ellipse2D) = false
  def contains(line : Line2D) = false
  def contains(rectangle : Rectangle2D) = false
  def contains(segment : Segment2D) = false
  def contains(point : Vector2D) = (point - center).length < radius

  def closestPoint(point : Vector2D) = (center - point).unit * radius

  def distanceTo(arc : Arc2D) = POSITIVE_INFINITY
  def distanceTo(circle : Circle2D) = POSITIVE_INFINITY
  def distanceTo(ellipse : Ellipse2D) = POSITIVE_INFINITY
  def distanceTo(line : Line2D) = POSITIVE_INFINITY
  def distanceTo(rectangle : Rectangle2D) = POSITIVE_INFINITY
  def distanceTo(segment : Segment2D) = POSITIVE_INFINITY

  /**
   * Calculates the length between the circles radius and a point.
   */
  def distanceTo(point : Vector2D) = scala.math.abs(radius - (center - point).length)

  def intersects(arc : Arc2D) = false

  /**
   * Examines whether a circle and another circle intersects.
   *
   * Source: http://local.wasp.uwa.edu.au/~pbourke/geometry/2circle/
   */
  def intersects(circle : Circle2D) = {
    val d = (center - circle.center).length
    if (d > radius + circle.radius) { // If d > r0 + r1 then there are no solutions, the circles are separate.
      false
    } else if (d < radius - circle.radius) { // If d < |r0 - r1| then there are no solutions because one circle is contained within the other.
      false
    } else if (d == 0 && radius == circle.radius) { // If d = 0 and r0 = r1 then the circles are coincident and there are an infinite number of solutions.
      false
    } else { // Otherwise we have a hit!
      true
    }
  }

  def intersects(ellipse : Ellipse2D) = false
  def intersects(line : Line2D) = false

  /**
   * Examines whether a circle and a rectangle intersects.
   */
  def intersects(rect : Rectangle2D) = {
    val max = rect.topRight - center
    val min = rect.bottomLeft - center
    if (max.x < 0) { // Rectangle to the left
      if (max.y < 0) {
        (max.x * max.x + max.y * max.y) < radius * radius
      } else if (min.y > 0) {
        (max.x * max.x + min.y * min.y) < radius * radius
      } else {
        scala.math.abs(max.x) < radius
      }
    } else if (min.x > 0) { // Rectangle to the right
      if (max.y < 0) {
        (min.x * min.x + max.y * max.y) < radius * radius
      } else if (min.y > 0) {
        (min.x * min.x + min.y * min.y) < radius * radius
      } else {
        min.x < radius
      }
    } else { // Rectangle on circle vertical centerline
      if (max.y < 0) { // Due South
        scala.math.abs(max.y) < radius
      } else if (min.y > 0) { // Due North
        min.y < radius
      } else true
    }
  }

  def intersects(segment : Segment2D) = false

  def intersections(arc : Arc2D) = Set()
  def intersections(circle : Circle2D) : Set[Vector2D] = if (intersects(circle)) {
      // Calculate various constants
      val d = (center - circle.center).length
      val a = (radius * radius - circle.radius * circle.radius + d * d) / (2 * d) //a = (r02 - r12 + d2 ) / (2 d)
      val h = math.sqrt(radius * radius - a * a) // h^2 = r0^2 - a^2
      val p = center + ((circle.center - center) * a) / d // P0 + a ( P1 - P0 ) / d

      // If the diameter equals the two radii then there's only one solution (== p).
      if (d == radius + circle.radius) {
        Set(p) // Return
      } else { // Otherwise calculate the two solutions.
        // Set the value for calculating the coordinates.
        val xConst = h * (circle.center.y - center.y) / d
        val yConst = h * (circle.center.x - center.x) / d
        // Set the coordinates
        val firstSolution  = Vector(p.x - xConst, p.y + yConst) // x3 = x2 +- h ( y1 - y0 ) / d
        val secondSolution = Vector(p.x + xConst, p.y - yConst) // y3 = y2 -+ h ( x1 - x0 ) / d
        // Return
        Set(firstSolution, secondSolution)
      }
    } else Set()

  def intersections(ellipse : Ellipse2D) = Set()
  def intersections(line : Line2D) = Set()
  def intersections(rectangle : Rectangle2D) = Set()
  def intersections(segment : Segment2D) = Set()

  def transform(t : TransformationMatrix) = new Circle2D(t.transform(center), radius * t.scaleFactor)

  /**
   * Displays a set of vectors as the center along with
   * four vectors which is set on the East, South, West and North side of the circumference.
   */
  lazy val vertices = Set(center, center.changeX(radius), center.changeY(-radius), center.changeX(-radius), center.changeY(radius))
  
}

object Circle2D {

  /**
   * Creates a 2D circle.
   */
  def apply(center : Vector2D, point : Vector2D) = new Circle2D(center, (point - center).length)

}