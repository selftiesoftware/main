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

package com.siigna.util

import dxf.{DXFSection, DXFValue}
import geom.Vector

/**
 * Pimp-my-library for Vectors.
 */
class RichVector(vector : Vector) {

  /**
   * Return the Vector as a DXF object
   * @param value1  The id for the first value. Can vary depending on the type of object, but defaults to 10
   * @param value2  The id for the second value. Can vary depending on the type of object, but defaults to 20
   */
  def toDXF(value1 : Int = 10, value2 : Int = 20) = DXFSection(DXFValue(10, vector.x), DXFValue(20, vector.y))
  def toDXF : DXFSection = toDXF(10, 20)

}
