/*
 *
 *  * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 *  * to Share — to copy, distribute and transmit the work,
 *  * to Remix — to adapt the work
 *  *
 *  * Under the following conditions:
 *  * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 *  * Noncommercial — You may not use this work for commercial purposes.
 *  * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 *
 */

package com.siigna.app.model.action

import com.siigna.app.model.selection._
import com.siigna.app.model.shape.PolylineShape.PolylineShapeClosed
import scala.Some
import com.siigna.app.model.shape.PolylineShape.PolylineShapeClosed
import com.siigna.app.model.selection.BitSetShapeSelector
import com.siigna.app.model.Drawing
import com.siigna.util.geom.{SimpleRectangle2D, Rectangle2D, Vector2D}

/**
 *
 */
object SelectToggle {

  /**
   * Toggles the [[com.siigna.app.model.shape.Shape]] with the given id in the current selection by removing it if it
   * is fully selected, or adding it to the selection if it is not already selected.
   * @param id  The id of the shape.
   * @return  The new selection either with the shape added or removed, depending on its appearance in
   *          the previous selection.
   */
  def apply(id : Int) {
    Drawing.selection = Drawing.selection.get(id) match {
      case Some((_, FullShapeSelector)) => Drawing.selection.remove(id)
      case _ => Drawing.get(id) match {
        case Some(s) => Drawing.selection.add(id, s -> FullShapeSelector)
        case _ => Drawing.selection
      }
    }
  }

  /**
   * Toggles the selector of the given [[com.siigna.app.model.shape.Shape]]
   * in the current selection by removing the entire shape if it is fully selected, removing the part if it is
   * selected partially, or adding it to the selection if it is not already selected.
   * If the shape already existed in the previous selection and the subtraction of
   * the selector results in an [[com.siigna.app.model.selection.EmptyShapeSelector]], the shape is
   * completely removed from the selection.
   * @param id  The id of the shape.
   * @param selector  The selector describing the part of the shape to toggle.
   */
  def apply(id : Int, selector : ShapeSelector) {
    val s : Selection = Drawing.selection.get(id) match {
      // Completely remove the selection if all the points already are toggled
      case Some((_, s : BitSetShapeSelector)) if (s.contains(selector)) => Drawing.selection.remove(id, selector)
      // Otherwise we need to toggle the points if no neighbors exist
      case Some((shape, s : BitSetShapeSelector)) => {
        selector match {
          case BitSetShapeSelector(bits) => {
            // If it's a closed polyline we need to treat it specially
            val isClosedPolyline = shape match {
              case _ : PolylineShapeClosed => true
              case _ => false
            }

            val xs = bits.foldLeft(s.bits)((xs, i) => {
              // Add the index if the id has any neighbors
              if (xs(i - 1) || xs(i + 1) || (isClosedPolyline && (i == xs.last || i == xs.head))) xs + i
              // Remove the index if no neighbors were found
              else xs - i
            })
            Selection(Drawing.selection.updated(id, Drawing.selection(id)._1 -> ShapeSelector(xs)))
          }
          case EmptyShapeSelector => Drawing.selection
          case _ => Drawing.selection.remove(id)
        }
      }
      // Simple cases:
      case Some((_, FullShapeSelector)) => Drawing.selection.remove(id)
      case Some((shape, x)) => Drawing.selection.add(id, shape -> (x ++ selector))
      case _ => Drawing.selection.add(id, Drawing(id) -> selector)
    }

    Drawing.selection = s
  }



  /**
   * Toggles the [[com.siigna.app.model.shape.Shape]]s with the given ids in the current selection by removing them
   * if they already are selected, or adding them to the selection if they are not.
   * @param ids  The ids of the shape.
   * @return  The new selection either with the shapes added or removed, depending on their appearance in
   *          the previous selection.
   */
  def apply(ids : Traversable[Int]) {
    // Find the ids that are included in the selection already, and those that are not
    val (included, excluded) = ids.partition(i => Drawing.selection.contains(i))
    val mapped = excluded.toSeq.map(id => id -> (Drawing(id) -> FullShapeSelector)).toMap
    // Then remove the shapes that are in the selection, and add the ones that are not
    Drawing.selection = Drawing.selection.remove(included).add(mapped)
  }

  /**
   * Toggles the [[com.siigna.app.model.selection.ShapeSelector]]s of the given [[com.siigna.app.model.shape.Shape]]s
   * in the current selection by removing the parts from the shapes if they already have been selected, or
   * adding them to the selection if they are not already selected.
   * @param parts  The ids of the shapes mapped to the part to toggle from the selection.
   * @return  The new selection either with the shape-selectors added or removed, depending on its appearance in
   *          the previous selection. If a shape already existed in the previous selection and the subtraction of
   *          the selector results in an [[com.siigna.app.model.selection.EmptyShapeSelector]], the shape is
   *          completely removed from the selection.
   */
  def apply(parts : Map[Int, ShapeSelector]) {
    /*// Find the parts that are included in the selection already, and those that are not
    val (included, excluded) = parts.partition(t => selection.contains(t._1))
    // Remove the included selectors and add the excluded
    selection = selection.remove(included).add(excluded.map(t => t._1 -> (Drawing(t._1) -> t._2)))
    selection
    */
    // TODO: Optimize
    parts.foreach(t => apply(t._1, t._2))
  }
  /**
   * Toggles the [[com.siigna.app.model.shape.Shape]]s found close to the given [[com.siigna.util.geom.Vector2D]] by
   * the rules specified in the documentation for [[com.siigna.app.model.action.SelectToggle]]. Basically we
   * we toggle the selections by removing the parts from the shapes if they already have been selected, or adding them
   * to the selection if they are not already selected.
   * @param point  The point (Vector2D) to use to find shapes closer than [[com.siigna.app.Siigna#selectionDistance]]
   *               to the given point.
   */
  def apply(point : Vector2D) {
    val shapes = Drawing(point)
    val selection = shapes.map(t => t._1 -> t._2.getSelector(point))
    if (!selection.isEmpty) apply(selection)
  }

  /**
   * Toggles the [[com.siigna.app.model.shape.Shape]]s inside the given [[com.siigna.util.geom.SimpleRectangle2D]] by
   * the rules specified in the documentation for [[com.siigna.app.model.action.SelectToggle]]. Basically we
   * we toggle the selections by removing the parts from the shapes if they already have been selected, or adding them
   * to the selection if they are not already selected.
   * @param rectangle  The rectangle to use as bounding box for the shapes in the [[com.siigna.app.model.Model]], all
   *                   shapes inside it are included.
   * @param entireShapes  If set to true we toggle-select entire shapes as soon as they touch the rectangle, if
   *                      false we only select the parts that are inside the rectangle.
   */
  def apply(rectangle : SimpleRectangle2D, entireShapes : Boolean = false) {
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

    apply(shapes)
  }

}
