/*
 * Copyright (c) 2012. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.util.event

import com.siigna.util.geom.Vector2D

/**
 * An overall trait for mouse buttons.
 */
trait MouseButton

case object MouseButtonNone   extends MouseButton
case object MouseButtonLeft   extends MouseButton
case object MouseButtonRight  extends MouseButton
case object MouseButtonMiddle extends MouseButton

/**
 * An overall trait for mouse events containing a position, a button and a key.
 */
trait MouseEvent extends Event {

  def position : Vector2D

  def button : MouseButton

  def keys : ModifierKeys

}

case class MouseEnter(position : Vector2D, button : MouseButton, keys : ModifierKeys) extends MouseEvent { val symbol = 'MouseEnter }
case class MouseExit (position : Vector2D, button : MouseButton, keys : ModifierKeys) extends MouseEvent { val symbol = 'MouseExit }
case class MouseDown (position : Vector2D, button : MouseButton, keys : ModifierKeys) extends MouseEvent { val symbol = 'MouseDown }
case class MouseUp   (position : Vector2D, button : MouseButton, keys : ModifierKeys) extends MouseEvent { val symbol = 'MouseUp }
case class MouseDrag (position : Vector2D, button : MouseButton, keys : ModifierKeys) extends MouseEvent { val symbol = 'MouseDrag }
case class MouseMove (position : Vector2D, button : MouseButton, keys : ModifierKeys) extends MouseEvent { val symbol = 'MouseMove }
case class MouseWheel(position : Vector2D, button : MouseButton, keys : ModifierKeys, wheel : Double) extends MouseEvent { val symbol = 'MouseWheel }

/**
 * Companion object to the [[com.siigna.util.event.MouseWheel]] event.
 */
object MouseWheel {
  def apply(wheel : Double)(position : Vector2D, button : MouseButton, keys : ModifierKeys) =
    new MouseWheel(position, button, keys, wheel)
}
