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
package com.siigna.app

import com.siigna.app.model.server.User
import model._
import view._
import com.siigna.util.geom._
import java.awt.Cursor
import com.siigna.util.event.Track
import com.siigna.app.controller.remote.RemoteController

/**
 * <p>
 *   The Siigna object provides access to various core elements of the software.
 *   It is designed to be the easy access-point for graphics and system information such as mouse-position,
 *   paper scale, user-information etc.
 * </p>
 *
 * <p>
 *  Siigna inherits from [[com.siigna.app.SiignaAttributes]] which defines a number of attributes that can change
 *  the behaviour of the running program.
 * </p>
 *
 * $siignaAttributes
 *
 * <p>It also functions as the first [[com.siigna.app.view.Interface]] connected to the
 * [[com.siigna.app.view.View]]. Which basically means that Siigna is painting whatever the Siigna object tells
 * it to paint. And that is [[com.siigna.util.event.Track]], the [[com.siigna.app.view.Display]](s) and
 * the [[com.siigna.module.Module]]s.</p>
 */
object Siigna extends collection.mutable.HashMap[String, Any] with Interface with SiignaAttributes {

  /**
   * The active display, if any.
   */
  protected var display : Option[Display] = None

  /**
   * The active ModuleInterface.
   */
  private var _interface : Option[ModuleInterface] = None

  /**
   * If navigation is turned off the canvas of Siigna stops moving. In other words the
   * [[com.siigna.app.model.Drawing]] remains where it is while the modules still receives the events and
   * are still able to react.
   */
  var navigation = true

  /**
   * The current user logged in to Siigna. Set to anonymous user as default.
   */
  var user : User = User(3L, "Christoffer Pedersen", "john") //User(0L, "Anonymous", "0")

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
  def display(string : String) { display(new Popup(string)) }

  /**
   * A shorthand reference to display a tooltip.
   */
  def tooltip(strings : List[String]) { display(new Tooltip(strings)) }

  /**
   * Returns the active ModuleInterface that's been placed highest in the interface-hierarchy.
   * This is the interface Siigna calls to paint on first. If any interfaces lies before in the
   * module-chain (i. e. if the active interface doesn't belong to the Default module), they won't
   * get painted.
   * @return Some[ModuleInterface] if an interface has been set, None otherwise.
   */
  def interface : Option[ModuleInterface] = _interface

  /**
   * Sets the currently active [[com.siigna.app.view.ModuleInterface]].
   * This is the interface Siigna calls to paint on first. If any interfaces lies before in the
   * module-chain (i. e. if the active interface doesn't belong to the Default module), they won't
   * get painted.
   */
  def interface_=(interface : ModuleInterface) {
    this._interface = Some(interface)

    setCursor(interface.getCursor)
  }

  /**
   * A boolean value that indicates if we have made connection to the server and successfully authenticated the user.
   * @return  True if we can connect to the server and the user have been authenticated, false otherwise.
   */
  def isOnline : Boolean = RemoteController.isOnline

  /**
   * the ID of the latest created shape (used in Trim)
   *
   * @return Some(ID : Int) of the shape which was created last if relevant, else None
   */
  var latestID : Option[Int] = None

  /**
   * The entrance to the paint-functions of the interfaces, i. e. the modules, and the
   * [[com.siigna.app.view.Display]]. For the modules the matrix is forwarded in case the module
   * needs to use/reverse some of the transformations that already have been applied to the view.
   * <br />
   * The painting eludes the normal event-based thread communication, since the <code>Controller<code>
   * mechanism runs on it's own thread and we'd like to make sure that the painting happens instantly.
   *
   * The drawing of the [[com.siigna.module.Module]]s is synchronized to avoid any unfortunate collisions with
   * modules ending, module-information being garbage collected and what not.
   */
  def paint(graphics : Graphics, transformation : TransformationMatrix) {
    // Paint the interface
    if (_interface.isDefined) _interface.get.paint(graphics, transformation)

    // Paint the tracking - if needed
    Track.paint(graphics, transformation)

    // Paint the display or remove it if needed
    if (display.isDefined && display.get.isEnabled)
      display.get paint graphics
    else
      display = None
  }

  /**
   * Returns the paper scale of the current model.
   */
  def paperScale : Int = Drawing.boundaryScale

  /**
   * The selection-distance.
   */
  def selectionDistance : Double = {
    int("selectionDistance").get / View.zoom
  }

  /**
   * The track-distance.
   */
  def trackDistance : Double = {
    int("trackDistance").get / View.zoom
  }

  /**
   * Set's the current cursor of Siigna. Overrides the current value.
   */
  def setCursor(cursor : Cursor) {
    View setCursor cursor
  }
}
