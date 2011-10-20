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

import java.awt.{Component, Container, Cursor, Point, Toolkit}
import java.awt.image.MemoryImageSource

import com.siigna.app._

import com.siigna.util.geom.TransformationMatrix

/**
 * A public interface that provides an interface with the core.
 */
class Interface(interface : ViewInterface) extends ViewInterface {

  /**
   * Contains a list of components, added by this interface.
   */
  private var components : List[Component] = Nil

  /**
   * The cursor of the current interface.
   */
  var cursor : Cursor      = interface.cursor

  /**
   * The active display, if any.
   */
  var display : Option[Display] = interface.display

  /**
   * A list containing the paintfilters for the given view
   */
  var filters              = interface.filters

  /**
   * A boolean value that signals whether panning and zooming is active or not
   */
  var navigation : Boolean = interface.navigation

  /**
   * A paint-function that the interface should paint on every paint-tick.
   */
  private var paint : Option[(Graphics, TransformationMatrix) => Unit] = None

  /**
   * The Java Toolkit used by the interface.
   */
  private lazy val toolkit = Toolkit.getDefaultToolkit

  /**
   * Adds a component to the container of Siigna.
   * Instead of calling the direct value at Siigna.container, this function
   * cleans up after itself, removing every component added by this interface.
   */
  def addComponent(c : Component) {
    container(_.add(c))
    components = components :+ c
  }

  /**
   * Add a paint filterRecursive to the interface.
   */
  def addPaintFilter(filter : PaintFilter)  = filters += filter

  /**
   * Clears the display. NOT the interface. The interface can only be cleared by destroying the module.
   */
  def clearDisplay() { display = None }

  /**
   * Clears all paint filters.
   */
  def clearPaintFilters() { filters.clear() }

  /**
   * A method used to access the container of Siigna.
   * The function adds a validate method, which is required to display the
   * elements in the container correctly.
   */
  def container(f : Container => Any) {
    f(Siigna.container)
    Siigna.container.validate()
  }

  /**
   * Destroy this interface.
   * This method should not be called directly. It's being called by the module
   * the given interface is connected to, when the module exits.
   */
  def destroy() {
    components.foreach(c => container(_.remove(c)))
  }

  /**
   * Disables navigation (panning and zooming).
   */
  def disableNavigation() { navigation = false }

  /**
   * Saves a given display.
   */
  def display(newDisplay : Display) { this.display = Some(newDisplay) }

  /**
   * Saves a given display.
   */
  def display(newDisplay : Option[Display]) { this.display = newDisplay }

  /**
   * A shorthand reference to display a popup.
   */
  def display(string : String) { display(Popup(string)) }

  /**
   * Enables navigation (panning and zooming).
   */
  def enableNavigation() { navigation = true }

  /**
   * Sets the cursor to an invisible block.
   */
  def hideCursor() {
    val pic       = new Array[Int](32 * 32)
    val image     = toolkit.createImage(new MemoryImageSource(32, 32, pic, 0, 32))
    val invisible = toolkit.createCustomCursor(image, new Point(16, 16), "invisibleCursor");
    cursor = invisible
  }

  /**
   * Paints the interface. I. e. paint the filters, current paint-function and any active displays.
   */
  def paint(graphics : Graphics, transformation: TransformationMatrix) {
    //TODO: filters foreach{_.paint(bufferedSrc)}

    // Paint the current paint-function, if defined
    if (paint.isDefined) {
      paint.get.apply(graphics, transformation)
    }

    // Paint the display
    if (display.isDefined) display.get paint graphics
  }

  /**
   * Remove a paint filterRecursive from the list.
   */
  def paintFilterRemove(filter : PaintFilter) = filters -= filter

  /**
   * Resets the interface to default.
   */
  def reset() {
    navigation = true
    filters.clear()
    setCursor(Interface.Cursors.crosshair)
  }

  /**
   * Sets the cursor to a crosshair.
   */
  def setCursor(newCursor : Cursor) { cursor = newCursor }

  /**
   * Set the paint-function.
   */
  def setPaint(f : (Graphics, TransformationMatrix) => Unit) { paint = Some(f) }

}

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
	    val color   = 0xAAEEEEFF
	    for (i <- 4 to 28) {
	      if (i < 15 || i > 17) {
	        pic(i + 16 * 32) = color
	        pic(16 + i * 32) = color
	      }
	    }
	    val toolkit = Toolkit.getDefaultToolkit
	    val image = toolkit.createImage(new MemoryImageSource(32, 32, pic, 0, 32))
	    toolkit.createCustomCursor(image, new Point(16, 16), "crosshair")
	  }
	
	}
  
}