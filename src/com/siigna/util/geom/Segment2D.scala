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
 * The mathematical class for a line segment, defined as a line with a start
 * point (p1) and a end point (p2). The segment has a finite length.
 */
case class Segment2D(p1 : Vector2D, p2 : Vector2D) extends BasicGeometry[Dimension2]
{

  val center = (p1 + p2) / 2

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

  def boundary = Rectangle(p1, p2)

  /**
   * Calculates the distance between a vector and the line segment
   * TODO: Split this up for optimization.
   */
  def distanceTo(point : Vector2D) : Double = (point - closestPointTo(point)).length

  /**
   * Calculates the closest point from a point to this line.
   * TODO: Split the function up, so distanceToPoint uses a more optimized algorithm.
   */
  def closestPoint(point: Vector2D) = {
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

    // Otherwise the point is 'inside' the linesegment, and we calculate the
    // closest point by making a line from p1 to point and subtract it's
    // projection from p1.
    else {
      // Calculate the projection on the line.
      val projection = p2ToP1 * ((pointToP1 * p1ToP2) / (p2ToP1.length * p2ToP1.length))

      // Subtract the projection from p1 and return it
      p1 - projection
    }
  }

  def handles = List(p1, (p1 + p2) / 2, p2)

  /**
   * Determines whether this segment intersects with a given Arc.
   * TODO: Something wrong here..
   */
  def intersects(arc : Arc2D) =
  {
    val circle = Circle(arc.center, arc.start)
    val intersections = intersections(circle)
    if (intersections.size == 0)
      false
    else intersections.map(
      point =>
      {
        // Now we know that the circle that the arc is a part of intersects
        // with the segment. Now all we got to do is examine whether the point
        // is on the arc and the segment at the same time. If it is, we have
        // an intersection! Wee.
        (point - arc.end) * (arc.start - arc.end).normal >= 0 &&
        (p2 - p1) * (point - p1) >= 0 &&
        (p1 - p2) * (point - p2) >= 0
      }
    ).reduceLeft( _ || _ ) // This is NOT breasts!
  }

  /**
   * Determines whether a segment and a circle intersects.
   * TODO: Optimize
   */
  def intersect(circle : Circle) : Boolean =
    intersects(circle).size > 0

  /**
   * Determines whether a linesegment and a ellipse intersects.
   */
  def intersect(ellipse : Ellipse) : Boolean =
  {
    false
  }

  /**
   * Determines whether a linesegment and a rectangle intersects.
   */
  def intersect(rect : Rectangle) : Boolean =
  {
    // Check that there exist at least one rectangle segment that intersects
    // with the given line.
    Segment.segmentsOnClosedPathOfPoints(rect points) map(
      rectangleSegment => intersect(rectangleSegment)
    ) reduceLeft( _ || _ )
  }

  /**
   * Examine whether a line and a line-segment intersects.
   * TODO!
   */
  def intersect(line : Line) =
  {
    false
  }

  /**
   * Determines whether two line segments intersect.
   */
  def intersect(line: Segment) : Boolean =
  {
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
    //   det = | A  -B | = | B  A |
    // If det = 0 there are no solutions (the lines are parallel). Now we find
    // the solutions:
    //   u = | (D - C)  -B | / det = | B  (D - C) | / det
    //   v =                         | A  (D - C) | / det
    // The way the segments are defined u and v must be in the range 0...1
    // (both inclusive), else the intersection is "outside" the line segments.
    val det = Vector.determinant(B, A)
    val uNotDivided = Vector.determinant(B, D - C)
    val vNotDivided = Vector.determinant(A, D - C)
    
    // The determinant must not be 0, and 0 <= u <= 1 and 0 <= v <= 1.
    (det != 0 && between0And1(uNotDivided, det) && between0And1(vNotDivided, det))
  }

  /**
   * Returns a list of points where a the Line Segment intersects with a given arc.
   */
  def intersects(arc : Arc) : List[Vector] =
    intersects(Circle(arc.center, arc.start)).filter(p => arc.insideArcDegrees((p - arc.center).angle))

  /**
   * Returns a list of points, defined as the intersection(s) between the
   * segment and the circle.
   */
  def intersects(circle : Circle) : List[Vector] =
  {
    val f           = p2 - p1
    val g           = p1 - circle.center
    val determinant = (f * g) * (f * g) - (f * f) * (g * g - circle.radius * circle.radius)
    
    val t =
      if (determinant < 0)
        List()
      else if (determinant > 0)
        List((-(f * g) - scala.math.sqrt(determinant)) / (f * f),
             (-(f * g) + scala.math.sqrt(determinant)) / (f * f))
      else
        List(-(f * g) / (f * f))

    // Filter out the points, that isn't on the line-segment.
    t.map(f * _ + p1).filter( point =>
      ((p2 - p1) * (point - p1) >= 0 &&
       (p1 - p2) * (point - p2) >= 0)
    )
  }

  /**
   * Returns a list of points, defined as the intersection(s) between the
   * segment and the rectangle.
   */
  def intersects(rectangle : Rectangle) : List[Vector] =
    Segment.segmentsOnClosedPathOfPoints(rectangle points).map(this.intersects(_)).flatten

  /**
   * Returns a list of points, defined as the intersection(s) between the line-segment and the line.
   * TODO!
   */
  def intersects(line : Line) =
  {
    List()
  }

  /**
   * Returns a list of points, defined as the intersection between this segment and that segment.
   */
  def intersects(line : Segment) : List[Vector] = {
    // The line segments are defined by the equations:
    //   L1 = A*u + C
    //   L2 = B*v + D
    val A = p2 - p1
    val B = line.p2 - line.p1
    val C = p1
    val D = line.p1
    // To findRecursive the intersection, set the two above equations equal:
    //   L1 = L2  <=>  A*u + C = B*v + D
    // The intersection is defined by solving (u,v) in the linear system:
    //   A*u - B*v = D - C
    // Calculate the determinant:
    //   det = | A  -B | = | B  A |
    // If det = 0 there are no solutions (the lines are parallel).
    val det = Vector.determinant(B, A)
    // The way the segments are defined u and v must be in the range 0...1
    // (both inclusive), else the intersection is "outside" the line segments.
    val uNotDivided = Vector.determinant(B, D - C)
    val vNotDivided = Vector.determinant(A, D - C)

    // Test the conditions above.
    if (det != 0 && between0And1(uNotDivided, det) && between0And1(vNotDivided, det)) {
      // Now we find the solution for u:
      //   u = | (D - C)  -B | / det = | B  (D - C) | / det
      val u = uNotDivided / det
      // And use it in the equation L1 = A*u + C
      val intersection = A*u + C
      List(intersection) // Return the intersection.
    } else List[Vector]() // No intersection
  }
  

}

object Segment2D {

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
  def segmentsOnOpenPathOfPoints(points : List[Vector]) : List[Segment] =
  {
    points match {
      case Nil => Nil
      case point :: Nil => Nil
      case point1 :: point2 :: tail =>
                    List(new Segment(point1, point2)) ::: segmentsOnOpenPathOfPoints(point2 :: tail)
    }
  }

  /**
   * Transforms a list of points into a list of lines, that
   * closes the whole figure... Example:
   * <pre>
   * *    *        *----*
   *           ==> |    |
   * *             *----*
   * </pre>
   * @param points  A list of vectors
   * @return List[Segment]  A list of lineSegments
   */
  def segmentsOnClosedPathOfPoints(points : List[Vector]) : List[Segment] =
  {
    points match {
      case Nil => Nil
      case list => segmentsOnOpenPathOfPoints(list ::: List(list.head))
    }
  }

}

