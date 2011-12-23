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
 * A circle. Represented by a center and a point on the circle.
 */
trait Circle[D <: Dimension] extends EnclosedGeometry[D]
{
  
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