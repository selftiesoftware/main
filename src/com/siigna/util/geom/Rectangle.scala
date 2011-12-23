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
 * A rectangle given by two points. <b>Thus this rectangle cannot be rotated</b>.
 * TODO: Refactor with error-handling.
 * TODO: Refactor to an inner class with four doubles as parameters!
 */
trait Rectangle[D <: Dimension] extends EnclosedGeometry[D]
{

  /**
   * Computes the area of the rectangle.
   */
  def area : Double

  /**
   * The boundary of the rectangle. In this case returns itself.
   */
  def boundary = this

  /**
   * Examines whether a given rectangle is within (or on top of) the four boundaries
   * of this rectangle.
   */
  def contains(rectangle : Rectangle[D]) : Boolean

  /**
   * Calculate the circumference of the rectangle.
   */
  def circumference : Double

  /**
   * Calculates the distance to a point.
   */
  def distanceTo(point : Vector[D]) : Double

  /**
   * Expands this rectangle to include an arc.
   * TODO: Not the right way to include an arc!
   */
  def expand(arc : Arc[D]) : Rectangle[D]

  /**
   * Expands this rectangle to include a circle.
   */
  def expand(circle : Circle[D]) : Rectangle[D]

  /**
   * Expands this rectangle to include an ellipse.
   */
  def expand(e : Ellipse[D]) : Rectangle[D]

  /**
   * Expands this rectangle to include a point.
   */
  def expand(point : Vector[D]) : Rectangle[D]

  /**
   * Expands this rectangle to include another rectangle.
   */
  def expand(rect : Rectangle[D]) : Rectangle[D]

  /**
   * Checks whether the given point is on the outer edge of the rectangle.
   */
  def onPeriphery(point : Vector[D]) : Boolean

  /**
   * Calculate the overlap between this and another rectangle. If two rectangles do not overlap the area is 0.
   */ 
  def overlap(that : Rectangle[D]) : Double

  /**
   * Transforms the rectangle with the given matrix.
   */
  def transform(t : TransformationMatrix[D]) : Rectangle[D]

  /**
   * Returns a rectangle that encapsulates both this rectangle and the given rectangle.
   */
  def union(that : Rectangle[D]) : Rectangle[D]

}

/**
 * A companion object for the Rectangle class.
 */
object Rectangle {

  /**
   * Creates a 2-dimensional rectangle.
   */
  def apply(xMin : Double, yMin : Double, xMax : Double, yMax : Double) = new Rectangle2D(xMin, yMin, xMax, yMax)

  /**
   * Creates a 2-dimensional rectangle.
   */
  def apply(v1 : Vector2D, v2 : Vector2D) =
    new Rectangle2D(math.min(v1.x, v2.x), math.min(v1.y, v2.y), math.max(v1.x, v2.x), math.max(v1.y, v2.y))

}