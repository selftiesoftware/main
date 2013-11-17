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
import com.siigna.app.model.shape.Shape
import com.siigna.app.Siigna

/**
 * A trait that supplies the model with spatial information and spatial queries to retrieve one or more shapes
 * from their position.
 */
trait SpatialModel {

  /**
   * An empty PRTree for use if the 'real' prtree is unavailable.
   */
  protected lazy val emptyPRT = SiignaTree(Map())

  /**
   * The [[org.khelekore.prtree]] used by the model.
   */
  protected var PRT : Option[PRTree[SiignaTree.TreeType]] = None

  /**
   * A spatial search-tree (<a href="http://en.wikipedia.org/wiki/Priority_R-tree" title="PRTrees on Wikipedia">priority
   * r-tree</a>) for shapes used to search through the model.
   * The PRT is constructed lazily after the model changes, so the tree might not be up to date. If it has not been
   * calculated initially we create and return an empty one.
   * @return An instance of a [[org.khelekore.prtree.PRTree]].
   */
  def rtree : PRTree[SiignaTree.TreeType] = PRT.getOrElse(emptyPRT)

  /**
   * Query for shapes that are inside or intersecting the given boundary.
   * @param query  The query-rectangle or query-view defining the area for the shapes to be returned.
   * @return  The shapes that are inside or intersects the query-rectangle, paired with their keys.
   */
  def apply(query : SimpleRectangle2D) : Map[Int,Shape] = {
    SiignaTree.find(query,rtree).filter(s => query.intersects(s._2.geometry) || query.contains(s._2.geometry))
  }

  /**
   * Query for shapes close to the given point by a given radius.
   * @param point  The point to query.
   * @param radius  (Optional) The radius added to the selection distance. Defaults to
   *                [[com.siigna.app.Siigna#selectionDistance]].
   * @return  A Map of the shapes and their ids found within a distance from the given point <code><=</code> than the
   *          given radius.
   */
  def apply(point : Vector2D, radius : Double = Siigna.selectionDistance) : Map[Int,Shape] = {
    val r = Rectangle2D(point, radius * 2, radius * 2)
    SiignaTree.find(point,radius,rtree).filter(s => r.intersects(s._2.geometry) || r.contains(s._2.geometry))
  }

  /**
   * The minimum bounding rectangle for the model, i. e. the smallest rectangle including all the 
   * elements in the model.
   * @return  The minimum-bounding rectangle containing all the shapes in the model.
   */
  def mbr : SimpleRectangle2D = SiignaTree.mbr(rtree)

  /**
   * Locates one single shape that is closest to the given point. If no shape could be found None is returned.
   * @param point  The center of the search query.
   * @param radius  The radius of the search query.
   * @return  Some[(Int, Shape)] if a shape could be found, None otherwise.
   */
  def nearestShape(point : Vector2D, radius : Double = Siigna.selectionDistance) : Option[(Int, Shape)] = {
    val nearestShapes = apply(point, radius)
    if (!nearestShapes.isEmpty) {
      Some(nearestShapes.reduceLeft((a, b) => if (a._2.distanceTo(point) < b._2.distanceTo(point)) a else b))
    } else None
  }

}
