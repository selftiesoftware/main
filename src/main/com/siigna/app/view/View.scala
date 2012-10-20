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

import com.siigna.util.Implicits._
import com.siigna.util.logging.Log
import com.siigna.app.Siigna
import com.siigna.util.geom._
import java.awt.{Graphics => AWTGraphics, _}
import com.siigna.app.model.shape.{Shape, TextShape, PolylineShape}
import java.awt.image.{BufferedImage, VolatileImage}
import com.siigna.app.model.{Drawing, Model}
import scala.Some

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
 *   This might seem like an easy thing to do, but the core of the matter is how to do it efficiently. It is pretty
 *   cumbersome to apply two operations on each [[com.siigna.app.model.shape.Shape]] everytime we need to draw the
 *   shapes. This is where [[com.siigna.util.geom.TransformationMatrix]] comes in. This matrix is capable of containing
 *   <a href="http://en.wikipedia.org/wiki/Transformation_matrix">all lineary transformation<a>. In other words
 *   we can express every possible N-dimensional transformation in such a matrix. So instead of applying two
 *   operations we get one... And some other stuff.
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
 *  <i>from</i> the drawing space and <i>to</i> the device-space (<code>deviceTransformation()</code>) and one that can
 *  transform shapes <i>from</i> the device-space and <i>to</i> drawing-space (<code>drawingTransformation()</code>).
 *  The former is handy whenever we need to put the shapes onto the screen (and we need to do that a lot) while the
 *  latter can be used for keeping something on a fixed position, regardless of the zoom.
 * </p>
 */
object View {

  /**
   * A background image that can be re-used to draw as background on the canvas.
   */
  private var cachedBackground : BufferedImage = null

  /**
   * The [[java.awt.Canvas]] of the view. None before it has been set through the <code>setCanvas()</code> method.
   */
  private var canvas : Option[Canvas] = None

  /**
   * The shape used to draw the boundary. Overwrite to draw another boundary.
   */
  var boundaryShape : Rectangle2D => Shape = PolylineShape.apply(_).setAttribute("Color" -> "#AAAAAA".color)
  
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
   * The color of the paper (defaults to white)
   */
  var paperColor = 1.00f

  /**
   * The transformation of the user (panning and zooming), Starts out as a identity matrix since no
   * transformations has been done.
   */
  private var transformation = TransformationMatrix(center, 1).flipY

  /**
   * Define the boundary by grabbing the boundary of the model and snapping it to the current view and transformation.
   * */
  def boundary = {
    // Confines a coordinate within a lower and a higher boundary.
    def confine(coordinate : Double, lower : Double, higher : Double) : Double =
      if (coordinate < lower) lower else if (coordinate > higher) higher else coordinate

    //
    val offScreenBoundary = Drawing.boundary.transform(drawingTransformation)
    val topLeft           = Vector(confine(offScreenBoundary.topLeft.x, 0, width),
                                   confine(offScreenBoundary.topLeft.y, 0, height))
    val bottomRight       = Vector(confine(offScreenBoundary.bottomRight.x, 0, width),
                                   confine(offScreenBoundary.bottomRight.y, 0, height))
    Rectangle2D(topLeft, bottomRight)
  }

  /**
   * Returns the center of Siigna in device-coordinates (see documentation for the [[com.siigna.app.view.View]]),
   * relative from the top left corner of the screen.
   * @return A [[com.siigna.util.geom.Vector2D]] where x = screen-width/2 and y = screen-height/2
   */
  def center = screen.center

  /**
   * The device [[com.siigna.util.geom.TransformationMatrix]] that can transform shapes <i>from</i>
   * drawing-coordinates <i>to</i> device-coordinates.
   * @return  A [[com.siigna.util.geom.TransformationMatrix]]
   */
  def deviceTransformation = transformation.inverse

  /**
   * The drawing [[com.siigna.util.geom.TransformationMatrix]] that can transform shapes <i>from</i>
   * device-coordinates <i>to</i> drawing-coordinates.
   * @return  A [[com.siigna.util.geom.TransformationMatrix]]
   */
  def drawingTransformation = transformation

  /**
   * Finds the mouse position for the mouse.
   * @return  A [[com.siigna.util.geom.Vector2D]] describing the current position of the mouse on the canvas.
   */
  def mousePosition =
    if (canvas.isDefined && canvas.get.getMousePosition != null)
      Vector2D(canvas.get.getMousePosition).transform(deviceTransformation)
    else Vector2D(0, 0)

  def height = if (canvas.isDefined) canvas.get.getHeight else 0

  /**
   * Returns the canvas of the view.
   * @return  A positive integer
   */
  def width = if (canvas.isDefined) canvas.get.getWidth else 0

  protected[app] def setCanvas(canvas : Canvas) {
    this.canvas = Some(canvas)
  }

 /**
  * Draws the [[com.siigna.app.model.Model]] and the [[com.siigna.module.Module]]s.<br />
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
  */
  def paint(screenGraphics : AWTGraphics) {
    // Create a new transformation-matrix
    val transformation : TransformationMatrix = drawingTransformation

    // Retrieve graphics objects
    val graphics2D = screenGraphics.asInstanceOf[Graphics2D]
    val graphics = new Graphics(graphics2D)

    // Setup anti-aliasing
    val antiAliasing = Siigna.boolean("antiAliasing").getOrElse(true)
    val hints = if (antiAliasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF
    graphics2D setRenderingHint(RenderingHints.KEY_ANTIALIASING, hints)

    // Render and draw the background
    graphics2D drawImage(renderBackground, 0, 0, null)

    // Draw the paper as a white rectangle with a margin to illustrate that the paper will have a margin when printed.
    graphics2D.setBackground(new Color(1.00f, 1.00f, 1.00f, 0.96f))
    graphics2D.clearRect(boundary.xMin.toInt, boundary.yMin.toInt,
                  boundary.width.toInt, boundary.height.toInt)

    // Draw a white rectangle inside the boundary of the current model.
    //g.g.setBackground(new Color(1.00f, 1.00f, 1.00f, paperColor))
    //g.g.clearRect(boundary.xMin.toInt, boundary.yMin.toInt, boundary.width.toInt, boundary.height.toInt)

    // Draw model
    if (Drawing.size > 0) try {
      val mbr = Rectangle2D(boundary.topLeft, boundary.bottomRight).transform(drawingTransformation.inverse)
      Drawing(mbr).par.map(_._2 transform transformation) foreach(graphics draw) // Draw the entire Drawing
    } catch {
      case e : InterruptedException => Log.info("View: The view is shutting down; no wonder we get an error server!")
      case e => Log.error("View: Unable to draw Drawing: "+e)
    }

    // Draw the boundary shape
    graphics draw boundaryShape(boundary)

    // Fetch and draw the dynamic layer.
    // TODO: Cache this
    try {
      val color = Siigna.color("colorSelected").getOrElse("#22FFFF".color)

      // Draw selection
      Drawing.selection.foreach(s => s.selectedShapes.foreach(e => {
        graphics.draw(e.transform(transformation).setAttribute("Color" -> color))
      }))

      // Draw vertices
      Drawing.selection.foreach(_.foreach(i => {
        Drawing(i._1).getVertices(i._2).foreach(p => {
          graphics.draw(transformation.transform(p), color)
        })
      }))
    } catch {
      case e => Log.error("View: Unable to draw the dynamic Model: ", e)
    }

    // Paint the module-loading icon in the top left corner
    ModulesLoader.paint(graphics,transformation)

    // Paint the modules, displays and filters accessible by the interfaces.
    try {
      Siigna.paint(graphics, transformation)
    } catch {
      case e : NoSuchElementException => Log.warning("View: No such element exception while painting the modules. This can be caused by a (premature) reset of the module variables.")
      case e => Log.error("View: Unknown error while painting the modules.", e)
    }
  }

  /**
   * Returns the pan for the View, i. e. the location of the user "camera" on a 2-dimensional plane.
   * @return  A [[com.siigna.util.geom.Vector2D]] describing the pan of the view
   */
  def pan = transformation.getTranslate

  /**
   * Pans the view by the given delta.
   * @param delta  How much the view should pan.
   */
  def pan(delta : Vector2D) {
    if (Siigna.navigation) transformation = transformation.translate(delta)
  }

  /**
   * Pans the x-axis of view by the given delta.
   * @param delta  How much the x-axis of the view should pan.
   */
  def panX(delta : Double) {
    if (Siigna.navigation) transformation = transformation.translateX(delta)
  }

  /**
   * Pans the y-axis of view by the given delta.
   * @param delta  How much the y-axis of the view should pan.
   */
  def panY(delta : Double) {
    if (Siigna.navigation) transformation = transformation.translateX(delta)
  }

  /**
   * Renders a background-image consisting of "chess checkered" fields. When done the image is stored in a
   * local variable. If the renderBackground method is called again, we simply return the cached copy
   * unless the dimensions of the view has changed, in which case we need to re-render it.
   */
  def renderBackground : BufferedImage = {
    if (cachedBackground == null || cachedBackground.getHeight != height
                                 || cachedBackground.getWidth  != width) {
      // Create image
      val image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
      val g = image.getGraphics
      val size = Siigna.int("backgroundTileSize").getOrElse(12)
      var x = 0
      var y = 0

      // Clear background
      g setColor Siigna.color("colorBackgroundDark").getOrElse("#DADADA".color)
      g fillRect (0, 0, width, height)
      g setColor Siigna.color("colorBackgroundLight").getOrElse("E9E9E9".color)

      // Draw a chess-board pattern
      var evenRow = false
      while (x < width) {
        while (y < height) {
          g.fillRect(x, y, size, size)
          y += size << 1
        }
        x += size
        y = if (evenRow) 0 else size
        evenRow = !evenRow
      }
      cachedBackground = image
    }
    cachedBackground
  }

  def setCursor(cursor : Cursor) {
    if (canvas.isDefined) canvas.get.setCursor(cursor)
  }

  /**
   * The screen as a rectangle, given in device coordinates.
   */
  def screen = Rectangle2D(0, 0, width, height)

  /**
   * Returns the TransformationMatrix for the current pan distance and zoom
   * level of the view, translated to a given point.
   */
  def transformationTo(point : Vector2D) : TransformationMatrix = transformation.translate(point)

  /**
   * Short explanation: Redirects the update-method to <code>paint</code>.
   *
   * <p><b>Longer explanation</b>:
   * AWT draws in a way that is logical for larger software but rather
   * illogical for smaller ones. When the software needs to draw on a
   * panel it calls the <code>repaint</code>-function, which then dispatches
   * the request on to <code>update</code>. This function waits until the
   * program has the capacity for server and then calls <code>paint</code> to
   * do the dirty-work.
   * </p>
   *
   * <p><b>Further explanation:</b>
   * The reason it's great for large application is that they can end up in
   * steal resources, or situations where you need to draw on layers
   * (which probably is the case in most of the software working with AWT - or
   * Swing), where it's handy to wait until all changes on a
   * layer has been performed before server the whole thing all over. But
   * for a program such as Siigna where the main task is to actually draw a
   * drawing and modules, we don't need to put the task in line more than we need
   * to just get it done. Furthermore this jumping back and forth from
   * repaint-update-paint can be yet another source of double-buffering, since
   * <code>update</code> can cause irregular calls to <code>paint</code>
   * which then can end up in a situation where the former paint-job is overridden
   * and thus a black screen occur (see above). And that makes us saaad pandas.
   * However: Keep in mind that the <code>update</code> function is there for
   * a reason. Don't just call paint and expect the whole thing to sort
   * itself out...
   * </p>
   *
   * <p><b>Short explanation:</b>
   * We override the update functionality and dispatches it directly to <code>paint</code>, in order
   * to avoid double-buffering.
   * </p>
   */
  def update(g : AWTGraphics) { paint(g) }

  /**
   * Returns the current zoom-level of the View.
   * @return  A Double describing the zoom-level - high means very close, low means far away
   */
  def zoom = transformation.scaleFactor

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
    val zoomDelta = if (delta > 10) 10 else if (delta < -10) -10 else delta
    val zoomLevel = transformation.scaleFactor
    if (Siigna.navigation && (zoomLevel < 50 || zoomDelta > 0)) {
      val zoomFactor = scala.math.pow(2, -zoomDelta * Siigna.double("zoomSpeed").getOrElse(0.5))
      if ((zoomLevel > 0.000001 || zoomDelta < 0)) {
          transformation = transformation.scale(zoomFactor, point)
        }
    }
  }

}