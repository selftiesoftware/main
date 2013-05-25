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

import com.siigna.app.model.action.{TransformShapeParts, Action, AddAttributes}
import com.siigna.app.model.selection._
import com.siigna.app.model.shape.Shape
import reflect.runtime.universe._
import com.siigna.util.Log

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
 * }}}
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
trait SelectableModel extends ActionModel {

  /**
   * Deletes the current selection without applying the changes to the [[com.siigna.app.model.Model]]. This can
   * be used if the user regrets the changes and wishes to annul the selection instead of saving it. Synonym to
   * setting the selection to [[com.siigna.app.model.selection.Selection.empty]] or simply executing an action
   * on the [[com.siigna.app.model.Model]] (since the selection is tied to the model, and the model is immutable).
   * @return  The [[com.siigna.app.model.SelectableModel]] containing the empty
   *          [[com.siigna.app.model.selection.Selection]].
   */
  def clearSelection() : SelectableModel  = {
    selection = Selection.empty
  }

  /**
   * Deselects the entire [[com.siigna.app.model.selection.Selection]] in the Model by setting the selection to None,
   * and applies the changes executed on the selection since it was selected.
   * <br>
   * This is import to remember so the changes from the selection can be made into
   * [[com.siigna.app.model.action.Action]]s that can be saved in the [[com.siigna.app.model.Model]].
   * @return  The [[com.siigna.app.model.SelectableModel]] containing the empty
   *          [[com.siigna.app.model.selection.Selection]].
   */
  def deselect() : SelectableModel = {
    selection = Selection.empty
  }

  /**
   * Deselects the [[com.siigna.app.model.shape.Shape]] with the given id by removing it from the current selection.
   * If the shape does not exist in the selection, nothing happens. The remaining selection - along with the changes
   * made to it so far - persists.
   * @param id  The id of the shape.
   * @return  The [[com.siigna.app.model.SelectableModel]] with the shape removed from the selection, if it existed
   *          in the selection to begin with, otherwise the same selection.
   */
  def deselect(id : Int) : SelectableModel = {
    selection = selection.remove(id)
  }

  /**
   * Deselects the [[com.siigna.app.model.shape.Shape]]s with the given ids by removing them from the current selection.
   * If one or more shapes does not exist in the selection, they are not removed. The remaining selection - along with
   * the changes made to it so far - persists.
   * @param ids The id of the shapes to remove from the selection.
   * @return  The [[com.siigna.app.model.SelectableModel]] with the shapes removed from the selection if they
   *          existed in it to begin with.
   */
  def deselect(ids : Traversable[Int]) : SelectableModel = {
    selection = selection.remove(ids)
  }

  /**
   * Removes a sub-part of a [[com.siigna.app.model.shape.Shape]] with the given id by subtracting the sub-part
   * (represented by a [[com.siigna.app.model.selection.ShapeSelector]]) from the current selection. If the shape
   * does not feature in the selection or the subtraction results in an empty selector (a
   * [[com.siigna.app.model.selection.EmptyShapeSelector]]), the entire shape is removed from the selection.
   * @param id  The id of the shape to remove the part from.
   * @param selector  The selector to remove from the shape.
   * @return  The [[com.siigna.app.model.SelectableModel]] with a selection where the part of the shape was removed.
   */
  def deselect(id : Int, selector : ShapeSelector) : SelectableModel = {
    selection = selection.remove(id, selector)
    this
  }

  /**
   * Removes a sub-part of each [[com.siigna.app.model.shape.Shape]] with the given ids by subtracting the sub-part
   * (represented by a [[com.siigna.app.model.selection.ShapeSelector]]) paired with the id from the current selection.
   * If the shape does not feature in the selection or the subtraction results in an empty selector (a
   * [[com.siigna.app.model.selection.EmptyShapeSelector]]), the entire shape is removed from the selection.
   * @param parts  The ids of the shapes to subtract parts from, paired with the part to subtract.
   * @return  A [[com.siigna.app.model.SelectableModel]] where the parts of the shapes removed have been removed from
   *          the selection.
   */
  def deselect(parts : Map[Int, ShapeSelector]) : SelectableModel = {
    selection = selection.remove(parts)
    this
  }

  /**
   * Retrieves the changes made upon the current selection as a single [[com.siigna.app.model.action.Action]], if any
   * changes are found.
   * @return  Some[Action] if any changes were found, None otherwise
   */
  def getChanges : Option[Action] = {
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
   * Selects a single [[com.siigna.app.model.shape.Shape]] from its id.
   * @param id  The id of the given shape in the [[com.siigna.app.model.Model]].
   * @return  The [[com.siigna.app.model.SelectableModel]] with the given shape added to the selection, if it exists
   *          in the [[com.siigna.app.model.Model]].
   */
  def select(id : Int) : SelectableModel =
    model.shapes.get(id).map(s => select(id, s, FullShapeSelector)).getOrElse(this)

  /**
   * Selects a single [[com.siigna.app.model.shape.Shape]] from its id with the given
   * [[com.siigna.app.model.selection.ShapeSelector]].
   * @param id  The id of the given shape in the [[com.siigna.app.model.Model]].
   * @param selector  The [[com.siigna.app.model.selection.ShapeSelector]] describing how the shape is to be selected.
   * @return  The [[com.siigna.app.model.SelectableModel]] with the given shape added to the selection, if it exists
   *          in the [[com.siigna.app.model.Model]].
   */
  def select(id : Int, selector : ShapeSelector) : SelectableModel =
    model.shapes.get(id).map(s => select(id, s, selector)).getOrElse(this)

  /**
   * Selects an entire [[com.siigna.app.model.shape.Shape]] based on its id. Equivalent to calling
   * select(id, FullShapePart)
   * @param id  The id of the shape to select.
   * @param shape  The shape to associate with the given id.
   * @throws  NoSuchElementException  If the shape with the given ID did not exist in the [[com.siigna.app.model.Model]].
   * @return  The [[com.siigna.app.model.SelectableModel]] with the shape added to the selection.
   */
  def select(id : Int, shape : Shape) : SelectableModel = select(id, shape, FullShapeSelector)

  /**
   * Selects a part of a shape based on its id. If the ShapePart is a
   * [[com.siigna.app.model.selection.FullShapeSelector]] then the entire shape is selected, if the part is empty
   * nothing happens.
   * @param id  The id of the shape
   * @param selector  The selector of the shape describing how the shape should be selected.
   * @return  The [[com.siigna.app.model.SelectableModel]] with the shape added to the selection.
   */
  def select(id : Int, shape : Shape, selector : ShapeSelector) : SelectableModel = {
    selection = selection.add(id, shape -> selector)
  }

  /**
   * Selects the [[com.siigna.app.model.shape.Shape]]s with the given ids in the model, if they exist.
   * @param ids The shapes to select.
   * @return  The [[com.siigna.app.model.SelectableModel]] with the shapes added to the selection.
   */
  def select(ids : Traversable[Int]) : SelectableModel = {
    select(ids.map(i => i -> Drawing.get(i)).collect { case (i, Some(s)) => (i -> (s -> FullShapeSelector)) }.toMap)
  }

  /**
   * Adds a the selection for each given [[com.siigna.app.model.shape.Shape]] (based on their ids) to the current
   * selection. If the ShapePart is a [[com.siigna.app.model.selection.FullShapeSelector]] then the entire shape
   * is selected, if the part is empty or no shape with the given id could be found in the model, nothing happens.
   * @param parts  The [[com.siigna.app.model.selection.ShapeSelector]]s which describes the selection of each shape,
   *               paired with the id of the shape and the shape itself.
   * @return  The [[com.siigna.app.model.SelectableModel]] with the shapes added to the selection.
   */
  def select[T : TypeTag](parts : Map[Int, T]) : SelectableModel = {
    typeOf[T] match {
      case x if x <:< typeOf[Shape] =>
        selection = selection.add(parts.asInstanceOf[Map[Int, Shape]].map(s => s._1 -> (s._2 -> FullShapeSelector)).toMap)
      case x if x <:< typeOf[(Shape, ShapeSelector)] =>
        selection = selection.add(parts.asInstanceOf[Map[Int, (Shape, ShapeSelector)]])
      case e => Log.warning("SelectableModel: Expected Map[Int, Shape] or Map[Int, (Shape, ShapeSelector)], got ", e); this
    }
  }

  /**
   * Add the given selection to the current selection by merging the already selected shape-parts with the given
   * shape-parts.
   * @param selection  The Selection representing the selection to add to the current selection.
   * @return  The [[com.siigna.app.model.SelectableModel]] with the old selection replaced with the new.
   */
  def select(selection : Selection) : SelectableModel = {
    this.selection = this.selection.add(selection)
  }

  /**
   * Select every [[com.siigna.app.model.shape.Shape]] in the [[com.siigna.app.model.Drawing]].
   * @return  The [[com.siigna.app.model.SelectableModel]] with all shapes in the model selected.
   */
  def selectAll() : SelectableModel = {
    selection = Selection(model.shapes.map(i => i._1 -> (i._2 -> FullShapeSelector)))
  }

  /**
   * The current selection represented by a [[com.siigna.app.model.selection.Selection]] describing which
   * [[com.siigna.app.model.shape.Shape]]s have been selected and how.
   * @return  An instance of a [[com.siigna.app.model.selection.Selection]] which might be empty.
   */
  def selection : Selection = model.selection

  /**
   * Overrides (removes) the current selection, setting it to the given selection instead. Before the override we store
   * the changes made to the selection and applies them to the model.
   * @param selection  The new selection to use instead of the previous.
   * @return  The [[com.siigna.app.model.SelectableModel]] with the new selection.
   */
  def selection_=(selection : Selection) : SelectableModel = {
    // First retrieve the changes
    val action = getChanges

    // Set the selection
    model.selection = selection

    // .. And lastly execute the changes (if any)
    action.foreach(execute(_))

    this
  }


}