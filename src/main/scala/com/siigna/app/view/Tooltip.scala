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

package com.siigna.app.view

import com.siigna.app.model.shape.TextShape
import com.siigna.util.collection.Attributes
import com.siigna.util.geom.Vector2D
import java.awt.Color
import com.siigna.app.Siigna

/**
 * A popup that displays a string on the center of the screen, with pre-defined
 * fade functionality.
 */
class Tooltip(strings : List[String]) extends Display {

  val isEnabled = if(Siigna.areTooltipsEnabled) true else false

  /**
   * The color of the message text.
   */
  private var color = new Color(0, 0, 0, 255)

  def paint(graphics : Graphics) {
    //LINE 1
    if (strings.length > 0 && !strings(0).isEmpty) {
      // Define the transparency for the message
      val alpha : Double = 0.75

      // Retrieve the relative alpha in relation the the time
      // expired since startTime for the given color
      //def setTransparency(color : Color) = new Color(color.getRed, color.getGreen, color.getBlue, (color.getAlpha * alpha).toInt)

      // Define the text shape, draw the frame and draw the text
      //val text = TextShape(strings(0), Vector2D(View.center.x,30), 14, Attributes("TextAlignment" -> Vector2D(0.5, 0.5), "Color" -> setTransparency(color)))
      val text = TextShape(strings(0),Vector2D(View.center.x,30),17).setAttributes("TextAlignment" -> Vector2D(0, 0.5), "Color" -> new Color(0.40f, 0.40f, 0.40f, 0.70f))

      //paintFrameSpecifyCenter(graphics, text.boundary.width.toInt + 40, text.boundary.height.toInt + 15, setTransparency(backgroundColor),Vector2D(View.center.x,30))
      graphics draw text
    }
    //LINE 2
    if (strings.length > 1 && !strings(1).isEmpty) {
      // Define the transparency for the message
      val alpha : Double = 0.75

      // Retrieve the relative alpha in relation the the time
      // expired since startTime for the given color
      def setTransparency(color : Color) = new Color(color.getRed, color.getGreen, color.getBlue, (color.getAlpha * alpha).toInt)

      // Define the text shape, draw the frame and draw the text
      val text = TextShape(strings(1), Vector2D(View.center.x,60), 12, Attributes("TextAlignment" -> Vector2D(1, 0.5), "Color" -> new Color(0.40f, 0.40f, 0.40f, 0.70f)))
      paintFrameSpecifyCenter(graphics, text.boundary.width.toInt + 40, text.boundary.height.toInt + 15, setTransparency(backgroundColor),Vector2D(View.center.x,60))
      graphics draw text
    }
    //LINE 3
    if (strings.length > 2 && !strings(2).isEmpty) {
      // Define the transparency for the message
      val alpha : Double = 0.75

      // Retrieve the relative alpha in relation the the time
      // expired since startTime for the given color
      def setTransparency(color : Color) = new Color(color.getRed, color.getGreen, color.getBlue, (color.getAlpha * alpha).toInt)

      // Define the text shape, draw the frame and draw the text
      val text = TextShape(strings(2), Vector2D(View.center.x,90), 10, Attributes("TextAlignment" -> Vector2D(0.5, 0.5), "Color" -> new Color(0.40f, 0.40f, 0.40f, 0.70f)))
      paintFrameSpecifyCenter(graphics, text.boundary.width.toInt + 40, text.boundary.height.toInt + 15, setTransparency(backgroundColor),Vector2D(View.center.x,90))
      graphics draw text
    }
  }

  /**
   * Sets the color of the tooltip text.
   */
  def setColor(color : Color) {
    this.color = color
  }
}
