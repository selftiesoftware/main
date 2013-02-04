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

import com.siigna.util.Implicits._
import com.siigna.app.Siigna
import com.siigna.util.geom._
import java.awt.{Graphics => AWTGraphics, _}
import com.siigna.app.model.shape.Shape
import com.siigna.app.model.{Selection, Drawing}
import com.siigna.util.Log

/**
 * <p>
 *   This is the view part of the
 *  <a href="http://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller">Model-View-Controller</a> pattern.
 *   The view is responsible for painting the appropriate content (in this case the [[com.siigna.app.model.Drawing]])
 *   and transforming the content to the correct zoom scale and pan vector.
 * </p>
 *
 * <h3>Zoom and pan</h3>
 * <p>
 *   The zoom is basically how much the user has zoomed in or out of the drawing. And the pan is how much the user
 *   has moved his perspective in a 2-dimensional space, perpendicular to the [[com.siigna.app.model.Drawing]]
 *   surface. It goes without saying that moving every single [[com.siigna.app.model.shape.Shape]] in the drawing
 *   every time the user moves his or her mouse is an incredibly bad idea. Instead we maintain one single pan-vector
 *   and a single zoom-scale that we can apply on each shape as we draw them.
 * </p>
 *
 * <h3>Transformations</h3>
 * <p>
 *   This might seem like an easy thing to do, but the core of the matter is to do it efficiently. It is pretty
 *   cumbersome to apply two operations (zoom and pan) on each [[com.siigna.app.model.shape.Shape]] every time we
 *   need to draw the shapes. This is where [[com.siigna.util.geom.TransformationMatrix]] comes in. This matrix is
 *   capable of containing <a href="http://en.wikipedia.org/wiki/Transformation_matrix">all lineary transformations</a>.
 *   In other words we can express every possible N-dimensional transformation in such a matrix. So instead of
 *   applying two operations we get one... And some other stuff.
 * </p>
 *
 * <h3>Device and Drawing coordinates</h3>
 * <p>
 *  As written in the <a href="http://docs.oracle.com/javase/tutorial/2d/overview/coordinate.html">Java Tutorial</a>
 *  there are two different coordinate systems to think of when something is drawn: The coordinate system of the screen
 *  or printer that the image has to be projected upon - we call that the device coordinate system - and that of
 *  the [[com.siigna.app.model.shape.Shape]]s (which normally origins at (0, 0)), which we have dubbed the drawing
 *  coordinate system. The device coordinates use (0, 0) as the upper left corner and then displays
 *  <code>width * height</code> pixels, equal to the resolution of the device. The drawing coordinates does not fit
 *  into this coordinate-space on their own, so we have to transform them. For that purpose we have two
 *  [[com.siigna.util.geom.TransformationMatrix]]es that can transform [[com.siigna.app.model.shape.Shape]]s
 *  <i>from</i> the drawing space and <i>to</i> the device-space (<code>drawingTransformation()</code>) and one that can
 *  transform shapes <i>from</i> the device-space and <i>to</i> drawing-space (<code>deviceTransformation()</code>).
 *  The former is handy whenever we need to put the shapes onto the screen (and we need to do that a lot) while the
 *  latter can be used for keeping something on a fixed position, regardless of the zoom.
 * </p>
 */
object View {

  // The most recent mouse position
  private var _mousePosition = Vector2D(0, 0)

  /**
   * The [[java.awt.Canvas]] of the view. None before it has been set through the <code>setCanvas()</code> method.
   */
  private var canvas : Option[Canvas] = None

  /**
   * The frames in the current second.
   * @todo Use these!
   */
  var fpsCurrent : Double = 0

  /**
   * The second the fps is counting in.
   * @todo Use these!
   */
  var fpsSecond : Double = 0

  /**
   * The pan for the View, i. e. the location of the user "camera" on a 2-dimensional plane, relative to the
   * screen of the application/applet. A [[com.siigna.util.geom.Vector]] of (0, 0) thus means that the center of the
   * view-port (drawing) is in (0, 0) of the screen (top left corner). A Vector of (width / 2, height / 2) means
   * that the center of the view-port is in the center of the drawing.
   */
  var pan : Vector2D = Vector2D(0, 0)

  /**
   * A [[com.siigna.app.view.Renderer]] to render content for the view. This can be changed if you wish to display
   * the [[com.siigna.app.model.Drawing]] or the standard chess-checkered background differently. For example like so:
   * {{{
   *   object MyOwnRenderer extends Renderer { ... }
   *   View.renderer = MyOwnRenderer
   * }}}
   * All following calls to the <code>paint</code> (it will be called for you, don't worry) will use your renderer.
   */
  var renderer : Renderer = null

  /**
   * Resize listeners to be called by whoever initializes the frame or canvas to draw upon.
   */
  private var listenersResize : Seq[(Rectangle2D) => Unit] = Nil

  /**
   * Zoom listeners to be called whenever the view changes zoom.
   */
  private var listenersZoom : Seq[(Double) => Unit] = Nil

  /**
   * The zoom-level of the View. Starts in 1. The smaller the zoom is the smaller the shapes will be scales, which
   * is equivalent to zooming out. That bigger the zoom is the larger the shapes will be, similar to zooming in.
   */
  var zoom : Double = 1

  /**
   * Adds a resize listener that will be executed whenever the view is being resized.
   * @param f  The function to execute after the resize operation has been made. The screen-dimensions represented by
   *           a [[com.siigna.util.geom.Rectangle2D]] are given as a parameter to the callback-function.
   */
  def addResizeListener(f : (Rectangle2D) => Unit) { listenersResize :+= f }

  /**
   * Adds a listener that will be executed whenever the user zooms. The function to be called
   * will receive a parameter with the current zoom.
   * @param f  The function to be executed, taking the zoom-level after the zoom operation as a parameter.
   */
  def addZoomListener(f : (Double) => Unit) { listenersZoom :+= f }

  /**
   * Define the boundary by grabbing the boundary of the model and snapping it to the current view and transformation.
   * */
  def boundary = {
    val offScreenBoundary = Drawing.boundary.transform(drawingTransformation)
    val topLeft           = Vector2D(offScreenBoundary.topLeft.x,offScreenBoundary.topLeft.y)
    val bottomRight       = Vector2D(offScreenBoundary.bottomRight.x,offScreenBoundary.bottomRight.y)
    Rectangle2D(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y)
  }

  /**
   * Returns the center of Siigna in device-coordinates (see documentation for the [[com.siigna.app.view.View]]),
   * relative from the top left corner of the screen.
   * @return A [[com.siigna.util.geom.Vector2D]] where x = screen-width/2 and y = screen-height/2
   */
  def center = screen.center

  /**
   * The device [[com.siigna.util.geom.TransformationMatrix]] that can transform shapes <i>from</i>
   * device-coordinates <i>to</i> drawing-coordinates.
   * @return  A [[com.siigna.util.geom.TransformationMatrix]]
   */
  def deviceTransformation = drawingTransformation.inverse

  /**
   * The drawing [[com.siigna.util.geom.TransformationMatrix]] that can transform shapes <i>from</i>
   * drawing-coordinates <i>to</i> device-coordinates.
   * @return  A [[com.siigna.util.geom.TransformationMatrix]]
   */
  def drawingTransformation = TransformationMatrix(pan, zoom).flipY

  /**
   * Finds the mouse position for the mouse in device coordinates, that is the coordinate system where the upper
   * left corner of the entire Siigna drawing surface (on your computer screen) is (0, 0) and the bottom right
   * corner of the drawing surface is (width, height). If you would like to know where the mouse is positioned on
   * the drawing use <code>mousePositionDrawing</code> or simple transform it yourself:
   * {{{
   *   // Find the mouse position
   *   val position = View.mousePosition
   *
   *   // Transform it FROM the the screen device coordinates and TO the drawing coordinates
   *   position.transform(View.deviceTransformation)
   * }}}
   * @return  A [[com.siigna.util.geom.Vector2D]] describing the current position of the mouse on the screen.
   */
  def mousePositionScreen = _mousePosition

  /**
   * Finds the coordinates of the mouse on the drawing. That means that we translate the mouse coordinates from the
   * device coordinates into drawing coordinated (see the <code>mousePosition</code> method and description
   * for the [[com.siigna.app.view.View]].
   * @return  A [[com.siigna.util.geom.Vector2D]] describing the current posiiton on the mouse on the drawing.
   */
  def mousePositionDrawing = _mousePosition.transform(deviceTransformation)

  /**
   * Returns the height of the view.
   * @return  A positive integer
   */
  def height = if (canvas.isDefined) canvas.get.getHeight else 0

  /**
   * <p>
   *   Draws the classical chess-checkered pattern, cleans the drawing area with a white rectangle and draw
   *   the given [[com.siigna.app.model.shape.Shape]]s, [[com.siigna.app.view.Interface]] and
   *   [[com.siigna.app.model.Selection]].
   * </p>
   *
   * This function uses a hack that eliminates all flickering caused by
   * double-buffering (http://java.sun.com/products/jfc/tsc/articles/painting/).
   * Instead of server everything on the views Graphics-object immediately it
   * uses a buffer image represented as the var (<code>bufferedGraphics</code>)
   * when iterating through the DOM. When the image has been drawn, it then
   * returns the image with the graphical informations. This is done in
   * order to avoid the software from server several times on the view at
   * the same time (which is done when iterating through the DOM), and then
   * potentially clearing paint-methods that are in the making. This can
   * create 'black-outs' (also known as double-buffering) which makes us
   * saaaad pandas.
   *
   * For more, read: <a href="http://www.javalobby.org/forums/thread.jspa?threadID=16840&tstart=0">R.J. Lorimer's entry about hardwareaccelation</a>.
   *
   * @param screenGraphics  The AWT screen graphics to output the graphics to.
   * @param model  The shapes to draw mapped with their id's. The id's are used to collect any shape parts from the
   *               selection, if needed.
   * @param selection  The selection to draw, if any.
   * @param interface  The interface to draw (along with any [[com.siigna.module.Module]]s, if any.
   *                   Don't worry about this if you don't know what it is. Defaults to None.
   */
  def paint(screenGraphics : AWTGraphics, model : Map[Int, Shape], selection : Option[Selection] = None, interface : Option[Interface] = None) {
    // Create a new transformation-matrix
    val transformation : TransformationMatrix = drawingTransformation

    // Retrieve graphics objects
    val graphics2D = screenGraphics.asInstanceOf[Graphics2D]
    val graphics = Graphics(graphics2D)

    // Setup anti-aliasing
    val antiAliasing = Siigna.boolean("antiAliasing").getOrElse(true)
    val hints = if (antiAliasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF
    graphics2D setRenderingHint(RenderingHints.KEY_ANTIALIASING, hints)

    try {
      // Render and draw the renderer
      renderer.paint(graphics)
    } catch {
      case e : InterruptedException => Log.info("View: The view is shuttin while painting. Move along...!")
      case e : Throwable => Log.error("View: Unable to render drawing: " + e)
    }

    // Fetch and draw the dynamic layer.
    // TODO: Cache this
    try {
      val color = Siigna.color("colorSelected").getOrElse("#22FFFF".color)

      // Draw selection
      selection.par.foreach(s => s.selectedShapes.foreach(e => {
        graphics.draw(e.transform(transformation).setAttribute("Color" -> color))
      }))

      // Draw vertices
      selection.par.foreach(_.foreach(i => {
        model.get(i._1).foreach(_.getVertices(i._2).foreach(p => {
          graphics.draw(transformation.transform(p), color)
        }))
      }))
    } catch {
      case e : Exception => Log.error("View: Unable to draw the dynamic Model: ", e)
    }

    //Paint the module-loading icon in the top left corner
    //ModuleMenu.paint(graphics,transformation)

    // Paint the modules, displays and filters accessible by the interfaces.
    try {
      interface.foreach(_.paint(graphics, transformation))
    } catch {
      case e : NoSuchElementException => Log.warning("View: No such element exception while painting the modules. This can be caused by a (premature) reset of the module variables.")
      case e : Throwable => Log.error("View: Unknown error while painting the modules.", e)
    }
  }

  /**
   * Pans the view by the given delta.
   * @param delta  How much the view should pan.
   */
  def pan(delta : Vector2D) {
    if (Siigna.navigation) pan = pan + delta
  }

  /**
   * Pans the x-axis of view by the given delta.
   * @param delta  How much the x-axis of the view should pan.
   */
  def panX(delta : Double) {
    if (Siigna.navigation) pan = pan.copy(x = pan.x + delta)
  }

  /**
   * Pans the y-axis of view by the given delta.
   * @param delta  How much the y-axis of the view should pan.
   */
  def panY(delta : Double) {
    if (Siigna.navigation) pan = pan.copy(y = pan.y + delta)
  }

  /**
   * Resize the view to the given boundary.
   */
  protected[app] def resize(width : Int, height : Int) {
    if (canvas.isDefined) {
      // Resize the canvas
      canvas.get.setSize(width, height)

      // Pan the view if the pan isn't set
      if (View.pan == Vector2D(0, 0)) {
        View.pan(View.screen.center)
      }

      // Notify the listeners
      listenersResize.foreach(_(screen))
    }
  }

  /**
   * Sets the underlying canvas for setting cursors, size etc. This should not be touched by anyone outside the
   * siigna.app package!
   * @param canvas  The underlying canvas of the View object.
   */
  protected[app] def setCanvas(canvas : Canvas) {
    this.canvas = Some(canvas)
    pan = Vector2D(canvas.getWidth / 2, canvas.getHeight / 2)
  }

  /**
   * Sets the cursor for Siigna.
   * @param cursor  The new cursor
   */
  def setCursor(cursor : Cursor) {
    if (canvas.isDefined) canvas.get.setCursor(cursor)
  }

  /**
   * Sets the mouse position of Siigna. Only accessible by the app package.
   * @param v  The position of the mouse.
   */
  protected[app] def setMousePosition(v : Vector2D) {
    _mousePosition = v
  }

  /**
   * The screen as a rectangle, given in device coordinates.
   */
  def screen : Rectangle2D = SimpleRectangle2D(0, 0, width, height)

  /**
   * Returns the TransformationMatrix for the current pan distance and zoom
   * level of the view, translated to a given point.
   */
  def transformationTo(point : Vector2D) : TransformationMatrix = drawingTransformation.translate(point)

  /**
   * Returns the width of the view.
   * @return  A positive integer
   */
  def width = if (canvas.isDefined) canvas.get.getWidth else 0

  /**
   * Carries out a zoom action by zooming with the given delta and then panning
   * the view relative to the current zoom-factor.
   * The zoom-function are disabled if:
   * <ol>
   *   <li>The navigation-flag are set to false.</li>
   *   <li>The zoom level are below 0.00001 or above 50</li>
   * </ol>
   * Also, if the delta is cropped at (+/-)10, to avoid touch-pad bugs with huge deltas etc.
   *
   * The zoom is, by the way logarithmic (base 2), since linear zooming gives some very brutal zoom-steps.
   *
   * @param point  The center for the zoom-operation
   * @param delta  The amount of zoomSpeed-units to zoom
   * @see [[com.siigna.app.SiignaAttributes]]
   */
  def zoom(point : Vector2D, delta : Double) {
    //TODO: Test this!
    val zoomDelta = if (delta > 10) 10 else if (delta < -10) -10 else delta
    if (Siigna.navigation && (zoom < 50 || zoomDelta > 0)) {
      val zoomFactor = scala.math.pow(2, -zoomDelta * Siigna.double("zoomSpeed").getOrElse(0.5))
      if ((zoom > 0.000001 || zoomDelta < 0)) {
        zoom *= zoomFactor
      }
      pan = (pan - point) * zoomFactor + point

      // Notify the listeners
      listenersZoom.foreach(_(zoom))
    }
  }
}