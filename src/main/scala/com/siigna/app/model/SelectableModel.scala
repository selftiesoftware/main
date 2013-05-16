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

package com.siigna.app.model

import com.siigna.app.model.action.{TransformShapeParts, Action, AddAttributes}
import com.siigna.app.model.selection._
import com.siigna.util.geom.SimpleRectangle2D
import com.siigna.app.model.shape.PolylineShape.PolylineShapeClosed

/**
 * <p>
 *   A trait that interfaces with the [[com.siigna.app.model.selection.Selection]] of the model (or as we call it
 *   internally, the "dynamic" or "mutable" layer) that, if not empty, represents parts or subsets of one or more
 *   selected shapes.
 *</p>
 *
 * <p>
 *   The SelectableModel is a way to indirectly manipulate the underlying [[com.siigna.app.model.ImmutableModel]]
 *   through the temporary "dynamic layer" that can be changed and updated without any effect on the actual
 *   shapes, but with visual feedback to the user. This can be very useful (and gives enormous performance benefits)
 *   when you need to alter shapes many times before storing any final changes.
 * </p>
 *
 * <p>
 *   To create a selection the <code>select</code> method should be used. This method creates a selection of the given
 *   shapes and [[com.siigna.app.model.selection.ShapeSelector]]s and adds it to the current selection, if any. The
 *   manipulations done by the user are not stored before it has been deselected via the method <code>deselect</code>.
 *   The method collects all the changes stored in the selection and applies them to the model.
 * </p>
 *
 * <h2>Use cases</h2>
 * <p>
 *   If a users finds any shapes he/she would like to manipulate, they can be altered many times via the
 *   [[com.siigna.app.model.selection.Selection]], giving the user a chance to see the output of the transformations
 *   before storing them. The first example covers a simple way to retrieve shapes from the
 *   [[com.siigna.app.model.Drawing]], setting their attributes and moving them 100 units to the right. Afterwards
 *   we <code>deselect</code> the selection, to store the changes permanently.
 * </p>
 * {{{
 *   // Get some random shapes from the Drawing (this finds shapes close to (0, 0))
 *   val shapes = Drawing( Vector2D( 0, 0) )
 *
 *   // Select them
 *   Drawing.select(shapes)
 *
 *   // Set the color to white
 *   Drawing.selection.addAttribute("Color" -> "#FFFFFF".color)
 *
 *   // Move the selection 100 units to the right
 *   Drawing.selection.transform( TransformationMatrix( Vector2D(100, 0) ) )
 *
 * }}}
 *
 * @see [[com.siigna.app.model.Drawing]], [[com.siigna.app.model.Model]], [[com.siigna.app.model.selection.Selection]],
 *     [[com.siigna.app.model.selection.ShapeSelector]]
 */
trait SelectableModel {

  /**
   * The MutableModel on which the selections can be performed.
   */
  protected def model : Model

  /**
   * Deletes the current selection without applying the changes to the [[com.siigna.app.model.Drawing]]. This can
   * be used if the user regrets the changes and wishes to annul the selection instead of saving it. Synonym to
   * setting the selection to [[com.siigna.app.model.selection.Selection.empty]].
   * @return  The new (empty) [[com.siigna.app.model.selection.Selection]].
   */
  def clearSelection() = {
    selection = Selection.empty
    selection
  }

  /**
   * Deselects the entire [[com.siigna.app.model.selection.Selection]] in the Model by setting the selection to None,
   * and applies the changes executed on the selection since it was selected.
   * <br>
   * This is import to remember so the changes from the selection can be made into
   * [[com.siigna.app.model.action.Action]]s that can be saved in the [[com.siigna.app.model.Model]].
   * @return  The new (empty) selection.
   */
  def deselect() : Selection = {
    val action = getChanges(selection)
    selection = Selection.empty
    action.foreach(Drawing execute _)
    selection
  }

  /**
   * Deselects the [[com.siigna.app.model.shape.Shape]] with the given id by removing it from the current selection.
   * If the shape does not exist in the selection, nothing happens. The remaining selection - along with the changes
   * made to it so far - persists.
   * @param id  The id of the shape.
   * @return  The new selection with the shape removed if it existed in the selection, otherwise the same selection.
   */
  def deselect(id : Int) : Selection = {
    selection = selection.remove(id)
    selection
  }

  /**
   * Deselects the [[com.siigna.app.model.shape.Shape]]s with the given ids by removing them from the current selection.
   * If one or more shapes does not exist in the selection, they are not removed. The remaining selection - along with
   * the changes made to it so far - persists.
   * @param ids The id of the shapes to remove from the selection.
   * @return  The new selection with the shapes removed if they existed in the selection.
   */
  def deselect(ids : Traversable[Int]) : Selection = {
    selection = selection.remove(ids)
    selection
  }

  /**
   * Removes a sub-part of a [[com.siigna.app.model.shape.Shape]] with the given id by subtracting the sub-part
   * (represented by a [[com.siigna.app.model.selection.ShapeSelector]]) from the current selection. If the shape
   * does not feature in the selection or the subtraction results in an empty selector (a
   * [[com.siigna.app.model.selection.EmptyShapeSelector]]), the entire shape is removed from the selection.
   * @param id  The id of the shape to remove the part from.
   * @param selector  The selector to remove from the shape.
   * @return  A new [[com.siigna.app.model.selection.Selection]] with the part of the shape removed.
   */
  def deselect(id : Int, selector : ShapeSelector) : Selection = {
    selection = selection.remove(id, selector)
    selection
  }

  /**
   * Removes a sub-part of each [[com.siigna.app.model.shape.Shape]] with the given ids by subtracting the sub-part
   * (represented by a [[com.siigna.app.model.selection.ShapeSelector]]) paired with the id from the current selection.
   * If the shape does not feature in the selection or the subtraction results in an empty selector (a
   * [[com.siigna.app.model.selection.EmptyShapeSelector]]), the entire shape is removed from the selection.
   * @param parts  The ids of the shapes to subtract parts from, paired with the part to subtract.
   * @return  A new [[com.siigna.app.model.selection.Selection]] with the parts of the shapes removed.
   */
  def deselect(parts : Map[Int, ShapeSelector]) : Selection = {
    selection = selection.remove(parts)
    selection
  }

  def deselect(rectangle : SimpleRectangle2D, entireShapes : Boolean) : Selection = {
    val shapes = if (!entireShapes) {
      model(rectangle).map(t => t._1 -> t._2.getSelector(rectangle))
    } else {
      // TODO: Write a method that can take t._2.geometry and NOT it's boundary...
      model(rectangle).collect {
        case t if (rectangle.intersects(t._2.geometry.boundary)) => {
          (t._1 -> FullShapeSelector)
        }
      }
    }

    selection = selection.remove(shapes)
    selection
  }

  /**
   * Retrieves the changes made upon the given selection as [[com.siigna.app.model.action.Action]]s, if any changes
   * are found.
   * @param selection The [[com.siigna.app.model.selection.Selection]] containing changes to be executed.
   * @return  Some[Action] if any changes were found, None otherwise
   */
  def getChanges(selection : Selection) : Option[Action] = {
    selection match {
      case s : NonEmptySelection => {
        // Find the resulting transformation, if any
        val transform = if (!s.transformation.isEmpty) {
          Some(TransformShapeParts(selection.map(t => t._1 -> t._2._2), s.transformation))
        } else None

        // Find the resulting attributes, if any
        val attributes = if (!s.attributes.isEmpty) {
          Some(new AddAttributes(selection.map(t => t._1 -> t._2._1.attributes), s.attributes))
        } else None

        (transform, attributes) match {
          case (Some(t), Some(a)) => Some(t merge a)
          case (Some(t), None) => Some(t)
          case (None, Some(a)) => Some(a)
          case _ => None
        }
      }
      case _ => None
    }
  }

  /**
   * Selects an entire shape based on its id. Equivalent to calling select(id, FullShapePart)
   * @param id  The id of the shape to select.
   * @throws  NoSuchElementException  If the shape with the given ID did not exist in the [[com.siigna.app.model.Model]].
   * @return  The new selection with the added shape.
   */
  def select(id : Int) : Selection = {
    select(id, FullShapeSelector)
    selection
  }

  /**
   * Selects several whole shapes based on their ids.
   * @param ids  The id's of the shapes to select.
   * @throws NoSuchElementException If one or more of the ids could not be found in the [[com.siigna.app.model.Model]].
   * @return  The new selection with the added shapes.
   */
  def select(ids : Traversable[Int]) : Selection = {
    val shapes = ids.map(i => i -> Drawing(i))

    if (!shapes.isEmpty) {
      // First retrieve the changes
      val action = getChanges(selection)

      // Then create a new selection
      selection = selection.add(shapes.map(s => s._1 -> (s._2 -> FullShapeSelector)).toMap)

      // .. And lastly execute the changes
      action.foreach(a => Drawing execute a)
    }

    selection
  }

  /**
   * Selects a part of a shape based on its id. If the ShapePart is a FullShapePart then the
   * entire shape is selected, if the part is empty or no shape with the given id could be found in the model,
   * nothing happens.
   * @param id  The id of the shape
   * @param selector  The selector of the shape describing how the shape should be selected.
   * @return  The new selection after the selection.
   */
  def select(id : Int, selector : ShapeSelector) : Selection = {
    Drawing.get(id) match {
      case Some(s) => selection = selection.add(id, s -> selector)
      case _ =>
    }
    selection
  }

  /**
   * Add the given selection to the current selection by merging the already selected shape-parts with the given
   * shape-parts.
   * @param selection  The Selection representing the selection to add to the current selection.
   * @return  The new active selection.
   */
  def select(selection : Selection) : Selection = {
    this.selection = this.selection.add(selection)
    this.selection
  }

  /**
   * Select every [[com.siigna.app.model.shape.Shape]] in the [[com.siigna.app.model.Drawing]].
   * @return  The new selection, containing all the shapes in the model.
   */
  def selectAll() = {
    selection = Selection(model.shapes.map(i => i._1 -> (i._2 -> FullShapeSelector)))
    selection
  }

  /**
   * The current selection represented by a [[com.siigna.app.model.selection.Selection]] where shapes can and can not
   * be set.
   * @return  Some[Selection] if a selection is active or None if nothing has been selected
   */
  def selection : Selection = model.selection

  /**
   * Overrides (removes) the current selection and sets it to the given selection instead. This will remove all
   * the changes made to the previous selection.
   * @param selection  The new selection to use.
   */
  def selection_=(selection : Selection) : Selection = { model.selection = selection; selection }

  /**
   * Toggles the [[com.siigna.app.model.shape.Shape]] with the given id in the current selection by removing it if it
   * is fully selected, or adding it to the selection if it is not already selected.
   * @param id  The id of the shape.
   * @return  The new selection either with the shape added or removed, depending on its appearance in
   *          the previous selection.
   */
  def toggleSelect(id : Int) : Selection = {
    selection = selection.get(id) match {
      case Some((_, FullShapeSelector)) => selection.remove(id)
      case _ => selection.add(id, Drawing(id) -> FullShapeSelector)
    }
    selection
  }

  /**
   * Toggles the selector of the given [[com.siigna.app.model.shape.Shape]]
   * in the current selection by removing the entire shape if it is fully selected, removing the part if it is
   * selected partially, or adding it to the selection if it is not already selected.
   * @param id  The id of the shape.
   * @param selector  The selector describing the part of the shape to toggle.
   * @return  The new selection either with the shape added or removed, depending on its appearance in
   *          the previous selection. If the shape already existed in the previous selection and the subtraction of
   *          the selector results in an [[com.siigna.app.model.selection.EmptyShapeSelector]], the shape is
   *          completely removed from the selection.
   */
  def toggleSelect(id : Int, selector : ShapeSelector) : Selection = {
    selection = selection.get(id) match {
      // Completely remove the selection if all the points already are toggled
      case Some((_, s : BitSetShapeSelector)) if (s.contains(selector)) => selection.remove(id, selector)
      // Otherwise we need to toggle the points if no neighbors exist
      case Some((shape, s : BitSetShapeSelector)) => {
        // If it's a closed polyline we need to treat it specially
        val isClosedPolyline = shape match {
          case _ : PolylineShapeClosed => true
          case _ => false
        }

        selector match {
          case BitSetShapeSelector(bits) => {
            val xs = bits.foldLeft(s.bits)((xs, i) => {
              // Add the index if the id has any neighbors
              if (xs(i - 1) || xs(i + 1) || (isClosedPolyline && (i == xs.last || i == xs.head))) xs + i
              // Remove the index if no neighbors were found
              else xs - i
            })
            selection = Selection(selection.updated(id, selection(id)._1 -> ShapeSelector(xs)))
            selection
          }
          case EmptyShapeSelector => selection
          case _ => selection.remove(id)
        }
      }
      // Simple cases:
      case Some((_, FullShapeSelector)) => selection.remove(id)
      case Some((shape, x)) => selection.add(id, shape -> (x ++ selector))
      case _ => selection.add(id, Drawing(id) -> selector)
    }
    selection
  }

  /**
   * Toggles the [[com.siigna.app.model.shape.Shape]]s with the given ids in the current selection by removing them
   * if they already are selected, or adding them to the selection if they are not.
   * @param ids  The ids of the shape.
   * @return  The new selection either with the shapes added or removed, depending on their appearance in
   *          the previous selection.
   */
  def toggleSelect(ids : Traversable[Int]) : Selection = {
    // Find the ids that are included in the selection already, and those that are not
    val (included, excluded) = ids.partition(i => selection.contains(i))
    val mapped = excluded.toSeq.map(id => id -> (Drawing(id) -> FullShapeSelector)).toMap
    // Then remove the shapes that are in the selection, and add the ones that are not
    selection = selection.remove(included).add(mapped)
    selection
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
  def toggleSelect(parts : Map[Int, ShapeSelector]) : Selection = {
    /*// Find the parts that are included in the selection already, and those that are not
    val (included, excluded) = parts.partition(t => selection.contains(t._1))
    // Remove the included selectors and add the excluded
    selection = selection.remove(included).add(excluded.map(t => t._1 -> (Drawing(t._1) -> t._2)))
    selection
    */
    // TODO: Optimize
    parts.foreach(t => toggleSelect(t._1, t._2))
    selection
  }


}