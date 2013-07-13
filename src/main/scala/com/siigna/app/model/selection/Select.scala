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

import com.siigna.util.geom.Vector2D
import com.siigna.app.model.Drawing
import com.siigna.app.model.shape.Shape
import com.siigna.util.geom.SimpleRectangle2D

/**
 * <p>
 *   An object that provides shortcuts to selecting objects in the [[com.siigna.app.model.Model]]. Selections are a
 *   great tool to manipulate [[com.siigna.app.model.shape.Shape]]s, since they can provide dynamic
 *   manipulation without altering the model before the user deselcts the changes. A Selection works on everything
 *   ranging from subsets of a single shape to a large collection of whole shapes.
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
object Select {

  /**
   * Select every [[com.siigna.app.model.shape.Shape]] in the [[com.siigna.app.model.Drawing]].
   */
  def all() {
    Drawing.select(Selection(Drawing.shapes.map(i => i._1 -> (i._2 -> FullShapeSelector))))
  }

  /**
   * Selects one whole [[com.siigna.app.model.shape.Shape]] given by its id. If no shape could be found, nothing
   * happens.
   * @param id  The id of the shape to select.
   */
  def apply(id : Int) {
    Drawing.select(id)
  }

  /**
   * Selects the part of the [[com.siigna.app.model.shape.Shape]] (given by its id) that are close to the
   * given [[com.siigna.util.geom.Vector2D]]. If a shape with the given id could not be found or no part could be
   * found close to the point, nothing happens.
   * @param id  The id of the shape to select.
   * @param point  The point that decides which part of the shape to select.
   */
  def apply(id : Int, point : Vector2D) {
    Drawing.get(id) match {
      case Some(shape) => {
        shape.getSelector(point) match {
          case EmptyShapeSelector =>
          case x => Drawing select(id, shape, shape.getSelector(point))
        }
      }
      case _ =>
    }
  }

  /**
   * Selects the part of the [[com.siigna.app.model.shape.Shape]] (given by its id) that are intersected by or
   * inside the given [[com.siigna.util.geom.Rectangle2D]]. If a shape with the given id could not be found or no part
   * could be found close to the point, nothing happens.
   * @param id  The id of the shape to select.
   * @param rectangle  The rectangle to determine which part of the shape to select.
   */
  def apply(id : Int, rectangle : SimpleRectangle2D) {
    Drawing.get(id) match {
      case Some(shape) => {
        shape.getSelector(rectangle) match {
          case EmptyShapeSelector =>
          case x => Drawing select(id, shape, x)
        }
      }
      case _ =>
    }
  }

  /**
   * Selects a part of a shape based on its id. If the ShapePart is a FullShapePart then the
   * entire shape is selected, if the part is empty or no shape with the given id could be found in the model,
   * nothing happens.
   * @param id  The id of the shape
   * @param selector  The selector of the shape describing how the shape should be selected.
   */
  def apply(id : Int, selector : ShapeSelector) {
    Drawing.get(id) match {
      case Some(s) => apply(id, s, selector)
      case _ =>
    }
  }

  /**
   * Selects the part of the given [[com.siigna.app.model.shape.Shape]] with the given id that is described by
   * the given [[com.siigna.app.model.selection.ShapeSelector]].
   * @param id  The id of the shape to select.
   * @param shape  The shape to apply the selector to.
   * @param selector  The selector, describing which parts of the shape have been selected.
   */
  def apply(id : Int, shape : Shape, selector : ShapeSelector) {
    Drawing.select(id, shape, selector)
  }

  /**
   * Selects several [[com.siigna.app.model.shape.Shape]]s by their ids.
   * @param ids  The ids of the shapes to select.
   */
  def apply(ids : Int*) { apply(ids) }

  /**
   * Selects several [[com.siigna.app.model.shape.Shape]]s by their ids.
   * @param ids The ids of the shapes to select.
   */
  def apply(ids : Traversable[Int]) {
    Drawing.select(ids.map(i => i -> Drawing.get(i)).collect { case (i, Some(s)) => i -> s }.toMap)
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
   * Searches the [[com.siigna.app.model.Model]] for [[com.siigna.app.model.shape.Shape]]s close to the point and
   * selects the part that are close. If no selection could be made (no shapes are close or the point did not result in
   * any meaningful selections), nothing happens.
   * @param point  The point to use to search for shapes in the model and choose the parts to select.
   */
  def apply(point : Vector2D) {
    apply(Drawing(point), point)
  }

  /**
   * Selects the [[com.siigna.app.model.shape.Shape]]s that are inside the given
   * [[com.siigna.util.geom.SimpleRectangle2D]]. If the <code>entireShapes</code> value is set to true, we select a
   * shape entirely as soon as it touches the rectangle, otherwise we just select the included vertices.
   *
   * @param rectangle  The rectangle to use as bounding box for the shapes in the [[com.siigna.app.model.Model]], all
   *                   shapes inside it are included.
   * @param entireShapes  If set to true we select entire shapes as soon as they touch the rectangle, if
   *                      false we only select the parts that are inside the rectangle.
   */
  def apply(rectangle : SimpleRectangle2D, entireShapes : Boolean = false)  {
    val shapes = if (!entireShapes) {
      Drawing(rectangle).map(t => t._1 -> (t._2 -> t._2.getSelector(rectangle)))
    } else {
      Drawing(rectangle).collect {
        case t if rectangle.intersects(t._2.geometry) || rectangle.contains(t._2.geometry) => {
          t._1 -> (t._2 -> FullShapeSelector)
        }
      }
    }
    Drawing.select(Selection(shapes))
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

}