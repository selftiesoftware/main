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

package com.siigna.util.collection

import java.awt.Color

import com.siigna.util.Implicits._

/**
 * Preferences used by Siigna.
 * We've made an object since we need to store several values when the program is initialized.
 */

object Preferences extends scala.collection.mutable.ListMap[String, Any] {

  /**
   * Set initial values
   */
  this += "anti-aliasing"   -> true
  this += "colorBackground" -> "#F9F9F9".color
  this += "colorDraw"       -> "#000000".color
  this += "colorSelected"   -> "#7777FF".color
  this += "colorUniverse"   -> "#DDDDDD".color

  /**
   * Returns a boolean preference. Unless anything else is specified the defaulting value is set to true.
   */
  def boolean(name : String, defaultValue : Boolean = true) = try {
      this(name).asInstanceOf[Boolean]
    } catch {
      case _ => defaultValue
    }

  /**
   * Returns a colour preference. Unless anything else is specified the defaulting value is set to black.
   */
  def color(name : String, defaultValue : Color = "#000000".color) = try {
    this(name).asInstanceOf[Color]
  } catch {
    case _ =>
  }

  /**
   * Toggles a boolean preference on/off or creates the given attribute if not defined.
   * The default value is false unless stated otherwise.
   */
  def toggle(preference : String, defaultValue : Boolean = false) : Unit = {
    val res = get(preference)
    if (res.isEmpty)
      update(preference, defaultValue)
    else if (res.get == false)
      update(preference, true)
    else update(preference, false)
  }

}