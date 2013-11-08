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

package com.siigna.app.model.selection

import scala.Some
import com.siigna.app.model.shape.PolylineShape.PolylineShapeClosed
import com.siigna.app.model.Drawing
import com.siigna.util.geom.{SimpleRectangle2D, Vector2D}

/**
 * <p>
 *   An object that provides shortcuts to toggle-selecting objects in the [[com.siigna.app.model.Model]]. Selections
 *   are a great tool to manipulate [[com.siigna.app.model.shape.Shape]]s, since they can provide dynamic
 *   manipulation without altering the model before the user deselcts the changes. A Selection works on everything
 *   ranging from subsets of a single shape to a large collection of whole shapes.
 * </p>
 *
 * <h4>A note on toggle-selection</h4>
 * <p>
 *   Toggling a selection means adding the parts of the selection that have not already been selected, while removing
 *   the parts that already are. There are some special cases though. Imagine having a
 *   [[com.siigna.app.model.shape.PolylineShape]] with three vertices and thus coherent line-segments. Lets say the
 *   first line-segment is already selected and the user wants to toggle-select the second part, the naive choice
 *   would be to add the last point of the polyline, but remove the middle point, since it already is selected. This
 *   is not very intuitive though, so whenever we encounter a selection that spans several points, we examine if
 *   any neighbour-points are active. If so we actually do not remove the active points to prevent this inconsistency.
 * </p>
 *
 * <p>
 *   The selections are stored in the [[com.siigna.app.model.selection.Selection]] class which is
 *   stored in the [[com.siigna.app.model.Model]]. Each time a model changes the selection
 *   are cleared. This is done mainly to avoid inconsistent selections (if shapes are removed etc.).
 * </p>
 * <p>
 *   A Selection does not duplicate the shapes, but instead stores the information about which parts of the shapes are
 *   selected (the [[com.siigna.app.model.selection.ShapeSelector]]). If a user chooses to select only one point of
 *   a larger shape for instance, the selection should support this.
 * </p>
 * <h2>Selection examples</h2>
 * <h4>Select shapes close to a point</h4>
 * <p>
 *   To select the shapes that are close to a given point, you simply need to provide a point like so:
 *   {{{
 *     val point = Vector2D(3.1415, 1.16)
 *     Select(point)
 *   }}}
 * </p>
 * <h4>Select shapes inside a selection-box</h4>
 * <p>
 *   To select shapes (or parts of them) that are inside a selection-box, simply provide a
 *   [[com.siigna.util.geom.Rectangle2D]] like so:
 *   {{{
 *     val rectangle = Rectangle2D(0, 0, 10, 10)
 *     Select(rectangle)
 *   }}}
 *   The above code selects the parts of the shapes that are inside the rectangle. If you wish to select entire
 *   shapes, as long as any part of it touches the selection-box, you can provide a boolean as second parameter:
 *   {{{
 *     val rectangle = Rectangle2D(0, 0, 10, 10)
 *     Select(rectangle, true)
 *   }}}
 * </p>
 * <h4>Toggle-select</h4>
 * <p>
 *   It is also possible to toggle-select a part of a shape (that is select parts that are not selected and deselect
 *   parts that are selected) via the [[com.siigna.app.model.selection.SelectToggle]] object. Similarly you can
 *   deselect shapes via the [[com.siigna.app.model.selection.Deselect]] object. The same methods apply as above
 *   (select with point, rectangle and ids), but via the SelectToggle object instead:
 *   {{{
 *     val point = Vector2D(42, math.Pi)
 *     SelectToggle(point)
 *   }}}
 * </p>
 * <h4>Deselecting</h4>
 * <p>
 *   Deselection works in the exact same way as [[com.siigna.app.model.selection.SelectToggle]] and
 *   [[com.siigna.app.model.selection.Select]], but by deselecting the result instead (surprise!). As simple deselection
 *   could then look like this:
 *   {{{
 *     val point = Vector2D(42, math.Pi)
 *     Deselect(point)
 *   }}}
 * </p>
 * <h2>Advanced examples</h2>
 * <p>
 *   Like other helper-objects (such as the objects in the [[com.siigna.app.model.action]] package), the
 *   [[com.siigna.app.model.selection.Select]], [[com.siigna.app.model.selection.Deselect]] and
 *   [[com.siigna.app.model.selection.SelectToggle]] helpers basically just forwards calls to the
 *   [[com.siigna.app.model.SelectableModel]] (with a few changes), to hide the complexity of the underlying
 *   implementation to the user.
 * </p>
 * <p>
 *   The above examples have used geometry (vectors and rectangles) to find the shapes to (de)select in the
 *   model. To (de)select specific shapes you can also use their ids (an Int) or find the shapes in the
 *   [[com.siigna.app.model.Model]] (accessible via the [[com.siigna.app.model.Drawing]]). It can be done in many ways,
 *   but the common is probably to use geometry to search for shapes close to a given
 *   [[com.siigna.util.geom.Vector2D]] or inside a given [[com.siigna.util.geom.Rectangle2D]].
 * {{{
 *   Drawing(Vector2D(0, 0))            // Map[Int, Shape]
 *   Drawing(Rectangle2D(0, 0, 10, 10)) // Map[Int, Shape]
 * }}}
 *   Note that the return-type is a map of Ints (identifiers) and shapes. These maps can be passed directly on to the
 *   [[com.siigna.app.model.selection.Select]], [[com.siigna.app.model.selection.Deselect]] and
 *   [[com.siigna.app.model.selection.SelectToggle]] objects which then will call the appropriate methods in the
 *   [[com.siigna.app.model.SelectableModel]]. Since the drawing basically is a map of Int -> Shape, you can
 *   also, however, search through it using all the regular collection-methods provided by scala and pass the
 *   result to the helper-objects.
 * </p>
 * <p>
 *   For a further description of these methods, see the [[com.siigna.app.model.Drawing]],
 *   [[com.siigna.app.model.SpatialModel]] or [[com.siigna.app.model.Model]].
 * </p>
 * <h2>Defining your own selection</h2>
 * <p>
 *   If you wish to specify a different behavior for a selection than [[com.siigna.app.model.selection.Select]],
 *   [[com.siigna.app.model.selection.Deselect]] and [[com.siigna.app.model.selection.SelectToggle]] provides,
 *   you can go straight to the source and manipulate the [[com.siigna.app.model.SelectableModel]]. See the
 *   [[com.siigna.app.model.SelectableModel]] for more details.
 * </p>
 *
 * @see [[com.siigna.app.model.selection.SelectToggle]]
 * @see [[com.siigna.app.model.selection.Deselect]]
 * @see [[com.siigna.app.model.Model]]
 * @see [[com.siigna.app.model.SpatialModel]]
 * @see [[com.siigna.app.model.SelectableModel]]
 * @see [[com.siigna.app.model.shape.Shape]]
 * @see [[com.siigna.app.model.selection.Selection]]
 * @see [[com.siigna.app.model.selection.ShapeSelector]]
 */
object SelectToggle {

  /**
   * Toggles the [[com.siigna.app.model.shape.Shape]] with the given id in the current selection by removing it if it
   * is fully selected, or adding it to the selection if it is not already selected.
   * @param id  The id of the shape.
   * @param drawing  The [[com.siigna.app.model.Drawing]] to perform the selection upon.
   */
  def apply(id : Int)(implicit drawing : Drawing) {
    drawing.selection = drawing.selection.get(id) match {
      case Some((_, FullShapeSelector)) => drawing.selection.remove(id)
      case _ => drawing.get(id) match {
        case Some(s) => drawing.selection.add(id, s -> FullShapeSelector)
        case _ => drawing.selection
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
   * @param drawing  The [[com.siigna.app.model.Drawing]] to operate on.
   */
  def apply(id : Int, selector : ShapeSelector)(implicit drawing : Drawing) {
    drawing.selection = drawing.selection.get(id) match {
      // Completely remove the selection if all the points already are toggled
      case Some((_, s : BitSetShapeSelector)) if (s.contains(selector)) => drawing.selection.remove(id, selector)
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

              // Add the index if the id has any neighbors or if it does not exist already
              if ((xs(i - 1) || xs(i + 1) || (isClosedPolyline && (i == xs.last || i == xs.head))) || !xs(i)) xs + i
              // Remove the index if no neighbors were found
              else xs - i
            })
            println("SelectToggle on: " + ShapeSelector(xs))
            Selection(drawing.selection.updated(id, drawing.selection(id)._1 -> ShapeSelector(xs)))
          }
          case EmptyShapeSelector => {
            println("Select toggle 2222222222")
            drawing.selection
          }
          case _ => {
            println("Select toggle 3333333")
            drawing.selection.remove(id)
          }
        }
      }
      // Simple cases:
      case Some((_, FullShapeSelector)) => drawing.selection.remove(id)
      case Some((shape, x)) => drawing.selection.add(id, shape -> (x ++ selector))
      case _ => drawing.selection.add(id, drawing(id) -> selector)
    }
  }



  /**
   * Toggles the [[com.siigna.app.model.shape.Shape]]s with the given ids in the current selection by removing them
   * if they already are selected, or adding them to the selection if they are not.
   * @param ids  The ids of the shape.
   * @param drawing  The [[com.siigna.app.model.Drawing]] to perform the selection upon.
   */
  def apply(ids : Traversable[Int])(implicit drawing : Drawing) {
    // Find the ids that are included in the selection already, and those that are not
    val (included, excluded) = ids.partition(i => drawing.selection.contains(i))
    val mapped = excluded.toSeq.map(id => id -> (drawing(id) -> FullShapeSelector)).toMap
    // Then remove the shapes that are in the selection, and add the ones that are not
    drawing.selection = drawing.selection.remove(included).add(mapped)
  }

  /**
   * Toggles the [[com.siigna.app.model.selection.ShapeSelector]]s of the given [[com.siigna.app.model.shape.Shape]]s
   * in the current selection by removing the parts from the shapes if they already have been selected, or
   * adding them to the selection if they are not already selected.
   * @param parts  The ids of the shapes mapped to the part to toggle from the selection.
   * @param drawing  The [[com.siigna.app.model.Drawing]] to perform the selection upon.
   */
  def apply(parts : Map[Int, ShapeSelector])(implicit drawing : Drawing) {
    /*// Find the parts that are included in the selection already, and those that are not
    val (included, excluded) = parts.partition(t => selection.contains(t._1))
    // Remove the included selectors and add the excluded
    selection = selection.remove(included).add(excluded.map(t => t._1 -> (drawing(t._1) -> t._2)))
    selection
    */
    // TODO: Optimize
    parts.foreach(t => apply(t._1, t._2))
  }
  /**
   * Toggles the [[com.siigna.app.model.shape.Shape]]s found close to the given [[com.siigna.util.geom.Vector2D]] by
   * the rules specified in the documentation for [[com.siigna.app.model.selection.SelectToggle]]. Basically we
   * we toggle the selections by removing the parts from the shapes if they already have been selected, or adding them
   * to the selection if they are not already selected.
   * @param point  The point (Vector2D) to use to find shapes closer than [[com.siigna.app.Siigna#selectionDistance]]
   *               to the given point.
   * @param drawing  The [[com.siigna.app.model.Drawing]] to perform the selection upon.
   */
  def apply(point : Vector2D)(implicit drawing : Drawing) {
    val shapes = drawing(point)
    val selection = shapes.map(t => t._1 -> t._2.getSelector(point))
    if (!selection.isEmpty) apply(selection)
  }

  /**
   * Toggles the [[com.siigna.app.model.shape.Shape]]s inside the given [[com.siigna.util.geom.SimpleRectangle2D]] by
   * the rules specified in the documentation for [[com.siigna.app.model.selection.SelectToggle]]. Basically we
   * we toggle the selections by removing the parts from the shapes if they already have been selected, or adding them
   * to the selection if they are not already selected.
   * @param rectangle  The rectangle to use as bounding box for the shapes in the [[com.siigna.app.model.Model]], all
   *                   shapes inside it are included.
   * @param entireShapes  If set to true we toggle-select entire shapes as soon as they touch the rectangle, if
   *                      false we only select the parts that are inside the rectangle.
   * @param drawing  The [[com.siigna.app.model.Drawing]] to perform the selection upon.
   */
  def apply(rectangle : SimpleRectangle2D, entireShapes : Boolean = false)(implicit drawing : Drawing) {
    val shapes = if (!entireShapes) {
      drawing(rectangle).map(t => t._1 -> t._2.getSelector(rectangle))
    } else {
      // TODO: Write a method that can take t._2.geometry and NOT it's boundary...
      drawing(rectangle).collect {
        case t if (rectangle.intersects(t._2.geometry.boundary)) => {
          (t._1 -> FullShapeSelector)
        }
      }
    }

    apply(shapes)
  }

}
