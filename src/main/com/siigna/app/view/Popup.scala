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

import com.siigna.app.Siigna
import com.siigna.app.model.shape.TextShape
import com.siigna.util.Implicits._
import com.siigna.util.collection.Attributes
import com.siigna.util.geom.Vector

/**
 * A popup that displays a string on the center of the screen.
 *
 * TODO: Rename?
 */
case class Popup(message : String) extends Display {

  def paint(graphics : Graphics) {
    val text = TextShape(message, Siigna.center, 10, Attributes("TextAlignment" -> Vector(0.5, 0.5), "Color" -> "#000000".color))
    paintFrame(graphics, text.boundary.width.toInt + 40, text.boundary.height.toInt + 20)
    graphics draw text
  }

}
