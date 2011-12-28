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

import java.applet.Applet
import java.awt.{Color, Graphics2D, Graphics => AWTGraphics, RenderingHints}
import java.awt.image.VolatileImage

import com.siigna.util.Implicits._
import com.siigna.util.collection.Preferences
import com.siigna.app.model.Model
import com.siigna.app.model.shape.PolylineShape
import com.siigna.util.logging.Log
import com.siigna.app.Siigna
import com.siigna.util.geom.{Vector2D, Rectangle, Vector}

/**
 * The View. The view is responsible for painting the appropriate
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
  var fpsCurrent : Double = 0;

  /**
   * The second the fps is counting in.
   */
  var fpsSecond : Double = 0;

  /**
   * Describes how far the view has been panned. This vector is given in
   * physical coordinates, relative from the top left point of the screen.
   */
  var pan           = Vector(0, 0)

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
   * The zoom scale. Starts out in 1:1.
   */
  var zoom : Double = 1

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
    val transformation = Siigna.virtual

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

      // Draw a black border
      val p = PolylineShape.fromRectangle(offscreenBoundary).setAttribute("Color" -> "#555555".color)
      graphics.draw(p)

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
        // TODO: Set the MBR for the model
        //val mbr = Rectangle(Vector(0, 0).transform(transformation.inverse), Vector(size.width, size.height).transform(transformation.inverse)).toMBR
        Model map(_ transform transformation) foreach(graphics draw) // Draw the entire Model
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
        dynamic.values.map(_.shape.addAttribute("Color" -> "#FF9999".color).transform(transformation)).foreach(graphics draw)
        dynamic.values.map(_.immutableShape.addAttribute("Color" -> "#DDDDDD".color).transform(transformation)).foreach(graphics draw)
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

  } finally {
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
  def startPan(point : Vector2D) {
    panPointOld   = pan
    panPointMouse = point
  }

  /**
   * Short explanation: Redirects the update-method to <code>paint</code>.
   *
   * <p><b>Longer explanation</b>:
   * AWT draws in a way that is logical for larger software but rather
   * illogical for smaller ones. When the software needs to draw on a
   * panel it calls the <code>repaint</code>-function, which then dispatches
   * the request on to <code>update</code>. This function waits until the
   * program has the capacity for drawing and then calls <code>paint</code> to
   * do the dirty-work.
   * </p>
   *
   * <p><b>Further explanation:</b>
   * The reason it's great for large application is that they can end up in
   * situations that needs massive computer-power, where a sudden painting can
   * steal resources, or situations where you need to draw on layers
   * (which probably is the case in most of the software working with AWT - or
   * Swing in particular), where it's handy to wait until all changes on a
   * layers has been performed before drawing the whole thing all over. But
   * for a program such as Siigna where the main task is to actually draw a
   * Model, we don't need to put the task in line more than we need
   * to just get it done. Furthermore this jumping back and forth from
   * repaint-update-paint can be yet another source of double-buffering
   * (see above), since <code>update</code> can cause irregular calls to <code>paint</code>
   * which then can end up in a situation where the former paint-job is overridden
   * and thus a black screen occur. Which makes us saaad pandas.
   * However: Keep in mind that the <code>update</code> function is there for
   * a reason. Don't ever just call paint and expect the whole thing to sort
   * itself out...
   * </p>
   *
   * <p><b>Short explanation:</b>
   * We override the update functionality and dispatches it directly to <code>paint</code>, in order
   * to avoid double-buffering.
   * </p>
   */
  override def update(g : AWTGraphics) {
    paint(g)
  }

  /**
   * Pans the view.
   */
  def pan(endPoint : Vector2D) {
    if (Siigna.navigation) pan = panPointOld + endPoint - panPointMouse
  }

  /**
   * Pans the view by a given x and y coordinate.
   */
  def pan(x : Double, y : Double) {
    if (Siigna.navigation) pan = panPointOld + Vector2D(x, y) - panPointMouse
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
  def zoom(point : Vector2D, _delta : Int)
  {
    val delta = if (_delta > 10) 10 else if (_delta < -10) -10 else _delta
    if (Siigna.navigation && (zoom < 50 || delta > 0)) {
      val zoomFactor = scala.math.pow(2, -delta * Siigna.zoomSpeed)
      if ((zoom > 0.000001 || delta < 0)) {
          zoom *= zoomFactor
        }
      pan = (pan - point) * zoomFactor + point
    }
  }

}