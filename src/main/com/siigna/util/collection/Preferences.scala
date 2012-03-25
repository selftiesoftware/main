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

package com.siigna.util.collection

import com.siigna.util.Implicits._
import java.awt.{Dimension, Color}
import com.siigna.app.view.View

/**
 * Preferences used by Siigna.
 * We've made an object since we need to store several values when the program is initialized.
 */
object Preferences extends scala.collection.mutable.HashMap[String, Any] {

  // Set initial values
  this += "antiAliasing"         -> true
  this += "backgroundTileSize"   -> 12
  this += "colorBackground"      -> "#F9F9F9".color
  this += "colorBackgroundLight" -> "#E9E9E9".color
  this += "colorBackgroundDark"  -> "#DADADA".color
  this += "colorDraw"            -> "#000000".color
  this += "colorSelected"        -> "#7777FF".color
  this += "colorUniverse"        -> "#DDDDDD".color
  this += "defaultScreenSize"    -> new Dimension(600, 400)
  this += "selectionDistance"    -> 5.0 * View.zoom

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
    case _ => defaultValue
  }

  /**
   * Returns a double preference.
   */
  def double(name : String) = apply(name).asInstanceOf[Double]

  /**
   * Returns a specific value as an option of a given type. If the value doesn't not exist the method returns None.
   */
  def get[T](key : String) : Option[T] = try {
    Some(apply(key).asInstanceOf[T])
  } catch {
    case _ => None
  }

  /**
   * Toggles a boolean preference on/off or creates the given attribute if not defined.
   * The default value is false unless stated otherwise.
   */
  def toggle(preference : String, defaultValue : Boolean = false) {
    val res = get(preference)
    if (res.isDefined && res.get.isInstanceOf[Boolean])
      update(preference, !res.get.asInstanceOf[Boolean])
    else
      update(preference, defaultValue)
  }

}