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

package com.siigna.util.rtree

import com.siigna.util.geom.SimpleRectangle2D

/**
 * Orders rectangles after one of four available dimensions.
 * 
 * @author Jens Egholm <jensep@gmail.com>
 */
trait MBROrdering extends Ordering[SimpleRectangle2D]

/**
 * Orders two shapes after the least x value.
 */
case object OrderMinX extends MBROrdering {
  def compare(s1 : SimpleRectangle2D, s2 : SimpleRectangle2D) =
    java.lang.Double.compare(s1.xMin, s2.xMin)
}
/**
 * Orders two shapes after the least y value.
 */
case object OrderMinY extends MBROrdering {
  def compare(s1 : SimpleRectangle2D, s2 : SimpleRectangle2D) =
    java.lang.Double.compare(s1.yMin, s2.yMin)
}
/**
 * Orders two shapes after the biggest x value.
 */
case object OrderMaxX extends MBROrdering {
  def compare(s1 : SimpleRectangle2D, s2 : SimpleRectangle2D) =
    java.lang.Double.compare(s1.xMax, s2.xMax)
}
/**
 * Orders two shapes after their biggest y value.
 */
case object OrderMaxY extends MBROrdering {
  def compare(s1 : SimpleRectangle2D, s2 : SimpleRectangle2D) =
    java.lang.Double.compare(s1.yMax, s2.yMax)
}