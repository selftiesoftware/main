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

package com.siigna.app.view

import java.awt.Color

import com.siigna.app.Siigna
import com.siigna.util.Implicits._

/**
 * The overall trait for displays.
 * TODO: Insert fade-effect: end-time - start-time 
 */
trait Display {

  /**
   * Paints on top of a given Graphics element.
   */
  def paint(graphics : Graphics)

  /**
   * Paints a default frame with a given width and height
   */
  def paintFrame(graphics : Graphics, width : Int, height : Int) {
    val center = Siigna.center
    graphics setColor new Color(0f, 0f, 0f, 0.1f)
    graphics.g.fillRoundRect(center.x.toInt - width / 2, center.y.toInt - height / 2, width, height, 20, 20)
    graphics setColor Color.white
  }

}
