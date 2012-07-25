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
 * A <code>Geometry</code> that encloses (surrounds) a n-dimensional space. E. g. a <code>Rectangle</code>.
 * TODO: Set the methods in here and introduce generic type
 */
sealed trait GeometryEnclosed {

  /**
   * Determines the enclosed area of the geometry.
   */
  def area : Double

}

/**
 * A geometry that encloses a 2-dimensional space.
 */
trait GeometryEnclosed2D extends GeometryEnclosed with Geometry2D {

  type T <: GeometryEnclosed2D

  // TODO: Introduce expand

  /**
   * Examines whether a the given geometry is completely enclosed by this geometry.
   * @return true if the given geometry is inside, false otherwise
   */
  def contains(geometry : Geometry2D) : Boolean
  
}