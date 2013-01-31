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
import com.siigna.util.logging.Log
import com.siigna.app.Siigna
import com.siigna.util.geom._
import java.awt.{Graphics => AWTGraphics, _}
import com.siigna.app.model.shape.{Shape, PolylineShape}
import java.awt.image.BufferedImage
import com.siigna.app.model.{Selection, Drawing}
import scala.Some
import com.siigna.module.ModuleMenu
import com.siigna.app.model.Drawing._
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
  //add actionlostener
  addActionListener((_, _) => {
    //and send the renderModel
    renderModel(true)
  })

  // The most recent mouse position
  private var _mousePosition = Vector2D(0, 0)

  /**
   * A background image that can be re-used to draw as background on the canvas.
   */
  private var cachedBackground : BufferedImage = null

  /**
   * An image of the model that can be re-used instead of calculating the shapes.
   */
  private var cachedModel : BufferedImage = null
  var currentZoom : Double = 0.0
  var currentPan : Vector2D = Vector2D(0,0)

  /**
   * The [[java.awt.Canvas]] of the view. None before it has been set through the <code>setCanvas()</code> method.
   */
  private var canvas : Option[Canvas] = None

  /**
   * The shape used to draw the boundary. Overwrite to draw another boundary.
   */
  var boundaryShape : SimpleRectangle2D => Shape = PolylineShape.apply(_).setAttribute("Color" -> "#AAAAAA".color)

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
   * The pan for the View, i. e. the location of the user "camera" on a 2-dimensional plane.
   */
  var pan : Vector2D = Vector2D(0, 0)

  /**
   * The zoom-level of the View.
   */
  var zoom : Double = 1

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
  def mousePosition = _mousePosition

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
   *                   Defaults to None.
   */
  def paint(screenGraphics : AWTGraphics, model : Map[Int, Shape], selection : Option[Selection] = None, interface : Option[Interface] = None) {
    // Create a new transformation-matrix
    val transformation : TransformationMatrix = drawingTransformation
    // Retrieve graphics objects
    val graphics2D = screenGraphics.asInstanceOf[Graphics2D]
    val graphics = new Graphics(graphics2D)

    // Setup anti-aliasing
    val antiAliasing = Siigna.boolean("antiAliasing").getOrElse(true)
    val hints = if (antiAliasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF
    graphics2D setRenderingHint(RenderingHints.KEY_ANTIALIASING, hints)

    try {
      // Render and draw the background
      graphics2D drawImage(renderBackground, 0, 0, null)

      // Draw the paper as a white rectangle with a margin to illustrate that the paper will have a margin when printed.
      graphics2D.setBackground(new Color(1.00f, 1.00f, 1.00f, 0.96f))
      graphics2D.clearRect(boundary.xMin.toInt, boundary.yMin.toInt - boundary.height.toInt,boundary.width.toInt, boundary.height.toInt)

      //graphics.drawRectangle(pan - boundary.topLeft + Vector2D(4, 4), pan - boundary.topLeft - Vector2D(4, 4))
     // graphics.drawRectangle(Vector2D(boundary.xMin.toInt-2, boundary.yMin.toInt-2), Vector2D(boundary.xMax+2, boundary.yMax+2))

      // OBSOLETE (no cache) : Draw model
      //if (Drawing.size > 0) try {
      //  val mbr = Rectangle2D(boundary.topLeft, boundary.bottomRight).transform(drawingTransformation.inverse)
      //  Drawing(mbr).par.map(_._2 transform transformation) foreach(graphics draw) // Draw the entire Drawing
      //} catch {
      //  case e : InterruptedException => Log.info("View: The view is shutting down; no wonder we get an error server!")
      //  case e : Throwable => Log.error("View: Unable to draw Drawing: "+e)
      //}
      
      // Render and draw the model - with cache
      //val bound = Drawing.boundary.transform(drawingTransformation)

      //a test rectangle showing the current boundary TOP LEFT CORNER
      graphics.drawRectangle(Vector2D(0, 0), Drawing.boundary.transform(drawingTransformation).bottomLeft)
      graphics.drawRectangle(Vector2D(-2, -2), Vector2D(2, 2))

      val panVector = pan - Vector2D(boundary.width.toInt/2,boundary.height.toInt/2)
      val x = (panVector.x).toInt
      val y = (panVector.y).toInt

      graphics2D drawImage(renderModel(false), x , y, null)

    }catch {
      case e : InterruptedException => Log.info("View: The view is shutting down; no wonder we get an error server!")
      case e : Throwable => Log.error("View: Unable to draw Drawing: "+e)
    }

    // Draw the boundary shape
    graphics draw boundaryShape(boundary)

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

  /**
   * Renders a background-image consisting of "chess checkered" fields. When done the image is stored in a
   * local variable. If the renderBackground method is called again, we simply return the cached copy
   * unless the dimensions of the view has changed, in which case we need to re-render it.
   */

  def renderModel(fromAction : Boolean) : BufferedImage = {

    def updateCache = {
      val m = Drawing
      val s = drawingTransformation.scaleFactor
      def width = (m.boundary.width * s + 1).toInt
      def height = (m.boundary.height * s + 1).toInt
      val image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)  // Create image
      val g = image.getGraphics.asInstanceOf[Graphics2D]  //enable drawing on the image

      // Setup anti-aliasing
      val antiAliasing = Siigna.boolean("antiAliasing").getOrElse(true)
      val hints = if (antiAliasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF

      g setRenderingHint(RenderingHints.KEY_ANTIALIASING, hints)

      val graphics = new Graphics(g)

      //TODO: Why is 0,0 not always located in the TOP LEFT corner of the paper?!?!
      graphics.drawRectangle(Vector2D(0, 0) - Vector2D(boundary.width.toInt/2,boundary.height.toInt/2), Vector2D(5, 5)- Vector2D(boundary.width.toInt/2,boundary.height.toInt/2))
      //graphics.drawRectangle(Vector2D(0, 0), Vector2D(width/2,height/2))

      //apply the graphics class to the model with g - (adds the changes to the image)
      Drawing.foreach(tuple => {
        graphics.draw(tuple._2.transform(TransformationMatrix(Vector2D(width/2,height/2),drawingTransformation.scaleFactor).flipY))
      })
      currentZoom = zoom  //store the zoom and pan settings
      currentPan  = pan
      cachedModel = image //update the image
      cachedModel //return it
    }

    //if (cachedModel == null || fromAction == true || pan != currentPan || zoom  != currentZoom) {
    if (cachedModel == null || fromAction == true || zoom  != currentZoom) {
      updateCache
    } else {
      cachedModel
    }
  }

  /**
   * Resizes the view to the given boundary.
   */
  def resize(width : Int, height : Int) {
    if (canvas.isDefined) {
      // Resize the canvas
      canvas.get.setSize(width, height)

      // Pan the view if the pan isn't set
      if (View.pan == Vector2D(0, 0)) {
        View.pan(View.screen.center)
      }
    }
  }

  /**
   * Sets the underlying canvas for setting cursors, size etc.
   * @param canvas  The underlying canvas of the View object.
   */
  protected[app] def setCanvas(canvas : Canvas) {
    this.canvas = Some(canvas)
    pan = Vector2D(canvas.getWidth / 2, canvas.getHeight / 2)
  }

  /**
   * Sets the cursor for the view.
   * @param cursor  The new cursor
   */
  def setCursor(cursor : Cursor) {
    if (canvas.isDefined) canvas.get.setCursor(cursor)
  }

  /**
   * Sets the mouse position of the view. Only accessible by the controller package.
   * @param v  The position of the mouse.
   */
  protected[app] def setMousePosition(v : Vector2D) {
    _mousePosition = v
  }

  /**
   * The screen as a rectangle, given in device coordinates.
   */
  def screen = SimpleRectangle2D(0, 0, width, height)

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
    val zoomDelta = if (delta > 10) 10 else if (delta < -10) -10 else delta
    if (Siigna.navigation && (zoom < 50 || zoomDelta > 0)) {
      val zoomFactor = scala.math.pow(2, -zoomDelta * Siigna.double("zoomSpeed").getOrElse(0.5))
      if ((zoom > 0.000001 || zoomDelta < 0)) {
        zoom *= zoomFactor
      }
      pan = (pan - point) * zoomFactor + point
    }
  }

}