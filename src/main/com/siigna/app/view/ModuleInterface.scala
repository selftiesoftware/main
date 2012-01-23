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

import java.awt.Cursor

import com.siigna.app.Siigna
import com.siigna.util.geom.TransformationMatrix

/**
 * Every Module is given an unique instance of ModuleInterface to access graphical stuff. The
 * graphical settings are inherited in a direct line from the Siigna object.
 * <br />
 * A ModuleInterface can be chained to another interface. If the interface is chained every
 * paint-call is forwarded to the chained instance as well. In this way every active module
 * gets painted, starting from the first one initialized to the latest.
 */
class ModuleInterface extends Interface {

  /**
   * The chained interface, if any.
   */
  protected var chain : Option[ModuleInterface] = None

  /**
   * The cursor of the current interface, if any.
   */
  protected var cursor : Cursor = Interface.Cursors.crosshair

  /**
   * The active display, if any.
   */
  protected var display : Option[Display] = None

  /**
   * A paint-function that the interface should paint on every paint-tick.
   */
  private var paint : Option[(Graphics, TransformationMatrix) => Unit] = None

  /**
   * Chain the interface to another interface.
   */
  def chain(interface : ModuleInterface) {
    chain = Some(interface)
  }

  /**
   * Clears the display. NOT the interface. The interface can only be cleared by
   * terminating the module. ModuleInterfaces can be <code>reset()</code> though.
   */
  def clearDisplay() { display = None }

  /**
   * Saves a given display.
   */
  def display(display : Display) { this.display = Some(display) }

  /**
   * Saves a given display.
   */
  def display(display : Option[Display]) { this.display = display }

  /**
   * A shorthand reference to display a popup.
   */
  def display(string : String) { display(Popup(string)) }

  /**
   * Returns the current cursor of the current interface (if an interface
   * is currently chained, it returns the cursor of the chained interface).
   */
  def getCursor : Cursor = if (chain.isDefined) chain.get.getCursor else cursor

  /**
   * Sets the cursor to an invisible block.
   */
  def hideCursor() {
    setCursor(Interface.Cursors.invisible)
  }

  /**
   * Paints the interface. I. e. paint the filters, current paint-function and displays.
   * If the interface is chaining to another interface then we let that interface paint.
   */
  def paint(graphics : Graphics, transformation: TransformationMatrix) {
    // Paint the current paint-function, if defined
    if (paint.isDefined) paint.get.apply(graphics, transformation)

    // Paint the chain
    if (chain.isDefined) chain.get.paint(graphics, transformation)

    // Paint the display of this interface - if defined
    if (display.isDefined) display.get paint graphics
  }

  /**
   * Resets this interface by setting cursor to default and clearing the display.
   * <br/>
   * Remember that you have to direct the call to the bottom of the interface-chain
   * if you want to clear the cursor completely.
   */
  def reset() {
    cursor  = Interface.Cursors.crosshair
    display = None
  }

  /**
   * Sets the cursor to a crosshair.
   */
  def setCursor(newCursor : Cursor) {
    cursor = newCursor
    Siigna.setCursor(cursor)
  }

  /**
   * Set the paint-function.
   */
  def setPaint(f : (Graphics, TransformationMatrix) => Unit) { paint = Some(f) }

  /**
   * Unchains any interfaces from this interface.
   */
  def unchain() {
    chain = None
  }

}