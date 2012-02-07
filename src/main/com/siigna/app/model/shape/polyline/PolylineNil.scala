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

package com.siigna.app.model.shape.polyline

import com.siigna.util.geom.TransformationMatrix

/**
 * An empty Polyline list.
 */
case object PolylineNil extends InnerPolylineShape {

  def isEmpty = true

  def head = throw new NoSuchElementException("head of empty polyline list")

  def tail = throw new NoSuchElementException("tail of empty polyline list")

  def toShapes = throw new UnsupportedOperationException("cannot create a shape from an empty list")

  def transform(t : TransformationMatrix) = throw new UnsupportedOperationException("cannot transform an empty list")

}
