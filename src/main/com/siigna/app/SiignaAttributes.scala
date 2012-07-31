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

package com.siigna.app

import com.siigna.util.collection.AttributesLike
import collection.mutable.{Map}
import java.awt.Dimension
import com.siigna.util.Implicits._

/**
 * <p>Attributes such as selection distance, anti-aliasing etc. for the Siigna application
 * accessible via the [[com.siigna.app.Siigna]] object.
 * </p>
 * <p>The class stores a number of pre-set values into a mutable map which can be changed actively
 * while Siigna is running.
 * </p>
 * <p>Currently the following values are set:
 * <dl>
 *  <dt>antiAliasing</dt>
 *    <dd>A boolean value signalling if anti-aliasing should be on for the shapes in the Model.
 *        The modules are always drawn with anti-aliasing. Defaults to true.</dd>
 *  <dt>backgroundTileSize</dt>
 *    <dd>The size of the square tiles drawn behind the actual drawable canvas, given in pixels. Defaults to 12.</dd>
 *  <dt>colorBackground</dt>
 *    <dd>The background color for the drawable canvas. Defaults to #F9F9F9 in hex.</dd>
 *  <dt>colorBackgroundLight</dt>
 *    <dd>The background color for the light squares in the background checkers-pattern. Defaults to #E9E9E9 in hex.</dd>
 *  <dt>colorBackgroundDark</dt>
 *    <dd>The background color for the dark squares in the background checkers-pattern. Defaults to #DADADA in hex.</dd>
 *  <dt>colorDraw</dt>
 *    <dd>The color every shapes are drawn with by default. Defaults to #000000 (black) in hex.</dd>
 *  <dt>colorSelected</dt>
 *    <dd>The color given to selected elements. Defaults to #7777FF in hex.</dd>
 *  <dt>defaultScreenSize</dt>
 *    <dd>The default size of the screen given as a [[java.awt.Dimension]]. Defaults to (600 x 400).</dd>
 *  <dt>printMargin</dt>
 *    <dd>The margin on pages when printing the content in Siigna, given in mm. Defaults to 13. </dd>
 *  <dt>printFormatMin</dt>
 *    <dd>The minimum format when printing, given in mm. Defaults to the width of a standard A4-size: 210.</dd>
 *  <dt>printFormatMax</dt>
 *    <dd>The maximum format when printing, given in mm. Defaults to the height of a standard A4-size: 297.</dd>
 *  <dt>selectionDistance</dt>
 *    <dd>The distance of which single-point selection happens, given in units. Defaults to 5.</dd> ,
 *  <dt>trackDistance</dt>
  *   <dd>Sensitivity of track.</dd>
 *  <dt>trackGuideColor</dt>
  *   <dd>The color to paint the track guides - the horizontal and vertical helper-lines when the user is "tasting" a point.</dd>
 *  <dt>zoomSpeed</dt>
 *    <dd>The speed with which the client zooms, given in percentages. Defaults to 0.5.</dd>
 * </dl>
 * </p>
 */
trait SiignaAttributes extends Map[String, Any] with AttributesLike {

  /**
   * The attributes of Siigna.
   */
  def self = toMap

  // Set the values
  this("antiAliasing")          = true
  this("backgroundTileSize")    = 12
  this("colorBackground")       = "#F9F9F9".color
  this("colorBackgroundLight")  = "#E9E9E9".color
  this("colorBackgroundDark")   = "#DADADA".color
  this("colorDraw")             = "#000000".color
  this("colorHover")            = "#22FFFF".color
  this("colorSelected")         = "#7777FF".color
  this("defaultScreenSize")     = new Dimension(600, 400)
  this("printMargin")           = 13.0
  this("printFormatMin")        = 210.0
  this("printFormatMax")        = 297.0
  this("selectionDistance")     = 5.0
  this("trackDistance")         = 3.0
  this("trackGuideColor")       = "#00FFFF".color
  this("zoomSpeed")             = 0.5
  
  /**
   * Toggles a boolean value or sets it to true if it does not exist. If there already is a
   * non-boolean value assigned to that name, nothing happens.
   */
  def toggle(key : String) {
    val bool = boolean(key)
    if (bool.isDefined) {
      update(key, !bool.get)
    } else if (!isDefinedAt(key)) {
      this.+(key -> true)
    }
  }

}
