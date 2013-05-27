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

package com.siigna.app.model.selection

import com.siigna.util.geom.SimpleRectangle2D
import com.siigna.app.model.Drawing

/**
 * An object that provides shortcuts to deselecting items in the [[com.siigna.app.model.Model]].
 */
object Deselect {

  /**
   * Searches for the [[com.siigna.app.model.shape.Shape]]s in the [[com.siigna.app.model.Drawing]] that is inside the
   * rectangle and selects them. If the <code>entireShapes</code> flag is enabled we select entire shapes even
   * though only a part of them touches the rectangle. If it is disabled we select
   * @param rectangle
   * @param entireShapes
   */
  def deselect(rectangle : SimpleRectangle2D, entireShapes : Boolean) {
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
