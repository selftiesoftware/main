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

package com.siigna.app.model.action

import com.siigna.app.model.{Selection, Model}
import com.siigna.util.geom.{Rectangle2D, Vector2D}
import com.siigna.app.model.shape.{EmptyShapeSelector, Shape, ShapeLike}

/**
 * An object that provides shortcuts to select objects in the model. Selections are crucial
 * to manipulating [[com.siigna.app.model.shape.Shape]]s, since they can provide dynamic
 * manipulation on everything ranging from subsets of a single shape to a large collection
 * of whole shapes.
 *
 * <br />
 * These selections are stored in the [[com.siigna.app.model.Selection]] class which is
 * stored in the [[com.siigna.app.model.MutableModel]]. Each time a model changes the selection
 * are destroyed.
 *
 * <br />
 * Selection does not duplicate the shapes, since this would take up way too much space, but
 * contains information about which parts of the shapes are selected (the
 * [[com.siigna.app.model.shape.ShapeSelector]]). If a user chooses to select only one point of
 * a larger shape for instance, the selection has to support this.
 *
 * @see Model
 * @see MutableModel
 * @see Selection
 * @see Shape
 */
object Select {

  /**
   * Selects one whole [[com.siigna.app.model.shape.Shape]].
   * @param id  The ID of the shape to getPart.
   */
  def apply(id : Int) {
    Model select id
  }
  
  def apply(id : Int, point : Vector2D) {
    Model.select(id, Model(id).getPart(point))
  }
  
  def apply(id : Int, r : Rectangle2D) {
    Model.select(id, Model(id).getPart(r))
  }
  
  def apply(r : Rectangle2D) {
    // TODO: Find everything close to the rectangle
    val selection = Model(r).map(t => t._1 -> t._2.getPart(r))
    val filtered = selection.filter(_._2 != EmptyShapeSelector)
    Model.select(Selection(filtered))
  }

  /**
   * Selects several [[com.siigna.app.model.shape.Shape]]s.
   * @param ids  The id's of the shapes to select.
   */
  def apply(ids : Int*) {
    Model select ids
  }

  /**
   * Selects several [[com.siigna.app.model.shape.Shape]]s.
   * @param ids The shapes to select.
   */
  def apply(ids : Traversable[Int]) {
    Model select ids
  }

}