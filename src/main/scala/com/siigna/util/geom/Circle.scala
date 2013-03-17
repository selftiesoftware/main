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
package com.siigna.util.geom

/**
 * A circle. Represented by a center and a point on the circle.
 */
trait Circle {
  
  def radius : Double

}

/**
 * A companion object to Circle.
 */
object Circle {

  /**
   * Creates a 2D circle.
   */
  def apply(center : Vector2D, radius : Double) = new Circle2D(center, radius)

  /**
   * Creates a 2D circle.
   */
  def apply(center : Vector2D, point : Vector2D) = new Circle2D(center, (point - center).length)

}

/**
 * A circle. Represented by a center and a radius.
 */
@SerialVersionUID(62708094)
case class Circle2D(override val center : Vector2D, radius : Double) extends Circle with GeometryClosed2D {

  type T = Circle2D

  def area = radius * radius * math.Pi

  def boundary = Rectangle2D(Vector2D(center.x - radius, center.y - radius), Vector2D(center.x + radius, center.y + radius))

  def contains(arc : Arc2D) = false
  def contains(geometry : Geometry2D) = geometry match {
    case circle : Circle2D => (radius >= circle.radius && (center - circle.center).length < (radius - circle.radius).abs)
    case point : Vector2D => (point - center).length < radius
    case g => throw new UnsupportedOperationException("Circle: Not yet implemented with " + g)
  }

  def closestPoint(point : Vector2D) = (center - point).unit * radius

  /**
   * Calculates the length between the circles radius and a point.
   */
  def distanceTo(geometry : Geometry2D) = geometry match {
    case point : Vector2D => scala.math.abs(radius - (center - point).length)
    case g => throw new UnsupportedOperationException("Circle: Not yet implemented with " + g)
  }

  def intersects(geom : Geometry2D) = geom match {
    /**
     * Examines whether a circle and another circle intersects.
     *
     * Source: http://local.wasp.uwa.edu.au/~pbourke/geometry/2circle/
     */
    case circle : Circle2D => {
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

    /**
     * Examines whether a circle and a rectangle intersects.
     */
    case rect : SimpleRectangle2D => {
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
    case g => throw new UnsupportedOperationException("Circle: Not yet implemented with " + g)
  }

  def intersections(geom : Geometry2D) : Set[Vector2D] = geom match {
    case circle : Circle2D if (intersects(circle)) => {
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
        val firstSolution  = Vector2D(p.x - xConst, p.y + yConst) // x3 = x2 +- h ( y1 - y0 ) / d
        val secondSolution = Vector2D(p.x + xConst, p.y - yConst) // y3 = y2 -+ h ( x1 - x0 ) / d
        // Return
        Set(firstSolution, secondSolution)
      }
    }
    case line : Line2D => line.intersections(this)
    case line : Segment2D => line.intersections(this)
    case g => throw new UnsupportedOperationException("Circle: Not yet implemented with " + g)
  }

  def transform(t : TransformationMatrix) = new Circle2D(t.transform(center), radius * t.scaleFactor)

  /**
   * Displays a set of vectors as the center along with
   * four vectors which is set on the East, South, West and North side of the circumference.
   */
  lazy val vertices = Seq(center, center + Vector2D(radius, 0), center - Vector2D(0, radius), center - Vector2D(radius, 0), center + Vector2D(0, radius))

}

object Circle2D {

  /**
   * Creates a 2D circle.
   */
  def apply(center : Vector2D, point : Vector2D) = new Circle2D(center, (point - center).length)

}