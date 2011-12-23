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

package com.siigna.app.view.event

/**
 * The basic interface for all events in the system
 */
trait Event
{
  
  /**
   * A symbolic name for a given event.
   */
  val symbol : Symbol

}

/**
 * ModifierKeys used to match further information in a given event.
 * Arguments are listed as follows: Shift - Control - Alt
 */
case class ModifierKeys(shift : Boolean, ctrl : Boolean, alt : Boolean)

