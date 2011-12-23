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
trait Line[D <: Dimension] extends Geometry[D]
{

  /**
   * Calculates the closest point on the line from a given vector.
   */
  def closestPoint(point : Vector[D]) : Vector[D]

  /**
   * Calculates the distance between a vector and a line.
   */
  def distanceTo(point : Vector[D]) : Double

  /**
   * Determines whether the line intersects with a rectangle.
   */
  def intersects(rectangle : Rectangle[D]) : Boolean

  /**
   * Returns a list of points, defined as the intersection(s) between the
   * line and the circle.
   */
  def intersections(circle : Circle[D]) : Set[Vector[D]]

  /**
   * Returns a list of points, defined as the intersection(s) between the
   * two endless lines.
   */
  def intersections(line : Line) : Set[Vector[D]]

}

/**
 * A companion object to the Line trait.
 */
object Line {

  /**
   * Creates a 2D line.
   */
  def apply(p1 : Vector2D, p2 : Vector2D) = new Line2D(p1, p2)

}