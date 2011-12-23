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
 */
trait Rectangle
{

  type T <: Rectangle
  type V <: Vector

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
  def contains(rectangle : T) : Boolean

  /**
   * Calculate the circumference of the rectangle.
   */
  def circumference : Double

  /**
   * Calculates the distance to a point.
   */
  def distanceTo(point : V) : Double

  /**
   * Checks whether the given point is on the outer edge of the rectangle.
   */
  def onPeriphery(point : V) : Boolean

  /**
   * Calculate the overlap between this and another rectangle. If two rectangles do not overlap the area is 0.
   */ 
  def overlap(that : T) : Double

  /**
   * Transforms the rectangle with the given matrix.
   */
  def transform(t : TransformationMatrix) : T

  /**
   * Returns a rectangle that encapsulates both this rectangle and the given rectangle.
   */
  def union(that : T) : T

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
  def apply(v1 : Vector2D, v2 : Vector2D) = Rectangle2D(v1, v2)

}