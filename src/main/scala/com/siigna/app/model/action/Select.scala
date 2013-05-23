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
import com.siigna.app.Siigna

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
 *
 * <p>
 *   A Selection does not duplicate the shapes, but instead stores the information about which parts of the shapes are
 *   selected (the [[com.siigna.app.model.selection.ShapeSelector]]). If a user chooses to select only one point of
 *   a larger shape for instance, the selection has to support this.
 * </p>
 *
 * <p>
 *   Like all objects in the <code>com.siigna.app.model.action</code> package, the
 *   [[com.siigna.app.model.action.Select]], [[com.siigna.app.model.action.Deselect]] and
 *   [[com.siigna.app.model.action.SelectToggle]] helpers basically just forwards calls to the
 *   [[com.siigna.app.model.SelectableModel]] (with a few changes), to hide the complexity of the underlying
 *   implementation to the user.
 * </p>
 *
 * <h2>Selection examples</h2>
 * <h4>Select shapes close to a point</h4>
 * <p>
 *   To select the shapes that are close to a given point, you simply need to provide a point like so:
 *   <pre>
 *     val point = Vector2D(3.1415, 1.16)
 *     Select(point)
 *   </pre>
 * </p>
 * <h4>Select shapes inside a selection-box</h4>
 * <p>
 *   To select shapes (or parts of them) that are inside a selection-box, simply provide a
 *   [[com.siigna.util.geom.Rectangle2D]] like so:
 *   <pre>
 *     val rectangle = Rectangle2D(0, 0, 10, 10)
 *     Select(rectangle)
 *   </pre>
 *   The above code selects the parts of the shapes that are inside the rectangle. If you wish to select entire
 *   shapes, as long as any part of it touches the selection-box, you can provide a boolean as second parameter:
 *   <pre>
 *     val rectangle = Rectangle2D(0, 0, 10, 10)
 *     Select(rectangle, true)
 *   </pre>
 * </p>
 * <p>
 *   It is also possible to toggle the selection (that is select that parts that are not selected and deselect the
 *   parts that are selected) via the [[com.siigna.app.model.action.SelectToggle]] object. Similarly you can
 *   deselect shapes via the [[com.siigna.app.model.action.Deselect]] object.
 * </p>
 *
 * <h2>Advanced examples</h2>
 * <p>
 *   To (de)select shapes you need to retrieve the shapes from the [[com.siigna.app.model.Model]] (done via the
 *   [[com.siigna.app.model.Drawing]]). It can be done in many ways, but the most common is to search for shapes
 *   close to a given [[com.siigna.util.geom.Vector2D]] or inside a given [[com.siigna.util.geom.Rectangle2D]] like so:
 * <pre>
 *   Drawing(Vector2D(0, 0))            // Map[Int, Shape]
 *   Drawing(Rectangle2D(0, 0, 10, 10)) // Map[Int, Shape]
 * </pre>
 *   For a further description of the methods, see the [[com.siigna.app.model.SpatialModel]] or
 *   [[com.siigna.app.model.Drawing]].
 * </p>
 *
 * @see [[com.siigna.app.model.action.SelectToggle]]
 * @see [[com.siigna.app.model.action.Deselect]]
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
   * @return  The new selection, containing all the shapes in the model.
   */
  def all() {
    Drawing.select(Selection(Drawing.shapes.map(i => i._1 -> (i._2 -> FullShapeSelector))))
  }

  /**
   * Selects one whole [[com.siigna.app.model.shape.Shape]].
   * @param id  The ID of the shape to select.
   */
  def apply(id : Int) {
    Drawing.get(id) match {
      case Some(s) => Drawing.select(id, s)
      case _ =>
    }
  }

  def apply(id : Int, point : Vector2D) {
    val shape = Drawing(id)
    Drawing select Selection(id -> (shape -> shape.getSelector(point)))
  }

  def apply(id : Int, r : SimpleRectangle2D) {
    val shape = Drawing(id)
    Drawing select(id, shape, shape.getSelector(r))
  }

  /**
   * Selects several [[com.siigna.app.model.shape.Shape]]s.
   * @param ids  The id's of the shapes to select.
   */
  def apply(ids : Int*) { apply(ids:_*) }

  /**
   * Selects several [[com.siigna.app.model.shape.Shape]]s.
   * @param ids The shapes to select.
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
   * Selects a part of a shape based on its id. If the ShapePart is a FullShapePart then the
   * entire shape is selected, if the part is empty or no shape with the given id could be found in the model,
   * nothing happens.
   * @param id  The id of the shape
   * @param selector  The selector of the shape describing how the shape should be selected.
   * @return  The new selection after the selection.
   */
  def apply(id : Int, selector : ShapeSelector) {
    Drawing.get(id) match {
      case Some(s) => Drawing.selection = Drawing.selection.add(id, s -> selector); Drawing.selection
      case _ => Drawing.selection
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
   *
   * @param rectangle
   * @param entireShapes
   */
  def apply(rectangle : SimpleRectangle2D, entireShapes : Boolean = false) {
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