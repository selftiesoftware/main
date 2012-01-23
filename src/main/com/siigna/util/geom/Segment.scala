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

