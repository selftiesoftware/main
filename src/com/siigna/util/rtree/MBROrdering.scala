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
 * Orders Minimum Bounding Rectangles after one of four available dimensions.
 * 
 * @author Jens Egholm <jensep@gmail.com>
 */
trait MBROrdering extends Ordering[MBR]

case object OrderMinX extends MBROrdering {
  def compare(r1 : MBR, r2 : MBR) = java.lang.Double.compare(r1.xMin, r2.xMin)
}
case object OrderMinY extends MBROrdering {
  def compare(r1 : MBR, r2 : MBR) = java.lang.Double.compare(r1.yMin, r2.yMin)
}
case object OrderMaxX extends MBROrdering {
  def compare(r1 : MBR, r2 : MBR) = java.lang.Double.compare(r2.xMax, r1.xMax)
}
case object OrderMaxY extends MBROrdering {
  def compare(r1 : MBR, r2 : MBR) = java.lang.Double.compare(r2.yMax, r1.yMax)
}