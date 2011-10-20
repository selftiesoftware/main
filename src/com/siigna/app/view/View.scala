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

import java.applet._
import java.awt.{Color, Cursor, Graphics2D, Graphics => AWTGraphics, RenderingHints}
import java.awt.image.VolatileImage

import com.siigna.app.controller.Control
import com.siigna.util.Implicits
import com.siigna.util.Implicits._
import com.siigna.util.collection.Preferences
import com.siigna.util.geom.{Rectangle, Vector}
import com.siigna.app.model.Model
import com.siigna.app.model.shape.PolylineShape
import com.siigna.util.logging.Log
import com.siigna.app.Siigna

/**
 * A trait for the view. The view is responsible for painting the appropriate
 * content, and transforming the zoom scale and the pan vector.
 * TODO: Cache(?)
 */
trait View extends Applet {

  /**
   * A volatile image, used to utilize hardware acceleration and cancel out the double-buffering issue
   * that can cause flickering when repainting (see below).
   */
  private var cachedBackgroundImage : Option[VolatileImage] = None

  /**
   * The frames in the current second.
   */
  private var fpsCurrent : Double = 0;

  /**
   * The second the fps is counting in.
   */
  private var fpsSecond : Double = 0;

  /**
   * An accesible interface for the view.
   */
  val interface = com.siigna.app.Siigna

  /**
   * The physical center of siigna.
   */
  var center : Vector = Vector(0, 0)

  /**
   * Repaints the view.
   */
  def repaint()

  /**
   * Sets the cursor to a given cursor bitmap.
   */
  def setCursor(customCursor : Cursor)

  /***************** VIEW-CODE ****************/

  /**
   * Creates a Volatile Image with the width and height of the current screen.
   */
  private def backBuffer : VolatileImage =
    getGraphicsConfiguration.createCompatibleVolatileImage(getSize width, getSize height)

  /**
   * Creates a Volatile Image with a given width and height.
   */
  private def backBuffer(height : Int, width : Int) : VolatileImage =
    getGraphicsConfiguration.createCompatibleVolatileImage(if (height > 0) height else 1, if (width > 0) width else 1)

 /**
  * Draws the view.<br />
  *
  * This function uses a hack that eliminates all flickering caused by
  * double-buffering (http://java.sun.com/products/jfc/tsc/articles/painting/).
  * Instead of drawing everything on the views Graphics-object immediately it
  * uses a buffer image represented as the var (<code>bufferedGraphics</code>)
  * when iterating through the DOM. When the image has been drawn, it then
  * returns the image with the graphical informations. This is done in
  * order to avoid the software from drawing several times on the view at
  * the same time (which is done when iterating through the DOM), and then
  * potentially clearing paint-methods that are in the making. This can
  * create 'black-outs' (also known as double-buffering) which makes us
  * saaaad pandas.
  *
  * For more, read: <a href="http://www.javalobby.org/forums/thread.jspa?threadID=16840&tstart=0">R.J. Lorimer's entry about hardwareaccelation</a>.
  */
  def draw(graphicsPanel : AWTGraphics) { try {

    // Create a new transformation-matrix
    val transformation = interface.virtual

    // Save the size of the view.
    val size = getSize

    /**
     * Confines a coordinate within a lower and a higher boundary.
     */
    def confine(coordinate : Double, lower : Double, higher : Double) : Double = if (coordinate < lower) lower else if (coordinate > higher) higher else coordinate

    // Define the boundary by first grabbing the boundary of the model, snapping it to the current view and saving it
    // in the boundary-value.
    val offscreenBoundary = Model.boundary.transform(transformation)
    val topLeft           = Vector(confine(offscreenBoundary.topLeft.x, 0, size.width),     confine(offscreenBoundary.topLeft.y, 0, size.height))
    val bottomRight       = Vector(confine(offscreenBoundary.bottomRight.x, 0, size.width), confine(offscreenBoundary.bottomRight.y, 0, size.height))
    val boundary          = Rectangle(topLeft, bottomRight)

    // Get the volatile images
    var backgroundImage = cachedBackgroundImage.getOrElse(backBuffer)

    // Loop the rendering.
    do {
      // Validate the backgroundImage
      if (backgroundImage.validate(getGraphicsConfiguration) == VolatileImage.IMAGE_INCOMPATIBLE)
        backgroundImage = backBuffer

      // Define the buffer graphics as an instance of <code>Graphics2D</code>
      // (which is much nicer than just <code>Graphics</code>).
      val graphics2D = backgroundImage.getGraphics.asInstanceOf[Graphics2D]

      // Wraps the graphics-object in our own Graphics-wrapper (more simple API).
      val graphics = new Graphics(graphics2D)

      // Clear the view and draw a greyish background.
      graphics2D setBackground("#DDDDDF".color)
      graphics2D clearRect(0, 0, size width, size height)

      // Draw a white rectangle inside the boundary of the current model.
      graphics2D setBackground(Color white)
      graphics2D clearRect(topLeft.x.toInt, bottomRight.y.toInt, boundary.width.toInt, boundary.height.toInt)

      // Examines whether we should redraw
      val delta = interface.pan - interface.lastPan

      // Draw a black border
      graphics.draw(PolylineShape.fromRectangle(offscreenBoundary).attributes_+=("Color" -> "#555555".color))

      // Draw the Model.
      // If the number of the shapes in the Model is less than 1.000 and the boundary of the Model
      // isn't much bigger than the current view then draw the entire model and save the image as the
      // last static image. This is done because it can save a lot of performance to save the static image
      // for later, since most of the redrawing happens because of panning, which strictly doesn't require a redraw.
      // Otherwise find the shapes inside the view-bound and paint them, without saving anything as static.
      // Examines whether the cached image is valid and applicable.

      // First set the anti-aliasing
      val antiAliasing = Preferences.boolean("anti-aliasing", true)
      val hints = if (antiAliasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF
      graphics2D setRenderingHint(RenderingHints.KEY_ANTIALIASING, hints)

      // Draw model
      try {
        // Set the MBR for the model
        val mbr = Rectangle(Vector(0, 0).transform(transformation.inverse), Vector(size.width, size.height).transform(transformation.inverse)).toMBR
        Model(mbr) map(_ transform transformation) foreach(graphics draw) // Draw the entire Model
        // Filter away shapes that are drawn in the dynamic layer and draw the rest.
        //Model.queryForShapesWithId(mbr).filterNot(e => dynamic.contains(e._1)).map(e => e._2.transform(transformation) ) foreach( graphics draw)
      } catch {
        case e => Log.error("View: Unable to draw Model: "+e)
      }

      // Fetch and draw the dynamic layer.
      // TODO: Cache this too
      try {
        val dynamic = Model.dynamicShapes
        // 1: Draw the dynamic layer and a shadow of the old shape
        dynamic.values.map(_.shape.attributes_+=("Color" -> "#FF9999".color).transform(transformation)).foreach(graphics draw)
        dynamic.values.map(_.immutableShape.attributes_+=("Color" -> "#DDDDDD".color).transform(transformation)).foreach(graphics draw)
      } catch {
        case e => Log.error("View: Unable to draw the dynamic Model: "+e)
      }

      // Save information about the current zoom and pan
      //lastPan = interface.pan
      //lastZoom = interface.zoom

      /***** MODULES *****/
      // Paint the modules, displays and filters accessible by the interfaces.
      try {
        Siigna.paint(graphics, transformation)
      } catch {
        case e => Log.error("View: Error while painting the interfaces.", e)
      }

      // Draw the image we get from the print-method on the view.
      // Parameters are (Image img, int x, int y, ImageObserver observer)
      graphicsPanel drawImage(backgroundImage, 0, 0, this)
    } while (backgroundImage.contentsLost) // Continue looping until the content isn't lost.

  // Catch unexpected errors
  } catch {
    case e => Log.error("View: Unknown critical error encountered while painting.", e)

  // Set the last pan-value and the last zoom-value.
  } finally {
    if (interface.lastZoom != interface.zoom)
      interface.lastPan = interface.pan
    interface.lastZoom = interface.zoom

    // Count the fps
    val nextSecond = System.currentTimeMillis() * 0.001 + 1
    if (fpsSecond + 1 < nextSecond) { // Shift the second and save the fps
      fpsSecond = nextSecond
      Siigna.fps = fpsCurrent
      fpsCurrent = 0
    } else {
      fpsCurrent += 1 // Add a counter
    }
  } }

  /**
   * Initializes a pan in order to save the start-vector of the current pan
   * and the vector for the <code>pan</code> value.
   */
  def startPan(point : Vector) {
    interface.panPointOld   = interface.pan
    interface.panPointMouse = point
  }

  /**
   * Pans the view and asks to repaint it afterwards.
   */
  def pan(endPoint : Vector) {
    if (interface.navigation) interface.pan = interface.panPointOld + endPoint - interface.panPointMouse
    repaint
  }

  /**
   * Carries out a zoom action by zooming with the given delta and then panning
   * the view relative to the current zoom-factor.
   * The zoom-function are disabled if:
   * <ol>
   *   <li>The navigation-flag are set to false.</li>
   *   <li>The zoom level are below 0.00001 or above 50</li>
   * </ol>
   * Also, if the delta is cropped at (+/-)10, to avoid touch-pad bugs with huge deltas etc.
   */
  def zoom(point : Vector, _delta : Int)
  {
    val delta = if (_delta > 10) 10 else if (_delta < -10) -10 else _delta
    if (interface.navigation && (interface.zoom < 50 || delta > 0)) {
      val zoomFactor = scala.math.pow(2, -delta * interface.zoomSpeed)
      if ((interface.zoom > 0.000001 || delta < 0)) {
          interface.zoom *= zoomFactor
        }
      interface.pan = (interface.pan - point) * zoomFactor + point
      repaint
    }
  }

}