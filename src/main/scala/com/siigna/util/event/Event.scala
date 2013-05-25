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

package com.siigna.util.event

/**
 * The basic interface for all events in the system
 */
trait Event
{
  
  /**
   * A symbolic name for a given event.
   */
  def symbol : Symbol

}

/**
 * ModifierKeys used to match further information in a given event.
 * Arguments are listed as follows: Shift - Control - Alt
 */
case class ModifierKeys(shift : Boolean, ctrl : Boolean, alt : Boolean)

/**
 * Companion object for [[com.siigna.util.event.ModifierKeys]].
 */
object ModifierKeys {

  /**
   * An instance of [[com.siigna.util.event.ModifierKeys]] where Shift is pressed.
   */
  val Shift = ModifierKeys(true, false, false)

  /**
   * An instance of [[com.siigna.util.event.ModifierKeys]] where Control is pressed.
   */
  val Control = ModifierKeys(false, true, false)

  /**
   * An instance of [[com.siigna.util.event.ModifierKeys]] where Alt is pressed.
   */
  val Alt = ModifierKeys(false, false, true)

}

