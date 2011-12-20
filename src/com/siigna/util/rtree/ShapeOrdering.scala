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

import com.siigna.app.model.shape.Shape

/**
 * Orders two shapes by their Minimum Bounding Rectangles after one of four available dimensions.
 * 
 * @author Jens Egholm <jensep@gmail.com>
 */
trait ShapeOrdering extends Ordering[Shape]

/**
 * Orders two shapes after the least x value.
 */
case object OrderMinX extends ShapeOrdering {
  def compare(s1 : Shape, s2 : Shape) =
    java.lang.Double.compare(s1.boundary.xMin, s2.boundary.xMin)
}
/**
 * Orders two shapes after the least y value.
 */
case object OrderMinY extends ShapeOrdering {
  def compare(s1 : Shape, s2 : Shape) =
    java.lang.Double.compare(s1.boundary.yMin, s2.boundary.yMin)
}
/**
 * Orders two shapes after the biggest x value.
 */
case object OrderMaxX extends ShapeOrdering {
  def compare(s1 : Shape, s2 : Shape) =
    java.lang.Double.compare(s1.boundary.xMax, s2.boundary.xMax)
}
/**
 * Orders two shapes after their biggest y value.
 */
case object OrderMaxY extends ShapeOrdering {
  def compare(s1 : Shape, s2 : Shape) =
    java.lang.Double.compare(s1.boundary.yMax, s2.boundary.yMax)
}