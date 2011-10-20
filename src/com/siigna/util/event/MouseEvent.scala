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

package com.siigna.util.event

import com.siigna.util.geom.Vector

/**
 * A trait for mouse-buttons. Use it to match on different mouse-buttons.
 */
trait MouseButton
final case object MouseButtonNone   extends MouseButton
final case object MouseButtonLeft   extends MouseButton
final case object MouseButtonRight  extends MouseButton
final case object MouseButtonMiddle extends MouseButton

/**
 * The upmost event for mouse-interactions in the type-hierarchy.
 */
trait MouseEvent extends Event
{

  /**
   * The position of the mouse as a vector.
   */
  def position : Vector

  /**
   * Stores which mouse-buttons currently are pressed.
   */
  def button : MouseButton

  /**
   * Stores which keyboard-buttons currently are pressed.
   */
  def keys : ModifierKeys

}

case class MouseEnter(position : Vector, button : MouseButton, keys : ModifierKeys) extends MouseEvent { val symbol = 'MouseEnter }
case class MouseExit (position : Vector, button : MouseButton, keys : ModifierKeys) extends MouseEvent { val symbol = 'MouseExit }
case class MouseDown (position : Vector, button : MouseButton, keys : ModifierKeys) extends MouseEvent { val symbol = 'MouseDown }
case class MouseUp   (position : Vector, button : MouseButton, keys : ModifierKeys) extends MouseEvent { val symbol = 'MouseUp }
case class MouseDrag (position : Vector, button : MouseButton, keys : ModifierKeys) extends MouseEvent { val symbol = 'MouseDrag }
case class MouseMove (position : Vector, button : MouseButton, keys : ModifierKeys) extends MouseEvent { val symbol = 'MouseMove }
case class MouseWheel(position : Vector, button : MouseButton, keys : ModifierKeys, wheel : Int) extends MouseEvent { val symbol = 'MouseWheel }

object MouseWheel
{
  def apply(wheel : Int)(position : Vector, button : MouseButton, keys : ModifierKeys) =
    new MouseWheel(position, button, keys, wheel)
}
