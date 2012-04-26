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

package com.siigna.app.model.shape

import com.siigna.util.geom.{Geometry2D, CollectionGeometry, Rectangle2D}

/**
 * A trait for immutable shapes containing other immutable shapes.
 * @tparam T  The type of shapes inside the collection.
 */
trait CollectionShape[G <: Shape] extends Shape with Iterable[G] {

  // TODO: Fix this
  def geometry = if (shapes.isEmpty) Rectangle2D.empty else CollectionGeometry(shapes.map(_.geometry))

  def iterator = shapes.toIterator

  /**
   * The inner shapes of the collection.
   */
  def shapes : Traversable[G]
}
