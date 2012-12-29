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

import com.siigna._

/**
 * Utilities for an ellipse
 * The ellipse is represented in the form <code>(x/a)^2 + (y/b)^2 = 1</code>.
 *
 * Pre-condition: <code>p.x != a</code>.
 * If the pre-condition isn't fulfilled, the ellipse is created with a very small b.
 */
trait Ellipse {

  /**
   * Calculates the distance from a ellipse to a point.
   */
/** Todo...
  def distanceToPoint(point : Vector) =
  {
    // Creates a transformation matrix that translates (pans) and rotates the
    // ellipse, so the center is positioned at <code>Vector(0, 0)</code> and the
    // two focus-points is on at the X-axis
    val rotated  = f1 angle
    val toCenter = TransformationMatrix(-center, 1) rotate(-rotated, center)

    // Transform the points
    val newF1     = f1.transform(toCenter)
    val newF2     = f2.transform(toCenter)
    val newCenter = center.transform(toCenter)

    // The ellipse is symmetric on all the four axes, so we flip the coordinates
    // of the point, so that we know it's in the first quadrant.
    val transformedPoint = point.transform(toCenter)
    val newPoint         = Vector(Math.abs(transformedPoint.x), Math.abs(transformedPoint.y))

    // The ellipse is a circle
    if (f1 == f2) {
      (a - (center - point).length)
      
    // If the x-coordinate of the point is zero, then the shortest distance to
    // the ellipse must be (0, b)
    } else if (newPoint.x == 0) {
      (newPoint + Vector(0, b)).length

    // Else if the y-coordinate of the point is zero, then the distance is
    // different, depending on whether you're inside the ellipse or outside.
    } else if (newPoint.y == 0) {
      // Inside:
      if (newPoint.x < (a - (b * b) / a)) {
        (b * Math.sqrt(Math.abs((1 - newPoint.x * newPoint.x) / (a * a - b * b))))
      // Outside:
      } else {
        Math.abs(a - newPoint.x)
      }
    // Here the point is not on either axis and we are forced to use more brutal
    // methods..
    } else {
      // Since we now are in a position where the point only can be found
      // using the equation for the ellipse, we have to guess the closest point
      // using Newton's method.

      // The function for finding a point on an ellipse, closest to a given point
      // in a 2-dimensional space, for a given value t:
      // <code>f(t) = (a * x / (t + a^2))^2 + (b * y / (t + b^2))^2 - 1 = 0</code>
      def native(t : Double) =
        (a * point.x / (t + a * a)) * (a * point.x / (t + a * a)) + (b * point.y / (t + b * b)) * (b * point.y / (t + b * b)) - 1

      // The derived function of the above:
      // <code>f'(t) = (-2 * a^2 * x^2) / (t + a^2)^3 + (-2 * b^2 * y^2) / (t + b^2)^3</code>.
      def derived(t : Double) =
        ((-2 * a * a * point.x * point.x) / (t + a * a) * (t + a * a) * (t + a * a)) + ((-2 * b * b * point.y * point.y) / ((t + b * b) * (t + b * b) * (t + b * b)))

      // Newton's metod is an iterative method that narrows the solution
      // using the following approximation:
      // <code>Xn+1 = (f(Xn) + f(Xn)/f'(Xn)) / 2</code>.
      def newton(t : Double) : Double = (native(t) + native(t) / derived(t)) / 2

      // Tests whether the guess is good enough, by comparing it to the previous
      // guess.
      def isGoodEnough(guess : Double) = (Math.abs(guess - newton(guess)) < 0.000001)

      // Iteration of Newtons method.
      def newtonIteration(guess : Double) : Double = {
        if (isGoodEnough(guess)) Math.abs(guess)
        else newtonIteration(newton(guess))
      }

      // Initial guess
      // If the point is inside the ellipse, the best guess is t = 0, otherwise
      // we gues t = b * (y - b).
      var guess : Double = if ((f1 - point).length + (f2 - point).length - 2 * a > 0) { Math.abs(b * (point.y - b)) } else { 0 }

      // Performs the iteration.
      newtonIteration(guess)
    }
  }
  */
  
}

/**
 * Utilities for an ellipse
 * The ellipse is represented in the form <code>(x/a)^2 + (y/b)^2 = 1</code>.
 *
 * Pre-condition: <code>p.x != a</code>.
 * If the pre-condition isn't fulfilled, the ellipse is created with a very small b.
 */
@SerialVersionUID(-428914763)
case class Ellipse2D(f1 : Vector2D, f2 : Vector2D, p : Vector2D) extends Ellipse //with GeometryClosed2D
{

  lazy val area = math.Pi * a * b

  val center = (f1 + f2)/2

  val a = ((f1 - p).length + (f2 - p).length)/2
  val b = try { p.y / scala.math.sqrt(scala.math.abs(1 - (p.x / a) * (p.x / a))) } catch {
    case e => //Log.warning("The two focal points are the same, parsing a very very small number.")
      0.00000001
  }
  val e = (center - f1).length / a

  def boundary = Rectangle2D(Vector2D(center.x - b, center.y - a), Vector2D(center.x + b, center.y + a))

  def contains[Geometry2D](g : Geometry2D) = false

  // TODO: Find a Math wiz.
  def distanceTo(geom : Geometry2D) = throw new UnsupportedOperationException("Ellipse: Not yet implemented with " + geom)

  /**
   * Calculates the distance from a ellipse to a point.
   */
  /** Todo...
  def distanceToPoint(point : Vector) =
  {
    // Creates a transformation matrix that translates (pans) and rotates the
    // ellipse, so the center is positioned at <code>Vector(0, 0)</code> and the
    // two focus-points is on at the X-axis
    val rotated  = f1 angle
    val toCenter = TransformationMatrix(-center, 1) rotate(-rotated, center)

    // Transform the points
    val newF1     = f1.transform(toCenter)
    val newF2     = f2.transform(toCenter)
    val newCenter = center.transform(toCenter)

    // The ellipse is symmetric on all the four axes, so we flip the coordinates
    // of the point, so that we know it's in the first quadrant.
    val transformedPoint = point.transform(toCenter)
    val newPoint         = Vector(Math.abs(transformedPoint.x), Math.abs(transformedPoint.y))

    // The ellipse is a circle
    if (f1 == f2) {
      (a - (center - point).length)

    // If the x-coordinate of the point is zero, then the shortest distance to
    // the ellipse must be (0, b)
    } else if (newPoint.x == 0) {
      (newPoint + Vector(0, b)).length

    // Else if the y-coordinate of the point is zero, then the distance is
    // different, depending on whether you're inside the ellipse or outside.
    } else if (newPoint.y == 0) {
      // Inside:
      if (newPoint.x < (a - (b * b) / a)) {
        (b * Math.sqrt(Math.abs((1 - newPoint.x * newPoint.x) / (a * a - b * b))))
      // Outside:
      } else {
        Math.abs(a - newPoint.x)
      }
    // Here the point is not on either axis and we are forced to use more brutal
    // methods..
    } else {
      // Since we now are in a position where the point only can be found
      // using the equation for the ellipse, we have to guess the closest point
      // using Newton's method.

      // The function for finding a point on an ellipse, closest to a given point
      // in a 2-dimensional space, for a given value t:
      // <code>f(t) = (a * x / (t + a^2))^2 + (b * y / (t + b^2))^2 - 1 = 0</code>
      def native(t : Double) =
        (a * point.x / (t + a * a)) * (a * point.x / (t + a * a)) + (b * point.y / (t + b * b)) * (b * point.y / (t + b * b)) - 1

      // The derived function of the above:
      // <code>f'(t) = (-2 * a^2 * x^2) / (t + a^2)^3 + (-2 * b^2 * y^2) / (t + b^2)^3</code>.
      def derived(t : Double) =
        ((-2 * a * a * point.x * point.x) / (t + a * a) * (t + a * a) * (t + a * a)) + ((-2 * b * b * point.y * point.y) / ((t + b * b) * (t + b * b) * (t + b * b)))

      // Newton's metod is an iterative method that narrows the solution
      // using the following approximation:
      // <code>Xn+1 = (f(Xn) + f(Xn)/f'(Xn)) / 2</code>.
      def newton(t : Double) : Double = (native(t) + native(t) / derived(t)) / 2

      // Tests whether the guess is good enough, by comparing it to the previous
      // guess.
      def isGoodEnough(guess : Double) = (Math.abs(guess - newton(guess)) < 0.000001)

      // Iteration of Newtons method.
      def newtonIteration(guess : Double) : Double = {
        if (isGoodEnough(guess)) Math.abs(guess)
        else newtonIteration(newton(guess))
      }

      // Initial guess
      // If the point is inside the ellipse, the best guess is t = 0, otherwise
      // we gues t = b * (y - b).
      var guess : Double = if ((f1 - point).length + (f2 - point).length - 2 * a > 0) { Math.abs(b * (point.y - b)) } else { 0 }

      // Performs the iteration.
      newtonIteration(guess)
    }
  }
    */

  /**
   * Examines whether the ellipse intersects with a segment
   */
  def intersects(geom : Geometry2D) = throw new UnsupportedOperationException("Ellipse: Not yet implemented with " + geom)

  /**
   * Examines whether the ellipse intersects with rectangle
   */
  def intersections(geom : Geometry2D) = throw new UnsupportedOperationException("Ellipse: Not yet implemented with " + geom)

}
