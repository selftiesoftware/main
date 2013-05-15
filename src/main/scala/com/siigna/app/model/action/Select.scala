/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.model.action

import com.siigna.util.geom.Vector2D
import com.siigna.app.model.Drawing
import com.siigna.app.model.selection._
import com.siigna.app.model.shape.Shape
import com.siigna.util.geom.SimpleRectangle2D
import scala.Some

/**
 * An object that provides shortcuts to selecting objects in the model. Selections are crucial
 * to manipulating [[com.siigna.app.model.shape.Shape]]s, since they can provide dynamic
 * manipulation on everything ranging from subsets of a single shape to a large collection
 * of whole shapes.
 *
 * <br />
 * These selections are stored in the [[com.siigna.app.model.selection.Selection]] class which is
 * stored in the [[com.siigna.app.model.Model]]. Each time a model changes the selection
 * are destroyed. This is done mainly to avoid inconsistent selections (if shapes are removed etc.).
 *
 * <br />
 * Selection does not duplicate the shapes, but contains information about which parts of the shapes are selected
 * (the [[com.siigna.app.model.selection.ShapeSelector]]). If a user chooses to select only one point of
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
   * @param id  The ID of the shape to select.
   */
  def apply(id : Int) {
    Drawing select id
  }
  
  def apply(id : Int, point : Vector2D) {
    val shape = Drawing(id)
    Drawing select Selection(id -> (shape -> shape.getSelector(point)))
  }
  
  def apply(id : Int, r : SimpleRectangle2D) {
    val shape = Drawing(id)
    Drawing select Selection(id -> (shape -> shape.getSelector(r)))
  }

  /**
   * Selects several [[com.siigna.app.model.shape.Shape]]s.
   * @param ids  The id's of the shapes to select.
   */
  def apply(ids : Int*) {
    Drawing select ids
  }

  /**
   * Selects several [[com.siigna.app.model.shape.Shape]]s.
   * @param ids The shapes to select.
   */
  def apply(ids : Traversable[Int]) {
    Drawing select ids
  }

  /**
   * Selects the parts of the given shapes that is close to the given point. If no selection could be made (the
   * given point did not result in any meaningful selections), nothing happens.
   * @param ids  The ids of the shapes to select a point in.
   * @param point  The point to match in the given shapes.
   */
  def apply(ids : Traversable[Int], point : Vector2D) {
    val shapes = ids.map(id => {
      val shape = Drawing(id)
      id -> (shape -> shape.getSelector(point))
    }).toMap.filter(p => p._2._2 != EmptyShapeSelector)
    // Only select something if the map is not empty
    if (!shapes.isEmpty) {
      Drawing select Selection(shapes)
    }
  }

  /**
   * Selects a part of a shape based on its id. If the ShapePart is a FullShapePart then the
   * entire shape is selected, if the part is empty or no shape with the given id could be found in the model,
   * nothing happens.
   * @param id  The id of the shape
   * @param selector  The selector of the shape describing how the shape should be selected.
   * @return  The new selection after the selection.
   */
  def apply(id : Int, selector : ShapeSelector) : Selection = {
    Drawing.get(id) match {
      case Some(s) => Drawing.selection = Drawing.selection.add(id, s -> selector)
      case _ => Drawing.selection
    }
  }

  def apply(rectangle : SimpleRectangle2D, entireShapes : Boolean = true) : Selection = {
    val shapes = if (!entireShapes) {
      Drawing(rectangle).map(t => t._1 -> (t._2 -> t._2.getSelector(rectangle)))
    } else {
      // TODO: Write a method that can take t._2.geometry and NOT it's boundary...
      Drawing(rectangle).collect {
        case t if (rectangle.intersects(t._2.geometry.boundary)) => {
          (t._1 -> (t._2 -> FullShapeSelector))
        }
      }
    }
    Drawing.selection = Drawing.selection.add(shapes)
  }

  /**
   * Select every [[com.siigna.app.model.shape.Shape]] in the [[com.siigna.app.model.Drawing]].
   * @return  The new selection, containing all the shapes in the model.
   */
  def all() = {
    Drawing.select(Selection(Drawing.shapes.map(i => i._1 -> (i._2 -> FullShapeSelector))))
  }

  /**
   * Selects the parts of the given shapes that is close to the given points. If no selection could be made the
   * given point did not result in any meaningful selections), nothing happens.
   * @param shapes  The shapes to select one or more parts of.
   * @param point  The point to match in the given shapes.
   */
  def apply(shapes : Map[Int, Shape], point : Vector2D) {
    val selection = shapes.map(t => t._1 -> (t._2 -> t._2.getSelector(point))).filter(_._2._2 != EmptyShapeSelector)
    if (!selection.isEmpty) {
      Drawing select Selection(selection)
    }
  }

  def toggle(point : Vector2D) {
    val shapes = Drawing(point)
    val selection = shapes.map(t => t._1 -> t._2.getSelector(point))
    if (!selection.isEmpty) {
      Drawing.toggleSelect(selection)
    }
  }

  /**
   * Toggles the [[com.siigna.app.model.selection.ShapeSelector]]s found of [[com.siigna.app.model.shape.Shape]]s
   * found in the [[com.siigna.app.model.Drawing]] by the given [[com.siigna.util.geom.SimpleRectangle2D]].
   * We toggles them by removing the parts from the shapes if they already have been selected, or
   * adding them to the selection if they are not already selected.
   * @param rectangle The rectangle used to search for shapes in the model.
   * @param entireShapes  A boolean value signalling if the entire shape should be selected if some of it touches
   *                      the rectangle (true) or just the points included in the rectangle.
   * @return  The new selection either with the shape-selectors added or removed, depending on its appearance in
   *          the previous selection. If a shape already existed in the previous selection and the subtraction of
   *          the selector results in an [[com.siigna.app.model.selection.EmptyShapeSelector]], the shape is
   *          completely removed from the selection.
   */
  def toggle(rectangle : SimpleRectangle2D, entireShapes : Boolean) = {
    val shapes = if (!entireShapes) {
      Drawing(rectangle).map(t => t._1 -> t._2.getSelector(rectangle))
    } else {
      // TODO: Write a method that can take t._2.geometry and NOT it's boundary...
      Drawing(rectangle).collect {
        case t if (rectangle.intersects(t._2.geometry.boundary)) => {
          (t._1 -> FullShapeSelector)
        }
      }
    }

    Drawing.toggleSelect(shapes)
  }

}