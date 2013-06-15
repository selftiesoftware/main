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

import com.siigna.util.Implicits._
import com.siigna.app.Siigna
import com.siigna.util.geom._
import java.awt.{Graphics => AWTGraphics, _}
import com.siigna.app.model.Drawing
import com.siigna.util.Log
import com.siigna.app.view.native.{SiignaRenderer, SiignaGraphics}
import com.siigna.app.model.shape.TextShape
import com.siigna.util.collection.Attributes

/**
 * A view in Siigna describing various information related to the visual interface, including a method to paint
 * the [[com.siigna.app.model.Model]] using [[com.siigna.app.view.Renderer]]s. Please refer to the
 * [[com.siigna.app.view.View]] object for more information.
 *
 * Used in the [[com.siigna.app.view.View$]] object which can be used throughout the application (modules included).
 * @see [[com.siigna.app.view.View$]]
 */
trait View {

  // The most recent mouse position
  protected var _mousePosition = Vector2D(0, 0)

  /**
   * A pan vector originating in the top-left corner of the screen. Protected because the public pan has it's (0,0)
   * in the center of the screen, which is much more intuitive.
   */
  protected var _pan : Vector2D = Vector2D(0, 0)

  /**
   * The time it takes to draw one frame.
   */
  protected var fpsTimeToDraw : Double = 0

  /**
   * The time it took to draw the last frame.
   */
  protected var fpsTimeToDrawLast : Double = 0

  /**
   * Pan listeners to be called whenever the user pans - that is moves the view-port of the drawing.
   */
  protected var listenersPan : Seq[Vector2D => Unit] = Nil

  /**
   * Resize listeners to be called by whoever initializes the frame or canvas to draw upon.
   */
  protected var listenersResize : Seq[(Rectangle2D) => Unit] = Nil

  /**
   * Zoom listeners to be called whenever the view changes zoom.
   */
  protected var listenersZoom : Seq[(Double) => Unit] = Nil

  /**
   * The protected renderer instance
   */
  protected var _renderer : Renderer = SiignaRenderer

  /**
   * A flag whether to show the fps or not.
   */
  var showFps = false

  /**
   * The zoom-level of the View. Starts in 1. The smaller the zoom is the smaller the shapes will be scales, which
   * is equivalent to zooming out. That bigger the zoom is the larger the shapes will be, similar to zooming in.
   */
  var zoom : Double = 1

  /**
   * Adds a pan listener that will be executed whenever the view is panning - that is whenever the user moves
   * the view-port of the drawing.
   * @param f  The function to execute after the pan operation have been made. The Vector2D signalling the distance
   *           from the previous pan-point will be given as a parameter.
   */
  def addPanListener(f : (Vector2D) => Unit) { listenersPan :+= f }

  /**
   * Adds a resize listener that will be executed whenever the view is being resized.
   * @param f  The function to execute after the resize operation have been made. The screen-dimensions represented by
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
  def drawingTransformation = TransformationMatrix(pan + center, zoom).flipY

  /**
   * Returns the center of Siigna in device-coordinates (see documentation for the [[com.siigna.app.view.View]]),
   * relative from the top left corner of the screen.
   * @return A [[com.siigna.util.geom.Vector2D]] where x = screen-width/2 and y = screen-height/2
   */
  def center = screen.center

  /**
   * The number of frames drawn per second. Useful for debugging purposes or to see how stressed the computer is.
   *
   * Uses an algorithm described at [http://stackoverflow.com/a/87333/999865].
   */
  def fps : Double = fpsTimeToDraw * 0.1 + fpsTimeToDrawLast * 0.9

  /**
   * The default graphics implementation represented as a function that can be called whenever someone needs an
   * instance of [[com.siigna.app.view.Graphics]]. If you wish to override the behaviour and insert a new Graphics
   * implementation, set this variable like so:
   * {{{
   *   class MyOwnGraphics(val AWTGraphics : Graphics2D) extends Graphics { ... }
   *   View.graphics = (AWTGraphics : Graphics2D) => new MyOwnGraphics(AWTGraphics)
   * }}}
   * The next time the View is asked to paing, the <code>MyOwnGraphics</code> class will be instantiated and returned.
   * This happens at every paint-cycle, so there's no need to do anything more.
   * @see [[com.siigna.app.view.View]]
   */
  var graphics : (Graphics2D) => Graphics = (g : Graphics2D) => new SiignaGraphics(g)

  /**
   * Returns the height of the view in screen-coordinates.
   * @return  A positive integer. If the screen have not yet been set we return 1.
   */
  def height : Int =  screen.height.toInt

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
   * <p>
   *   This method paints the View by placing graphical information on the given <code>screenGraphics</code> parameter.
   * </p>
   * <p>
   *   The method first draws the active [[com.siigna.app.view.Renderer]], as defined in the <code>renderer</code>
   *   method, meaning the current class responsible for rendering shapes in a cached and efficient manor (hopefully).
   * </p>
   * <p>
   *   Afterwards we move on to draw any [[com.siigna.app.model.selection.Selection]]s made by the user. They are all
   *   given the color defined in the <code>colorSelected</code> value in [[com.siigna.app.Siigna]].
   * </p>
   * <p>
   *   Lastly we paint the given [[com.siigna.app.view.Interface]] and through it, the modules. An Interface
   *   defines the paint-chain for the plugged in (active) modules, starting by painting the first module, receiving
   *   events from the [[com.siigna.app.controller.Controller]].
   * </p>
   *
   * @param screenGraphics  The AWT screen graphics to output the graphics to.
   * @param drawing  The [[com.siigna.app.model.Drawing]] to draw by giving it to active
   *                 [[com.siigna.app.view.Renderer]] and drawing the result on screen.
   * @param interface  The interface to draw (along with any [[com.siigna.module.Module]]s, if any.
   *                   Don't worry about this if you don't know what it is. Defaults to None.
   * @see [[com.siigna.app.view.Renderer]], [[com.siigna.app.view.Graphics]], [[com.siigna.app.view.Interface]],
   *     [[com.siigna.app.controller.Controller]]
   */
  def paint(screenGraphics : AWTGraphics, drawing : Drawing, interface : Option[Interface] = None)

  /**
   * The pan for the View, i. e. the location of the user "camera" on a 2-dimensional plane, relative to the
   * screen of the application/applet. A [[com.siigna.util.geom.Vector2D]] of (0, 0) means that the center of the
   * drawing is in the center of the screen (width / 2, height / 2). A Vector of (width / 2, height / 2) means
   * that the center of the drawing is in the bottom right of the screen.
   */
  def pan : Vector2D = Vector2D(_pan.x, _pan.y)

  /**
   * Pans the view by the given delta.
   * @param delta  How much the view should pan.
   */
  def pan(delta : Vector2D) {
    if (Siigna.navigation) {
      _pan = _pan + delta
      listenersPan.foreach(_.apply(delta))
    }
  }

  /**
   * Pans the x-axis of view by the given delta.
   * @param delta  How much the x-axis of the view should pan.
   */
  def panX(delta : Double) {
    if (Siigna.navigation) {
      _pan = _pan.copy(x = _pan.x + delta)
      listenersPan.foreach(_.apply(Vector2D(delta, _pan.y)))
    }
  }

  /**
   * Pans the y-axis of view by the given delta.
   * @param delta  How much the y-axis of the view should pan.
   */
  def panY(delta : Double) {
    if (Siigna.navigation) {
      _pan = _pan.copy(y = _pan.y + delta)
      listenersPan.foreach(_.apply(Vector2D(_pan.x, delta)))
    }
  }

  /**
   * A [[com.siigna.app.view.Renderer]] to render content for the view. This can be changed if you wish to display
   * the [[com.siigna.app.model.Drawing]] or the standard chess-checkered background differently. For example like so:
   * {{{
   *   object MyOwnRenderer extends Renderer { ... }
   *   View.renderer = MyOwnRenderer
   * }}}
   * All following calls to the <code>paint</code> will use your renderer. Paint will be called for you, don't worry.
   * <br>
   */
  def renderer : Renderer = _renderer

  /**
   * Removes the current [[com.siigna.app.view.Renderer]] and replaces it with the given. This is useful if you
   * wish to define alternate routines for caching and/or rendering the application.
   * @param renderer  The renderer to use. Cannot be null.
   * @throws IllegalArgumentException  If the renderer is null.
   */
  def renderer_=(renderer : Renderer) {
    require(renderer != null)
    _renderer = renderer
  }

  /**
   * Sets the mouse position of Siigna. Only accessible by the app package.
   * @param v  The position of the mouse.
   */
  protected[app] def setMousePosition(v : Vector2D) {
    _mousePosition = v
  }

  /**
   * The screen as a [[com.siigna.util.geom.SimpleRectangle2D]], given in device coordinates meaning that the
   * upper left corner is (0, 0) and the lower right is (width, height). Note: The coordinate system of the screen
   * thus have a negative y-axis where up is down and vice verse.
   * @return  A [[com.siigna.util.geom.SimpleRectangle2D]] from upper left corner (0, 0) to bottom right (width, height)
   */
  def screen : SimpleRectangle2D

  /**
   * Returns the width of the view in screen-coordinates.
   * @return  A positive integer. If the screen have not yet been set we return 1.
   */
  def width : Int = screen.width.toInt

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
      val oldPan = _pan
      _pan = ((pan - point + center) * zoomFactor) + point - center

      // Notify the listeners
      listenersZoom.foreach(_(zoom))
    }
  }

}

/**
 * <p>
 *   This is the view part of the
 *  <a href="http://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller">Model-View-Controller</a> pattern.
 *   The view is responsible for painting the appropriate content (in this case the [[com.siigna.app.model.Drawing]])
 *   by transforming the content to the correct zoom scale and pan vector and rendering it on the screen.
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
 *   applying two operations we can reduce it to one.
 * </p>
 *
 * <h3>Device and Drawing coordinates</h3>
 * <p>
 *  As written in the <a href="http://docs.oracle.com/javase/tutorial/2d/overview/coordinate.html">Java Tutorial</a>
 *  there are two different coordinate systems to think of when something is drawn: The coordinate system of the screen
 *  or printer that the image has to be projected upon - we call that the device coordinate system - and that of
 *  the [[com.siigna.app.model.shape.Shape]]s (normally originating at (0, 0)) - which we have dubbed the drawing
 *  coordinate system. The device coordinates use (0, 0) as the upper left corner and then displays
 *  <code>width * height</code> pixels, equal to the resolution of the device. The drawing coordinates does not fit
 *  into this coordinate-space on their own, so we have to transform them. For that purpose we have two
 *  [[com.siigna.util.geom.TransformationMatrix]]es that can transform [[com.siigna.app.model.shape.Shape]]s
 *  <i>from</i> the drawing space and <i>to</i> the device-space (<code>drawingTransformation()</code>) and one that can
 *  transform shapes <i>from</i> the device-space and <i>to</i> drawing-space (<code>deviceTransformation()</code>).
 *  The former is handy whenever we need to put the shapes onto the screen (and we need to do that a lot) while the
 *  latter can be used for keeping something on a fixed position, regardless of the zoom.
 * </p>
 * <p>
 *  Please refer to the [[com.siigna.util.geom.TransformationMatrix]] for use cases and examples.
 * </p>
 *
 * <h3>Custom rendering</h3>
 * <p>
 *   The View uses a [[com.siigna.app.view.Renderer]] to render the content in a more-or-less intelligent way (depending
 *   on the needs) using caching. The native implementation can be found in
 *   [[com.siigna.app.view.native.SiignaRenderer]].
 * </p>
 * <p>
 *   An implementation of [[com.siigna.app.view.Graphics]] is used to draw the actual shapes by the renderer. The
 *   native implementation can be found in [[com.siigna.app.view.native.SiignaGraphics]].
 * </p>
 * <p>
 *   If you have a smarter way of doing either of the above, or simply have other needs, you can override both.
 *   See the [[com.siigna.app.view.View#graphics]], [[com.siigna.app.view.View#renderer]] methods or
 *   [[com.siigna.app.view.native]] package for a description.
 * </p>
 *
 * @see [[com.siigna.app.model.Drawing]], [[com.siigna.app.controller.Controller]],
 *     [[com.siigna.util.geom.TransformationMatrix]]
 */
object View extends View {

  /**
   * The screen describing the dimensions of the canvas the view can draw upon.
   */
  protected var _screen : SimpleRectangle2D = SimpleRectangle2D(0, 0, 1, 1)

  def screen : SimpleRectangle2D = _screen

  /**
   * Sets the current screen to the given rectangle.
   * @param rectangle  The [[com.siigna.util.geom.SimpleRectangle2D]] describing the dimensions of the canvas.
   */
  protected def screen_=(rectangle : SimpleRectangle2D) { _screen = rectangle }

  // The canvas of the View
  private var _canvas : Option[Canvas] = None

  def paint(screenGraphics : AWTGraphics, drawing : Drawing, interface : Option[Interface] = None) {
    // Start the fps counter
    val fpsStart = System.currentTimeMillis()

    // Create a new transformation-matrix
    val transformation : TransformationMatrix = drawingTransformation

    // Retrieve graphics objects
    val graphics2D = screenGraphics.asInstanceOf[Graphics2D]
    val graphics = this.graphics(graphics2D)

    // Setup anti-aliasing
    val antiAliasing = Siigna.boolean("antiAliasing").getOrElse(true)
    val hints = if (antiAliasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF
    graphics2D setRenderingHint(RenderingHints.KEY_ANTIALIASING, hints)

    try {
      // Paint the renderer
      renderer.paint(graphics, drawing, this)
    } catch {
      case e : Throwable => Log.error("View: Error while rendering: ", e)
    }

    // Fetch and draw the dynamic layer.
    // TODO: Cache this
    try {
      val color = Siigna.color("colorSelected").getOrElse("#22FFFF".color)

      // Draw selection
      drawing.selection.parts(transformation).foreach(s => {
        graphics.draw(s.setAttribute("Color" -> color))
      })

      // Draw vertices
      drawing.selection.vertices.foreach(p => {
        graphics.draw(transformation.transform(p), color)
      })
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

    if (showFps) {
      graphics draw TextShape("FPS: " + fps.round, screen.bottomRight - Vector2D(20, -20), 10, Attributes("TextAlignment" -> Vector2D(1, 0)))

    }

    // Update the fps counter
    fpsTimeToDrawLast = fpsTimeToDraw
    fpsTimeToDraw = System.currentTimeMillis() - fpsStart
  }

  /**
   * Resize the view to the given boundary.
   */
  protected[app] def resize(width : Int, height : Int) {
    // Resize the canvas
    screen = SimpleRectangle2D(0, 0, width, height)

    // Notify the listeners
    listenersResize.foreach(_(screen))
  }

  /**
   * Sets the underlying canvas for setting cursors, size etc. This should not be touched by anyone outside the
   * siigna.app package!
   * @param canvas  The underlying canvas of the View object.
   */
  protected[app] def setCanvas(canvas : Canvas) {
    this._canvas = Some(canvas)
  }

  /**
   * Sets the cursor for Siigna.
   * @param cursor  The new cursor
   */
  def setCursor(cursor : Cursor) {
    if (_canvas.isDefined) _canvas.get.setCursor(cursor)
  }

}