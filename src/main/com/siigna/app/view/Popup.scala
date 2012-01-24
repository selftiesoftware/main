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

import com.siigna.app.Siigna
import com.siigna.app.model.shape.TextShape
import com.siigna.util.Implicits._
import com.siigna.util.collection.Attributes
import com.siigna.util.geom.Vector
import java.awt.Color

/**
 * A popup that displays a string on the center of the screen, with pre-defined
 * fade functionality.
 */
class Popup(message : String) extends Display {

  /**
   * The color of the message text.
   */
  private var color = new Color(0, 0, 0, 255);

  /**
   * The time the display is "alive", i. e. not fading.
   */
  private var displayTime  = 1000

  /**
   * The time it takes for the display to fade in milliseconds.
   */
  private var fadeTime = 1000

  /**
   * The time the display was initiated.
   */
  private val startTime = System.currentTimeMillis()

  private def timeElapsed = System.currentTimeMillis() - startTime
  
  def isEnabled = (timeElapsed < displayTime + fadeTime)

  def paint(graphics : Graphics) {
    // Define the transparency for the message
    val alpha : Double =
      if (timeElapsed > displayTime && displayTime > 0)
        (fadeTime - timeElapsed + displayTime).toDouble / fadeTime
      else
        1

    // Retrieve the relative alpha in relation the the time
    // expired since startTime for the given color
    def setTransparency(color : Color) = new Color(color.getRed, color.getGreen, color.getBlue, (color.getAlpha * alpha).toInt)

    // Define the text shape, draw the frame and draw the text
    val text = TextShape(message, Siigna.center, 10, Attributes("TextAlignment" -> Vector(0.5, 0.5), "Color" -> setTransparency(color)))
    paintFrame(graphics, text.boundary.width.toInt + 40, text.boundary.height.toInt + 20, setTransparency(backgroundColor))
    graphics draw text
  }

  /**
   * Sets the color of the popup text.
   */
  def setColor(color : Color) {
    this.color = color
  }

  /**
   * Sets the time it takes for the display to fade.
   */
  def setFadeTime(ms : Int) {
    fadeTime = ms
  }

  /**
   * Sets the time the message is displayed before starting to fade.
   */
  def setDisplayTime(ms : Int) {
    displayTime = ms
  }

}
