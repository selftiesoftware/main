/*
 * Copyright (c) 2011. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.view

import java.awt.{Cursor}
import com.siigna.util.geom.TransformationMatrix

/**
 * An abstract class for an Interface that's able to connect to a view.
 */
abstract class ViewInterface {

  /**
   * The cursor of the current interface.
   */
  def cursor : Cursor

  /**
   * The active display, if any.
   */
  def display : Option[Display]

  /**
   * A boolean value that signals whether panning and zooming is active.
   */
  def navigation : Boolean

  /**
   * Paint active filters and displays.
   * <b>Note</b>: This method is being called by the view. There's no need for
   * you to call it, unless you want to paint at some specific intervals or you
   * know what you do.
   */
  def paint(graphics : Graphics, transformation : TransformationMatrix)

}
