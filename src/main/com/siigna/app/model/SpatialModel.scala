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

package com.siigna.app.model

import com.siigna.util.geom.{Vector2D, Rectangle2D}
import shape.{Shape}
import collection.parallel.immutable.{ParMap, ParIterable}

/**
 * An interface that supplies
 */
trait SpatialModel[Key, Value <: Shape] {

  // TODO: Remove this in favour of the tree
  def shapes : Map[Key, Value]
  
  /**
   * The [[com.siigna.util.rtree.PRTree]] (Prioritized RTree) that stores dimensional orderings.
   * TODO: Implement spatial structure
   */
  //protected def rtree : PRTree

  /**
   * Query for shapes inside the given boundary.
   */
  def apply(query : Rectangle2D) : Map[Key, Value] = {
    shapes.filter((s : (Key, Value)) => query.contains(s._2.geometry.boundary) || query.intersects(s._2.geometry.boundary))
  }

  /**
   * Query for shapes close to the given point by a given radius.
   * @param query  The point to query.
   * @param radius  (Optional) The radius added to the point.
   */
  def apply(query : Vector2D, radius : Double = 5.0) : Map[Key, Value] = {
    apply(Rectangle2D(query.x - radius, query.y - radius, query.x + radius, query.y + radius))
  }

  /**
   * The minimum bounding rectangle for the model, i. e. the smallest rectangle including all the 
   * elements in the model.
   * @return  The minimum-bounding rectangle
   */
  def mbr : Rectangle2D =
    shapes.foldLeft[Rectangle2D](Rectangle2D(0, 0, 0, 0))((a : Rectangle2D, b : (Key, Value)) => a.expand(b._2.geometry.boundary))


}
