/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.view

import java.awt.{GraphicsEnvironment, GraphicsConfiguration}
import java.awt.image.VolatileImage


/**
 * a class to generate a <a href="http://content.gpwiki.org/index.php/Java:Tutorials:VolatileImage">Voltile Image </a>
 * that can be used as a performance optimisation strategy.

 */
object VRAMImage {
  val ge : GraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment()
  val gc : GraphicsConfiguration  = ge.getDefaultScreenDevice().getDefaultConfiguration()

  def createVolatileImage(width : Int, height : Int, transparency : Int) : VolatileImage = {
    var image : VolatileImage = null

    image = gc.createCompatibleVolatileImage(width, height, transparency)

    val valid : Int = image.validate(gc)

    if (valid == VolatileImage.IMAGE_INCOMPATIBLE) {
      image = this.createVolatileImage(width, height, transparency);
      return image
    }

    return image
  }
}
