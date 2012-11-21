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
import com.siigna.module.Module

/**
 * <p>
 *   Every Module is given an unique instance of ModuleInterface to access graphical stuff. The
 *   graphical settings are inherited in a direct line from the [[com.siigna.app.Siigna]] object.
 * </p>
 *
 * <p>
 *   A ModuleInterface can be chained to another interface. If the interface is chained every
 *   paint-call is forwarded to the chained instance as well. These chains are especially handy
 *   in the module system where the Default module can chain to another module and so on.
 *   This way, every active module gets painted.
 * </p>
 *
 * <p>
 *   Another handy feature is that ModuleInterfaces are bound to the module it is created for.
 *   That means that all graphical settings such as cursors, are set back to normal when a module
 *   closes, without the module having to remember to revert every graphical changes made.
 * </p>
 *
 * @param module  The module this interface is tied to.
 */
class ModuleInterface(module : Module) extends Interface {

  /**
   * The chained interface, if any.
   */
  protected var chain : Option[ModuleInterface] = None

  /**
   * The cursor of the current interface, if any.
   */
  protected var cursor : Cursor = Interface.Cursors.crosshair

  /**
   * Chain the interface to another interface.
   */
  def chain(interface : ModuleInterface) {
    chain = Some(interface)
  }

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
   * Examines whether the <code>ModuleInterface</code> is chaining to another module.
   */
  def isChained = chain.isDefined

  /**
   * Paints the interface. I. e. paint the filters, current paint-function and displays.
   * If the interface is chaining to another interface then we let that interface paint.
   */
  def paint(graphics : Graphics, transformation: TransformationMatrix) {
    // Paint the event-snaps of the module
    module.eventParser.paint(graphics, transformation)

    // Paint the current paint-function, if defined
    module.paint(graphics, transformation)

    // Paint the next interface in the chain
    if (chain.isDefined) chain.get.paint(graphics, transformation)
  }

  /**
   * Resets this interface by setting cursor to default.
   * <br/>
   * Remember that you have to direct the call to the bottom of the interface-chain
   * if you want to clear the cursor completely.
   */
  def reset() {
    cursor  = Interface.Cursors.crosshair
  }

  /**
   * Sets the cursor to a crosshair.
   */
  def setCursor(newCursor : Cursor) {
    cursor = newCursor
    Siigna.setCursor(cursor)
  }

  /**
   * Unchains any interfaces from this interface.
   */
  def unchain() {
    chain.foreach(_.unchain()) // Unchain sub-children
    chain = None // Unchain child
  }

}