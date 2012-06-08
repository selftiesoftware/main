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

package com.siigna.util.geom

/**
 * The mathematical class for a line segment, defined as a line with a start
 * point (p1) and a end point (p2). The segment has a finite length.
 */
@SerialVersionUID(-852679043)
case class Segment2D(p1 : Vector2D, p2 : Vector2D) extends GeometryBasic2D with Segment {

  import java.lang.Double.POSITIVE_INFINITY
  
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

  def boundary = Rectangle(p1, p2)

  def distanceTo(arc : Arc2D) = POSITIVE_INFINITY
  def distanceTo(circle : Circle2D) = POSITIVE_INFINITY
  def distanceTo(ellipse : Ellipse2D) = POSITIVE_INFINITY
  def distanceTo(line : Line2D) = POSITIVE_INFINITY
  def distanceTo(rectangle : Rectangle2D) = POSITIVE_INFINITY
  def distanceTo(segment : Segment2D) = POSITIVE_INFINITY

  /**
   * Calculates the distance between a vector and the line segment
   * TODO: Split this up for optimization.
   */
  def distanceTo(point : Vector2D) : Double = (point - closestPoint(point)).length

  /**
   * Calculates the closest point from a point to this line.
   * TODO: Split the function up, so distanceToPoint uses a more optimized algorithm.
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
   * Determines whether this segment intersects with a given Arc.
   */
  def intersects(arc : Arc2D) = false
  def intersects(circle : Circle2D) : Boolean = false
  def intersects(ellipse : Ellipse2D) : Boolean = false
  def intersects(rect : Rectangle2D) : Boolean = false
  def intersects(line : Line2D) = false

  /**
   * Determines whether two line segments intersect.
   */
  def intersects(line : Segment2D) : Boolean =
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
    val det = Vector2D.determinant(B, A)
    val uNotDivided = Vector2D.determinant(B, D - C)
    val vNotDivided = Vector2D.determinant(A, D - C)
    
    // The determinant must not be 0, and 0 <= u <= 1 and 0 <= v <= 1.
    (det != 0 && between0And1(uNotDivided, det) && between0And1(vNotDivided, det))
  }

  /**
   * Returns a list of points where a the Line Segment intersects with a given arc.
   */
  def intersections(arc : Arc2D) : Set[Vector2D] =
    intersections(Circle(arc.center, arc.radius)).filter(p => arc.insideArcDegrees((p - arc.center).angle))

  /**
   * Returns a list of points, defined as the intersection(s) between the
   * segment and the circle.
   */
  def intersections(circle : Circle2D) : Set[Vector2D] =
  {
    val f           = p2 - p1
    val g           = p1 - circle.center
    val determinant = (f * g) * (f * g) - (f * f) * (g * g - circle.radius * circle.radius)
    
    val t =
      if (determinant < 0)
        Set()
      else if (determinant > 0)
        Set((-(f * g) - scala.math.sqrt(determinant)) / (f * f),
             (-(f * g) + scala.math.sqrt(determinant)) / (f * f))
      else
        Set(-(f * g) / (f * f))

    // Filter out the points, that isn't on the line-segment.
    t.map(f * _ + p1).filter( point =>
      ((p2 - p1) * (point - p1) >= 0 &&
       (p1 - p2) * (point - p2) >= 0)
    )
  }

  def intersections(ellipse : Ellipse2D) = Set()

  /**
   * Returns a list of points, defined as the intersection(s) between the
   * segment and the rectangle.
   */
  def intersections(rectangle : Rectangle2D) = Set()

  /**
   * Returns a list of points, defined as the intersection(s) between the line-segment and the line.
   * TODO!
   */
  def intersections(line : Line2D) = Set()

  /**
   * Returns a list of points, defined as the intersection between this segment and that segment.
   */
  def intersections(line : Segment2D) = {
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
    val det = Vector2D.determinant(B, A)
    // The way the segments are defined u and v must be in the range 0...1
    // (both inclusive), else the intersection is "outside" the line segments.
    val uNotDivided = Vector2D.determinant(B, D - C)
    val vNotDivided = Vector2D.determinant(A, D - C)

    // Test the conditions above.
    if (det != 0 && between0And1(uNotDivided, det) && between0And1(vNotDivided, det)) {
      // Now we find the solution for u:
      //   u = | (D - C)  -B | / det = | B  (D - C) | / det
      val u = uNotDivided / det
      // And use it in the equation L1 = A*u + C
      val intersection = A*u + C
      // Return the intersection.
      Set(intersection)
    } else Set() // No intersection
  }
  
  def transform(t : TransformationMatrix) = new Segment2D(t.transform(p1), t.transform(p2))
  
  lazy val vertices = Seq(p1, p2)

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
   * *             *----*
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

