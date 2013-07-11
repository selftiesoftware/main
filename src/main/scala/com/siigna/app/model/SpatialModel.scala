/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */

package com.siigna.app.model

import org.khelekore.prtree.PRTree
import com.siigna.util.geom.{Rectangle2D, SimpleRectangle2D, Vector2D}
import shape.Shape
import com.siigna.app.Siigna

/**
 * An interface that supplies the model with spatial information and spatial queries to retrieve one or more shapes
 * from their position.
 */
trait SpatialModel[Key, Value <: Shape] {

  // TODO: Remove this in favour of the tree
  def shapes : Map[Key, Value]
  
  /**
   * The [[org.khelekore.prtree.PRTree]] (Prioritized RTree) that stores dimensional orderings.
   */
  //protected def rtree : PRTree

  /**
   * Query for shapes that are inside or intersecting the given boundary.
   * @param query  The query-rectangle or query-view defining the area for the shapes to be returned.
   * @return  The shapes that are inside or intersects the query-rectangle, paired with their keys.
   */
  def apply(query : Rectangle2D) : Map[Key, Value] = {
    shapes.filter((s : (Key, Value)) => {
      query.contains(s._2.geometry.boundary) || query.intersects(s._2.geometry)
    })
  }

  /**
   * Query for shapes close to the given point by a given radius.
   * @param point  The point to query.
   * @param radius  (Optional) The radius added to the selection distance. Defaults to
   *                [[com.siigna.app.Siigna#selectionDistance]].
   * @return  A Map of the shapes and their ids found within a distance from the given point <code><=</code> than the
   *          given radius.
   */
  def apply(point : Vector2D, radius : Double = Siigna.selectionDistance) : Map[Key, Value] = {
    shapes.filter(_._2.geometry.distanceTo(point) <= radius)
  }

  /**
   * The minimum bounding rectangle for the model, i. e. the smallest rectangle including all the 
   * elements in the model.
   * @return  The minimum-bounding rectangle containing all the shapes in the model.
   */
  def mbr : SimpleRectangle2D =
    if      (shapes.isEmpty)   SimpleRectangle2D(0, 0, 0, 0)
    else if (shapes.size == 1) shapes.head._2.geometry.boundary
    else { //TODO: PERFORMANCE DEPLEATING OPERATION!
      shapes.tail.foldLeft(shapes.head._2.geometry.boundary)((a : SimpleRectangle2D, b : (Key, Value)) => {
        a.expand(b._2.geometry.boundary)
      })
    }


}
