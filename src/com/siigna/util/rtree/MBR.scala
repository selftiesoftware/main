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

package com.siigna.util.rtree

/**
 * A class for a Minimum Bounding Rectangle.
 * The MBR is efficiently a representation of a point i 4D (i. e. a rectangle), 
 * created for optimization purposes in the Prioritized R-tree.
 * <b>Note: It is assumed that xMin <= xMax and yMin <= yMax!</b>
 * 
 * @author Jens Egholm <jensep@gmail.com>
 */
case class MBR(xMin : Double, yMin : Double, xMax : Double, yMax: Double) {

  /**
   * The center of the MBR as a 2D Tuple.
   */
  lazy val center : Tuple2[Double, Double] = (xMin + xMax * 0.5, yMin + yMax * 0.5)

  /**
   * Expand this MBR with another MBR.
   */
  def expand(that : MBR) =
    MBR(math.min(that.xMin, xMin),
        math.min(that.yMin, yMin),
        math.max(that.xMax, xMax),
        math.max(that.yMax, yMax))

  /**
   * Determines whether the rectangle intersects with <b>or</b> is contained within another MBR.
   */
  def overlap(that : MBR) =
	  !(xMin > that.xMax || xMax < that.xMin || yMin > that.yMax || yMax < that.yMin)
	  
}