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
package com.siigna.app

import com.siigna.util.logging.Log
import model.Model
import view._
import java.awt.{Cursor}
import com.siigna.util.geom._

/**
 * The Siigna object provides access to various core elements of the software. It also functions
 * as the first <code>Interface</code> connected to the unique interface of each module.
 */
object Siigna extends Interface {

  Log.level = Log.ERROR + Log.WARNING + Log.INFO

  /**
   * Counts the frames per second. Don't set this if you want the correct answer..
   */
  var fps : Double = 0

  /**
   * The active ModuleInterface.
   */
  private var interface : Option[ModuleInterface] = None

  /**
   * The current position of the mouse.
   */
  var mousePosition = Vector2D(0, 0)

  /**
   * If navigation is turned off the canvas of Siigna stops moving. In other words the
   * <code>Model</code> remains where it is while the modules still receives the events and
   * are still able to react.
   */
  var navigation = true

  /**
   * The printmargin of the paper to print on.
   */
  var printMargin   = 13.0

  /**
   * The minimum print format. Default is the A4-size.
   */
  var printFormatMin = 210.0 - printMargin

  /**
   * The maximum print format. Default is the A4-size.
   */
  var printFormatMax = 297.0 - printMargin

  /**
   * The screen as a rectangle, given in physical coordinates.
   * TODO: Reverse control to def!
   */
  var screen : Rectangle2D = Rectangle(Vector(0, 0), Vector(0, 0))

  /**
   * The version of Siigna.
   */
  val version = "v. 0.1.12.4"

  /**
   * The graphical environment for Siigna.
   */
  private var view : Option[View] = None

  /**
   * The zoom-speed. Defaults to 0.1 (i. e. 10%).
   */
  var zoomSpeed = 0.1

  /**
   * Returns the physical center of Siigna, relative from the top left corner
   * of the screen.
   */
  def center = screen.center

  /**
   * Get the current active cursor.
   */
  def cursor = 
    if (view.isDefined) view.get.getCursor
    else Cursor.DEFAULT_CURSOR

  /**
   * Returns the active ModuleInterface that's been placed highest in the interface-hierarchy.
   * This is the interface Siigna calls to paint on first. If any interfaces lies before in the
   * module-chain (i. e. if the active interface doesn't belong to the Default module), they won't
   * get painted.
   */
  def getInterface = interface

  /**
   * The entrance to the paint-functions of the interfaces, i. e. the modules. This function
   * requests the active interface to paint. The matrix is sent on in case the module needs
   * to use/reverse some of the transformations that already have been applied to the view.
   * <br />
   * The painting eludes the normal event-based thread communication, since we'd like to make
   * sure that the painting happens instantly.
   */
  def paint(graphics : Graphics, transformation : TransformationMatrix) {
    if (interface.isDefined) {
      interface.get.paint(graphics, transformation)
    }
  }

  /**
   * Returns the current panning position.
   */
  def pan =
    if (view.isDefined) view.get.pan
    else Vector2D(0, 0)

  /**
   * Returns the paper scale of the current model.
   */
  def paperScale = Model.boundaryScale

  /**
   * The physical TransformationMatrix.
   */
  def physical = TransformationMatrix(center, 1).flipY

  /**
   * Set's the current cursor of Siigna. Overrides the current value.
   */
  def setCursor(cursor : Cursor) {
    if (view.isDefined)
      view.get setCursor cursor
  }

  /**
   * Sets the currently active ModuleInterface.
   * This is the interface Siigna calls to paint on first. If any interfaces lies before in the
   * module-chain (i. e. if the active interface doesn't belong to the Default module), they won't
   * get painted.
   * <br />
   * In other words don't meddle with this if you don't know what you're doing.
   */
  def setInterface(interface : ModuleInterface) {
    this.interface = Some(interface)

    setCursor(interface.getCursor)
  }

  /**
   * Defines the view for Siigna to use. Should be called by the applet.
   */
  def setView(view : View) {
    this.view = Some(view)
  }

  /**
   * Returns the TransformationMatrix for the current pan distance and zoom
   * level of the view, translated to a given point.
   */
  def transformationTo(point : Vector2D) = TransformationMatrix(pan + point, zoom).flipY
  
  /**
   * Returns the TransformationMatrix for the current pan distance and zoom
   * level of the view.
   */
  def virtual = TransformationMatrix(pan, zoom).flipY

  /**
   * Returns a TransformationMatrix with a translation and scale that fits the
   * given rectangle.
   */
  def virtualTransformationTo(rectangle : Rectangle2D) = {
    // Calculates the difference between the size of the screen and the size of the
    // boundary. This is then multiplied on the zoom level to give the exact
    // scale for the TransformationMatrix.
    val screenFactor = screen.width / Model.boundary.transform(virtual).width
    val scaleFactor  = screenFactor * zoom

    TransformationMatrix(center, scaleFactor).flipY
  }

  /**
   * Returns the current zoom scale
   */
  def zoom =
    if (view.isDefined) view.get.zoom
    else 1.0

}