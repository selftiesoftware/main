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

import java.awt.{Cursor, Point, Toolkit}
import java.awt.image.MemoryImageSource


import com.siigna.util.geom.TransformationMatrix

/**
 *
 * <p>
 *   An Interface provides functionality to interact with the graphical part of Siigna. There are two
 *   types interfaces in the Siigna core: [[com.siigna.app.view.ModuleInterface]]s and the
 *   [[com.siigna.app.Siigna]] object. The inheritance hierarchy is defined as follows:
 * <pre>
 *       Interface
 *          |
 *    +-----+-------|
 *    |             |
 * Siigna      ModuleInterface
 * </pre>
 * </p>
 *
 * <p>
 *   Each module is connected to their unique instance of a [[com.siigna.app.view.ModuleInterface]] where they
 *   can set stuff like cursors, paint-functions etc. See [[com.siigna.app.view.ModuleInterface]] for more info.
 * </p>
 * <p>
 *   The [[com.siigna.app.Siigna]]object is the main entry-point to the graphical parts of Siigna. It communicates
 *   directly to the view. See the [[com.siigna.app.Siigna]] object for more info.
 * </p>
 */
trait Interface {

  /**
   * Paints the interface.
   */
  def paint(graphics : Graphics, transformation : TransformationMatrix)

  /**
   * Set's the current cursor.
   */
  def setCursor(cursor : Cursor)

}

/**
 * The object Interface provides references to cursors and other immutable graphical
 * values relevant to the Interface.
 */
object Interface {
 
  /**
   * Different default cursors.
   */
	object Cursors {

	  /**
	   * A crosshair represented as a gray cross.
	   */
	  lazy val crosshair : Cursor = {
	    val pic     = new Array[Int](32 * 32)
	    val color   = 0x447788FF
	    for (i <- 4 to 28) {
	      if (i < 16 || i > 16) {
	        pic(i + 16 * 32) = color
	        pic(16 + i * 32) = color
	      }
	    }
	    val toolkit = Toolkit.getDefaultToolkit
	    val image = toolkit.createImage(new MemoryImageSource(32, 32, pic, 0, 32))
	    toolkit.createCustomCursor(image, new Point(16, 16), "crosshair")
	  }

    /**
     * The classical point-and-click hand
     */
    lazy val hand = new Cursor(Cursor.HAND_CURSOR)

    /**
     * An invisible block.
     */
    lazy val invisible : Cursor = {
      val toolkit = Toolkit.getDefaultToolkit
      val pic     = new Array[Int](32 * 32)
      val image   = toolkit.createImage(new MemoryImageSource(32, 32, pic, 0, 32))
      toolkit.createCustomCursor(image, new Point(16, 16), "invisibleCursor")
    }
	
	}
  
}