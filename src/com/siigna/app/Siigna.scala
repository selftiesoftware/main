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

import java.awt.{Container, Cursor}

import com.siigna.util.geom.{Rectangle, TransformationMatrix, Vector}
import com.siigna.util.logging.Log
import model.Model
import view.{Graphics, Interface, PaintFilter, ViewInterface}

object Siigna extends ViewInterface {

  Log.level = Log.INFO

  /**
   * A container on which elements can be added, accessible for everyone.
   */
  val container : Container = new Container

  /**
   * Counts the frames per second. Don't set this if you want the correct answer..
   */
  var fps : Double = 0;

  /**
   * The interface provided by the modules.
   */
  var interface = new Interface(this)

  /**
   * Saves the last pan-value, used to optimize the rendering-process.
   */
  var lastPan = Vector(0, 0)

  /**
   * The last zoom-value, used to optimize the rendering-process.
   */
  var lastZoom = -1.0

  /**
   * The current position of the mouse.
   */
  var mousePosition = Vector(0, 0)

  /**
   * Describes how far the view has been panned. This vector is given in
   * physical coordinates, relative from the top left point of the screen.
   */
  var pan           = Vector(0, 0)

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
   * Describes the current mouse-location when panning.
   */
  var panPointMouse =  Vector(0, 0)

  /**
   * Describes the old panning-point, so the software can tell how much
   * the current panning has moved relative to the old.
   */
  var panPointOld   = Vector(0, 0)

  /**
   * The zoom scala. Starts out in 1:1.
   */
  var zoom : Double = 1

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
   * The cursor of the view.
   */
  def cursor = try { interface.cursor } catch { case _ => new Cursor(Cursor.CROSSHAIR_CURSOR) }

  def display = try { interface.display } catch {case _ => None }

  /**
   * Returns the TransformationMatrix for the current pan distance and zoom
   * level of the view, translated to a given point.
   */
  def transformationTo(point : Vector) = TransformationMatrix(pan + point, zoom).flipY

  /**
   * Whether or not navigation is enabled.
   */
  def navigation = try { interface.navigation } catch { case _ => true }

  /**
   * The entrance to the paint-functions of the interfaces, i. e. the modules. This function requests the active
   * interface to paint. The matrix is sent on in case the module needs to use/reverse some of the transformations
   * that already have been applied to the view.
   * <br />
   * The painting eludes the normal event-based thread communication, since we'd like to make sure that the painting
   * happens instantly. There are thus potential synchronization-issues between the paint-loop and the Control thread.
   */
  def paint(graphics : Graphics, transformation : TransformationMatrix) { interface.paint(graphics, transformation) }

  /**
   * Returns the paper scale of the current model.
   */
  def paperScale = Model.boundaryScale

  /**
   * The physical TransformationMatrix.
   */
  def physical = TransformationMatrix(center, 1).flipY

  /**
   * The screen as a rectangle, given in physical coordinates.
   */
  var screen : Rectangle = Rectangle(Vector(0, 0), Vector(0, 0))
  
  /**
   * Returns the TransformationMatrix for the current pan distance and zoom
   * level of the view.
   */
  def virtual = TransformationMatrix(pan, zoom).flipY

  /**
   * Returns a TransformationMatrix with a translation and scale that fits the
   * given rectangle.
   */
  def virtualTransformationTo(rectangle : Rectangle) = {
    // Calculates the difference between the size of the screen and the size of the
    // boundary. This is then multiplied on the zoom level to give the exact
    // scale for the TransformationMatrix.
    val screenFactor = screen.width / Model.boundary.transform(virtual).width
    val scaleFactor  = screenFactor * zoom

    TransformationMatrix(center, scaleFactor).flipY
  }
  

}