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

import com.siigna.util.geom.SimpleRectangle2D
import com.siigna.app.model.Drawing

/**
 * An object that provides shortcuts to deselecting items in the [[com.siigna.app.model.Model]].
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
 */
object Deselect {

  /**
   * Deselects the entire [[com.siigna.app.model.Drawing]].
   */
  def apply() {
    Drawing.deselect()
  }

  /**
   * Searches for the [[com.siigna.app.model.shape.Shape]]s in the [[com.siigna.app.model.Drawing]] that is inside the
   * rectangle and selects them. If the <code>entireShapes</code> flag is enabled we select entire shapes even
   * though only a part of them touches the rectangle. If it is disabled we select
   * @param rectangle
   * @param entireShapes
   */
  def apply(rectangle : SimpleRectangle2D, entireShapes : Boolean) {
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

    Drawing.deselect(shapes)
  }


}
