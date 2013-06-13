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

package com.siigna.util.geom

/**
 * The mathematical class for a line segment, defined as a line with a start
 * point (p1) and a end point (p2). The segment has a finite length.
 */
trait Segment

/**
 * The companion object to the Segment trait.
 */
object Segment {

  /**
   * Create a 2D segment.
   */
  def apply(p1 : Vector2D, p2 : Vector2D) = new Segment2D(p1, p2)

}

/**
 * The mathematical class for a line segment, defined as a line with a start
 * point (p1) and a end point (p2). The segment has a finite length.
 */
@SerialVersionUID(-852679043)
case class Segment2D(p1 : Vector2D, p2 : Vector2D) extends GeometryBasic2D with Segment {

  type T = Segment2D

  /**
   * This function determines whether 0 <= n/N <= 1 without using a
   * division operation (they're slow). This table sums up the calculation:
   *     n   N   n/N   Comment
   *     +   +    +    0 <= n/N <= 1  <=>  0 <= n <= N  (multiply by N)
   *     -   -    +    0 <= n/N <= 1  <=>  0 >= n >= N  (multiply by N)
   *     -   +    -    n/N < 0, always false
   *     +   -    -    n/N < 0, always false
   */
  private def between0And1(n : Double, N : Double) = {
    def sign(n : Double) = if (n >= 0) 1 else -1  // The sign of n (- or +).
    if (sign(n) == sign(N)) {
      if (sign(N) == 1)
        (0 <= n && n <= N)
      else
        (0 >= n && n >= N)
    } else false
  }

  def boundary = Rectangle2D(p1, p2)

  def distanceTo(geom  : Geometry2D) = geom match {
    case point : Vector2D => (point - closestPoint(point)).length
    case g => throw new UnsupportedOperationException("Segment: distanceTo not yet implemented with " + g)
  }

  /**
   * Calculates the closest point from a point to this line.
   */
  def closestPoint(point : Vector2D) = {
    // The point can be either 'outside' start or end. Therefore we have to
    // defines vectors for the two situations:
    val p1ToP2    = p2 - p1
    val pointToP1 = point - p1
    val p2ToP1    = p1 - p2
    val pointToP2 = point - p2

    // .. And calculate the length of the projections on the line
    // - derived from the formula |a||b|*cos(v) = a*b
    val projectionLength1 = p1ToP2 * pointToP1 / p1ToP2.length
    val projectionLength2 = p2ToP1 * pointToP2 / p2ToP1.length

    // If the length of the first projection is negative, the mouse's out of
    // bounds, which is why the closes point is first point.
    if (projectionLength1 <= 0)
      p1

    // If the length of the second projection is negative, we use the second
    // point.
    else if (projectionLength2 <= 0)
      p2

    // Otherwise the point is 'inside' the line-segment, and we calculate the
    // closest point by making a line from p1 to point and subtract it's
    // projection from p1.
    else {
      // Calculate the projection on the line.
      val projection = p2ToP1 * ((pointToP1 * p1ToP2) / (p2ToP1.length * p2ToP1.length))

      // Subtract the projection from p1 and return it
      p1 - projection
    }
  }

  /**
   * Determines whether this segment intersects with a given geometry.
   */
  def intersects(geom : Geometry2D) = geom match {

    case arc : Arc2D => arc.intersects(this)

    case circle : Circle2D => circle.intersects(this)
    case collection : CollectionGeometry2D => collection.intersects(this)

    /**
     * Determines whether two line segments intersect.
     */
    case line : Segment2D => {
      // The line segments are defined by the equations:
      //   L1 = A*u + C
      //   L2 = B*v + D
      val A = p2 - p1
      val B = line.p2 - line.p1
      val C = p1
      val D = line.p1
      // To find the intersection, set the two above equations equal:
      //   L1 = L2  <=>  A*u + C = B*v + D
      // The intersection is defined by solving (u,v) in the linear system:
      //   A*u - B*v = D - C
      // Calculate the determinant:
      //   det = | A - B | = | B  A |
      // If det = 0 there are no solutions (the lines are parallel). Now we find
      // the solutions:
      //   u = | (D - C)  -B | / det = | B  (D - C) | / det
      //   v =                         | A  (D - C) | / det
      // The way the segments are defined u and v must be in the range 0...1
      // (both inclusive), else the intersection is "outside" the line segments.
      val det = Vector2D.determinant(B, A)
      val uNotDivided = Vector2D.determinant(B, D - C)
      val vNotDivided = Vector2D.determinant(A, D - C)

      //if both segments are equal, no intersections should be returned
      if(p1 == line.p1 && p2 == line.p2) false
      //if both segments have just one coinciding endpoint, they intersect:
      else if((p1 == line.p1 && p2 != line.p2)||(p2 == line.p2 && p1 != line.p1)||(p2 == line.p1 && p1 != line.p2)||(p1 == line.p2 && p2 != line.p1)) true

      // The determinant must not be 0, and 0 <= u <= 1 and 0 <= v <= 1.
      else (det != 0 && between0And1(uNotDivided, det) && between0And1(vNotDivided, det))
    }
    case r : SimpleRectangle2D => {
      Seq(r.borderTop, r.borderRight, r.borderBottom, r.borderLeft).exists(_.intersects(this))
    }
    case g => throw new UnsupportedOperationException("Segment: intersects not yet implemented with " + g)
  }

  /**
   * Returns a list of points where a the Line Segment intersects with a given geometry.
   */
  def intersections(geom : Geometry2D) : Set[Vector2D] = geom match {

    case arc : Arc2D => arc.intersections(this)
    /**
     * Returns a list of points, defined as the intersection(s) between the
     * segment and the circle.
     */
    case circle : Circle2D => {
      val f           = p2 - p1
      val g           = p1 - circle.center
      val determinant = (f * g) * (f * g) - (f * f) * (g * g - circle.radius * circle.radius)

      val t : Set[Double] =
        if (determinant < 0)
          Set()
        else if (determinant > 0)
          Set((-(f * g) - scala.math.sqrt(determinant)) / (f * f),
            (-(f * g) + scala.math.sqrt(determinant)) / (f * f))
        else
          Set(-(f * g) / (f * f))

      // Filter out the points outside the line-segment.
      t.map(x => f * x + p1).filter( point =>
        ((p2 - p1) * (point - p1) >= 0 &&
          (p1 - p2) * (point - p2) >= 0)
      )
    }

    case collection : CollectionGeometry2D => collection.intersections(this)

    //find the intersection between two line segments
    case segment : Segment2D => {
      val x1 = this.p1.x
      val y1 = this.p1.y
      val x2 = this.p2.x
      val y2 = this.p2.y
      val x3 = segment.p1.x
      val y3 = segment.p1.y
      val x4 = segment.p2.x
      val y4 = segment.p2.y

      val bx = x2 - x1
      val by = y2 - y1
      val dx = x4 - x3
      val dy = y4 - y3
      val dot = bx * dy - by * dx

      if(dot == 0) Set[Vector2D]()
      else {
        val cx = x3 - x1
        val cy = y3 - y1
        val t = (cx * dy - cy * dx) / dot
        val u = (cx * by - cy * bx) / dot

        if((t >= 0 && t <= 1) && (u >= 0 && u <= 1) ) {
          Set(Vector2D(x1+t*bx, y1+t*by))
        }
        else Set[Vector2D]()
      }
    }
    case g => throw new UnsupportedOperationException("Segment: intersections not yet implemented with " + g)
  }

  def transform(t : TransformationMatrix) = new Segment2D(t.transform(p1), t.transform(p2))

  lazy val vertices = Seq(p1, p2)

}

object Segment2D {

  /**
   * Creates a [[com.siigna.util.geom.Segment2D]] from the two coordinates, written in pairs.
   * @param x1  The smallest x-coordinate
   * @param y1  The smallest y-coordinate
   * @param x2  The largest x-coordinate
   * @param y2  The largest y-coordinate
   * @return  An instance of a [[com.siigna.util.geom.Segment2D]]
   */
  def apply(x1 : Double, y1 : Double, x2 : Double, y2 : Double) =
    new Segment2D(Vector2D(x1, y1), Vector2D(x2, y2))

  /**
   * Transforms a list of points into a list of lines, that doesn't
   * close the whole figure... Example:
   * <pre>
   * *    *        *----*
   *           ==> |    |
   * *    *        *    *
   * </pre>
   *
   * @return List[Segment] A list of lineSegments
   */
  def segmentsOnOpenPathOfPoints(points : Seq[Vector2D]) : Seq[Segment2D] =
  {
    points match {
      case Nil => Nil
      case point :: Nil => Nil
      case point1 :: point2 :: tail =>
        Seq(Segment(point1, point2)) ++ segmentsOnOpenPathOfPoints(point2 :: tail)
    }
  }

  /**
   * Transforms a list of points into a list of lines, that
   * closes the whole figure... Example:
   * <pre>
   * *    *        *----*
   *           ==> |    |
   * *    *        *----*
   * </pre>
   * @param points  A list of vectors
   * @return List[Segment]  A list of lineSegments
   */
  def segmentsOnClosedPathOfPoints(points : Seq[Vector2D]) : Seq[Segment2D] =
  {
    points match {
      case Nil => Nil
      case list => segmentsOnOpenPathOfPoints(list ++ List(list.head))
    }
  }

}

