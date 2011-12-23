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
 * An endless mathematically representation of a line with infinite length. 
 */
<<<<<<< HEAD:src/com/siigna/util/geom/Line.scala
trait Line[D <: Dimension] extends Geometry[D]
{
=======
case class Line2D(p1 : Vector2D, p2 : Vector2D) extends Line with Geometry2D {
  
  import java.lang.Double.POSITIVE_INFINITY

  type T = Line2D

  val center = (p1 + p2) / 2

  def boundary = Rectangle(p1, p2)
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Line2D.scala

  /**
   * Calculates the closest point on the line from a given vector.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Line.scala
  def closestPoint(point : Vector[D]) : Vector[D]
=======
  def closestPoint(point : Vector2D) = {
    // Found this at: http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
    // We take advantage of the fact that the slope of two orthogonal lines
    // multiplied together = -1.
    val lineVector = p2 - p1
    val constant   = ((point.x - p1.x) * (p2.x - p1.x) + (point.y - p1.y) * (p2.y - p1.y)) / (lineVector.length * lineVector.length)

    // Use the determinant to calculate the point.
    val x = p1.x + constant * ((p2.x - p1.x))
    val y = p1.y + constant * ((p2.y - p1.y))

    // Return the point.
    Vector2D(x, y)
  }
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Line2D.scala

  def distanceTo(arc : Arc2D) = POSITIVE_INFINITY
  def distanceTo(circle : Circle2D) = POSITIVE_INFINITY
  def distanceTo(ellipse : Ellipse2D) = POSITIVE_INFINITY
  def distanceTo(line : Line2D) = POSITIVE_INFINITY
  def distanceTo(rectangle : Rectangle2D) = POSITIVE_INFINITY

  /**
   * Calculates the distance between a vector and a line.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Line.scala
  def distanceTo(point : Vector[D]) : Double

  /**
   * Determines whether the line intersects with a rectangle.
   */
  def intersects(rectangle : Rectangle[D]) : Boolean
=======
  def distanceTo(point : Vector2D) : Double = {
    if (p1 != p2) {
      // Calculate the distance from the projection on the line to the point.
      (closestPoint(point) - point).length
    } else {
      (p1 - point).length
    }
  }

  def distanceTo(segment : Segment2D) = POSITIVE_INFINITY

  def intersects(arc : Arc2D) = false
  def intersects(circle : Circle2D) = false
  def intersects(ellipse : Ellipse2D) = false
  def intersects(line : Line2D) = false
  def intersects(rectangle : Rectangle2D) = false
  def intersects(segment : Segment2D) = false

  def intersections(arc : Arc2D) = Set()
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Line2D.scala

  /**
   * Returns a list of points, defined as the intersection(s) between the
   * line and the circle.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Line.scala
  def intersections(circle : Circle[D]) : Set[Vector[D]]
=======
  def intersections(circle : Circle2D) =
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

    t map(f * _ + p1)
  }
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Line2D.scala

  def intersections(ellipse : Ellipse2D) = Set()

  /**
   * Returns a set of points, defined as the intersection(s) between the
   * two endless lines.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Line.scala
  def intersections(line : Line) : Set[Vector[D]]
=======
  def intersections(line : Line2D) = {
    // The lines are defined by the equations:
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
    // If det = 0 there are no solutions (the lines are parallel). Now we findRecursive
    // the solution for the first line, L1:
    //   u = | (D - C)  -B | / det = | B  (D - C) | / det
    val det = Vector2D.determinant(B, A)
    val u   = Vector2D.determinant(B, D - C) / det

    // To calculate the point of intersection we insert u in the line equation.
    // If the determinant = 0, the lines are parallel
    if (det == 0)
      Set()
    else
      Set[Vector2D](A * u + C)
  }
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Line2D.scala

  def intersections(rectangle : Rectangle2D) = Set()
  def intersections(segment : Segment2D) = Set()

  def transform(t : TransformationMatrix) = {
    Line2D(p1.transform(t), p2.transform(t))
  }

  def vertices = Set(p1, p2)

}

<<<<<<< HEAD:src/com/siigna/util/geom/Line.scala
/**
 * A companion object to the Line trait.
 */
object Line {

  /**
   * Creates a 2D line.
   */
  def apply(p1 : Vector2D, p2 : Vector2D) = new Line2D(p1, p2)
=======
object Line2D {

  /**
   * Transforms a list of points into a list of lines, that doesn't
   * close the whole figure... Example:
   * <pre>
   * *    *        *----*
   *           ==> |    |
   * *    *        *    *
   * </pre>
   *
   * @return List[Line] A list of endless lines
   */
  def linesOnOpenPathOfPoints(points : List[Vector2D]) : List[Line] =
  {
    points match {
      case Nil => Nil
      case point :: Nil => Nil
      case point1 :: point2 :: tail =>
                    List(new Line2D(point1, point2)) ::: linesOnOpenPathOfPoints(point2 :: tail)
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
   * @return List[Line]  A list of endless lines
   */
  def linesOnClosedPathOfPoints(points : List[Vector2D]) : List[Line] =
  {
    points match {
      case Nil => Nil
      case list => linesOnOpenPathOfPoints(list ::: List(list.head))
    }
  }
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Line2D.scala

}