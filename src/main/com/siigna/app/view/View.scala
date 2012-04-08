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
import com.siigna.util.collection.Preferences
import com.siigna.app.model.Model
import com.siigna.util.logging.Log
import com.siigna.app.Siigna
import com.siigna.util.geom._
import java.awt.{Image, Canvas, Color, Graphics2D, Graphics => AWTGraphics, RenderingHints}
import com.siigna.app.model.shape.{ImmutableShape, TextShape, PolylineShape}
import java.awt.image.{BufferedImage, VolatileImage}

/**
 * The View. The view is responsible for painting the appropriate
 * content, and transforming the zoom scale and the pan vector.
 * TODO: Cache(?)
 */
object View extends Canvas with Runnable {

  /**
   * A background image that can be re-used to draw as background on the canvas.
   */
  private var cachedBackgroundImage : Option[Image] = None      

  /**
   * An image of the cachedBackgroundImage and the [[com.siigna.app.model.Model]], used for caching purposes.
   */
  private var cachedForegroundImage : Option[Image] = None

  /**
   * A volatile image, used to utilize hardware acceleration and cancel out the double-buffering issue
   * that can cause flickering when repainting (see below).
   */
  private var cachedVolatileImage : Option[VolatileImage] = None

  /**
   * The shape used to draw the boundary. Overwrite to draw another boundary.
   */
  var boundaryShape : Rectangle2D => ImmutableShape = PolylineShape.fromRectangle(_).setAttribute("Color" -> "#AAAAAA".color)
  
  /**
   * The frames in the current second.
   */
  var fpsCurrent : Double = 0

  /**
   * The second the fps is counting in.
   */
  var fpsSecond : Double = 0

  /**
   * Describes how far the view has been panned. This vector is given in
   * physical coordinates, relative from the top left point of the screen.
   */
  var pan           = Vector2D(0, 0)

  /**
   * Describes the current mouse-location when panning.
   */
  var panPointMouse =  Vector2D(0, 0)

  /**
   * Describes the old panning-point, so the software can tell how much
   * the current panning has moved relative to the old.
   */
  var panPointOld   = Vector2D(0, 0)

  /**
   * The zoom scale. Starts out in 1:1.
   */
  var zoom : Double = 1

  /**
   * Creates a Volatile Image with the width and height of the current screen.
   */
  private def backBuffer : VolatileImage = {
    getGraphicsConfiguration.createCompatibleVolatileImage(getSize width, getSize height)
  }

  /**
   * Define the boundary by grabbing the boundary of the model and snapping it to the current view and transformation.
   * */
  def boundary = {
    // Confines a coordinate within a lower and a higher boundary.
    def confine(coordinate : Double, lower : Double, higher : Double) : Double =
      if (coordinate < lower) lower else if (coordinate > higher) higher else coordinate

    //
    val offScreenBoundary = Model.boundary.transform(View.virtual)
    val topLeft           = Vector(confine(offScreenBoundary.topLeft.x, 0, screen.width),
                                   confine(offScreenBoundary.topLeft.y, 0, getSize.height))
    val bottomRight       = Vector(confine(offScreenBoundary.bottomRight.x, 0, screen.width),
                                   confine(offScreenBoundary.bottomRight.y, 0, getSize.height))
    Rectangle2D(topLeft, bottomRight)
  }

  /**
   * Returns the physical center of Siigna, relative from the top left corner
   * of the screen.
   */
  def center = screen.center

 /**
  * Draws the [[com.siigna.app.model.Model]] and the [[com.siigna.module.Module]]s.<br />
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
  * TODO: Implement fork/join: http://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html
  *
  * For more, read: <a href="http://www.javalobby.org/forums/thread.jspa?threadID=16840&tstart=0">R.J. Lorimer's entry about hardwareaccelation</a>.
  */
  def draw(graphicsPanel : AWTGraphics) { if (getSize.getHeight > 0 && getSize.getWidth > 0) try {
    // Create a new transformation-matrix
    val transformation : TransformationMatrix = virtual

    // Get the volatile image
    var background = cachedVolatileImage.getOrElse(backBuffer)

    // Loop the rendering.
    do {
      // Validate the backgroundImage
      if (background.validate(getGraphicsConfiguration) == VolatileImage.IMAGE_INCOMPATIBLE)
        background = backBuffer

      // Define the buffer graphics as an instance of <code>Graphics2D</code>
      // (which is much nicer than just <code>Graphics</code>).
      val graphics2D = background.getGraphics.asInstanceOf[Graphics2D]

      // Wraps the graphics-object in our own Graphics-wrapper (simpler API).
      val graphics = new Graphics(graphics2D)

      // Draw the model
      if (cachedForegroundImage.isDefined) {
        graphics2D drawImage(cachedForegroundImage.get, 0, 0, this)
      }

      // Enable anti-aliasing
      graphics2D setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

      // Fetch and draw the dynamic layer.
      // TODO: Cache this
      /*try {
        //Model.dynamic.foreach(_addAttribute("Color" -> "#FF9999".color).transform(transformation)).foreach(graphics draw)
        Model.selection.foreach(_ (transformation).foreach(graphics draw))
        //dynamic.values.map(_.immutableShape.addAttribute("Color" -> "#DDDDDD".color).transform(transformation)).foreach(graphics draw)
      } catch {
        case e => Log.error("View: Unable to draw the dynamic Model: "+e)
      }*/

      /***** MODULES *****/
      // Paint the modules, displays and filters accessible by the interfaces.
      try {
        Siigna.paint(graphics, transformation)
      } catch {
        case e : NoSuchElementException => Log.warning("View: No such element exception while painting the modules. This can be caused by a (premature) reset of the module variables.", e)
        case e => Log.error("View: Unknown error while painting the modules.", e)
      }

      // Draw the image we get from the maneuvers above.
      // Parameters are (Image img, int x, int y, ImageObserver observer)
      graphicsPanel drawImage(background, 0, 0, this)
    } while (background.contentsLost) // Continue looping until the content isn't lost.

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
   * Pans the view.
   */
  def pan(endPoint : Vector2D) {
    if (Siigna.navigation) pan = panPointOld + endPoint - panPointMouse
    render()
  }

  /**
   * The physical TransformationMatrix.
   */
  def physical = TransformationMatrix(View.center, 1).flipY

  /**
   * Resizes the view to the given boundary.
   */
  override def resize(width : Int, height : Int) {
    // Pan the view if the pan isn't set
    if (View.pan == Vector2D(0, 0)) {
      View.pan(View.screen.center)
    }

    // Re-render the old background
    renderBackground()
  }

  /**
   * Renders the model and stores the image in a local variable for use during drawing.
   * In other words this is a cache function. Handy eh?
   */
  def render() {
    if (getSize.width > 0 && getSize.height > 0) try {
      // Create an image
      var image = backBuffer

      do {
        // Validate the backgroundImage
        if (image.validate(getGraphicsConfiguration) == VolatileImage.IMAGE_INCOMPATIBLE)
          image = backBuffer

        // Set the graphics
        val g = new Graphics(image.getGraphics.asInstanceOf[Graphics2D])
        g.setColor(Preferences.color("colorDraw"))

        // Render background (if empty)
        if (cachedBackgroundImage.isEmpty){ renderBackground() }

        // Draw the background
        g.g.drawImage(cachedBackgroundImage.get, 0, 0, null)

        // Draw a white rectangle inside the boundary of the current model.
        g.g.setBackground(Color white)
        g.g.clearRect(boundary.xMin.toInt, boundary.yMin.toInt, boundary.width.toInt, boundary.height.toInt)

        // Set anti-aliasing
        val antiAliasing = Preferences.boolean("antiAliasing", true)
        val hints = if (antiAliasing) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF
        g.g setRenderingHint(RenderingHints.KEY_ANTIALIASING, hints)

        // Draw model
        if (Model.size > 0) try {
          val mbr = Rectangle2D(boundary.topLeft, boundary.bottomRight).transform(virtual.inverse)
          Model(mbr).par.map(_._2 transform virtual) foreach(g draw) // Draw the entire Model
        } catch {
          case e : InterruptedException => Log.info("View: The view is shutting down; no wonder we get an error drawing!")
          case e => Log.error("View: Unable to draw Model: "+e)
        }

        // Draw the boundary shape
        g draw boundaryShape(boundary)

        // Draw a version number
        val v = TextShape(Siigna.version, Vector2D(screen.width - 60, 10), 10)
        g.draw(v)

      } while (image.contentsLost) // Continue looping until the content isn't lost.

      // Save the image
      cachedForegroundImage = Some(image)
    } catch {
      case e : NullPointerException => Log.info("View: Error fetching volatile image - probably not ready yet.")
      case e => Log.warning("View: Unknown error while painting the model: ", e)
    }
  }

  /**
   * Renders a background-image consisting of "chess checkered" fields and stores it
   * in a local variable for use during drawing.
   */
  def renderBackground() {
    if (getSize.height > 0 && getSize.width > 0) {
      // Create image
      val image = new BufferedImage(getSize.width, getSize.height, BufferedImage.TYPE_4BYTE_ABGR)
      val g = image.getGraphics
      val size = Preferences.get[Int]("backgroundTileSize").getOrElse(20)
      var x = 0
      var y = 0

      // Clear background
      g setColor Preferences.color ("colorBackgroundDark")
      g fillRect (0, 0, getSize.width, getSize.height)
      g setColor Preferences.color ("colorBackgroundLight")

      // Draw a chess-board pattern
      var evenRow = false
      while (x < getSize.width) {
        while (y < getSize.height) {
          g.fillRect(x, y, size, size)
          y += size << 1
        }
        x += size
        y = if (evenRow) 0 else size
        evenRow = !evenRow
      }
      cachedBackgroundImage = Some(image)
    }
  }

 /**
   * The active rendering-loop. Avoids paint-events from native Java.
   * See: <a href="http://download.oracle.com/javase/tutorial/fullscreen/rendering.html">download.oracle.com/javase/tutorial/fullscreen/rendering.html</a>.
   */
  override def run() { try {
    // State that we're initiating
    Log.success("View: Initiating paint-loop.")

    // Create peer or die trying!
    try {
      View.addNotify()
    } catch {
      case _ => {
        try {
          Thread.sleep(100)
          View.addNotify()
        } catch {
          case _ => {
            try {
              Thread.sleep(1000)
              View.addNotify()
            } catch {
              case _ => System.exit(-1) // Die!
            }
          }
        }
      }
    }

    // Create a buffer strategy of 2
    View.createBufferStrategy(2)

    // Start the loop
    while(true) {
      try {
        if (isShowing) {
          // Retrieve the graphics object from the buffer strategy
          val graphics = getBufferStrategy.getDrawGraphics
          draw(graphics)
          graphics.dispose()
          getBufferStrategy.show()
        }
      } catch {
        case e => Log.warning("View: Could not create graphics-object.")
      }
      
      // Terminate the thread if it's been interrupted
      if (Thread.currentThread().isInterrupted)
        throw new InterruptedException()
    }
  } catch {
    case e : InterruptedException => Log.info("View has been terminated.")
    case e => Log.error("View has been terminated with unexpected error.", e)
  } }

  /**
   * The screen as a rectangle, given in physical coordinates.
   */
  def screen = Rectangle2D(0, 0, getSize.width, getSize.height)

  /**
   * Initializes a pan in order to save the start-vector of the current pan
   * and the vector for the <code>pan</code> value.
   */
  def startPan(point : Vector2D) {
    panPointOld   = pan
    panPointMouse = point
  }

  /**
   * Returns the TransformationMatrix for the current pan distance and zoom
   * level of the view, translated to a given point.
   */
  def transformationTo(point : Vector2D) : TransformationMatrix =
    TransformationMatrix(View.pan + point, View.zoom).flipY

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
   * Swing), where it's handy to wait until all changes on a
   * layer has been performed before drawing the whole thing all over. But
   * for a program such as Siigna where the main task is to actually draw a
   * Model and modules, we don't need to put the task in line more than we need
   * to just get it done. Furthermore this jumping back and forth from
   * repaint-update-paint can be yet another source of double-buffering
   * , since <code>update</code> can cause irregular calls to <code>paint</code>
   * which then can end up in a situation where the former paint-job is overridden
   * and thus a black screen occur (see above). And that makes us saaad pandas.
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
  override def update(g : AWTGraphics) { paint(g) }

  /**
   * Returns the TransformationMatrix for the current pan distance and zoom
   * level of the view.
   */
  def virtual : TransformationMatrix = TransformationMatrix(View.pan, View.zoom).flipY

  /**
   * Returns a TransformationMatrix with a translation and scale that fits the
   * given rectangle.
   */
  def virtualTransformationTo(rectangle : Rectangle2D) = {
    // Calculates the difference between the size of the screen and the size of the
    // boundary. This is then multiplied on the zoom level to give the exact
    // scale for the TransformationMatrix.
    val screenFactor = View.screen.width / Model.boundary.transform(virtual).width
    val scaleFactor  = screenFactor * View.zoom

    TransformationMatrix(View.center, scaleFactor).flipY
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
   *
   * @param point  The center for the zoom-operation
   * @param delta  The amount of zoomSpeed-units to zoom
   * @see [[com.siigna.util.collection.Preferences]]
   */
  def zoom(point : Vector2D, delta : Double) {
    val zoomDelta = if (delta > 10) 10 else if (delta < -10) -10 else delta
    if (Siigna.navigation && (zoom < 50 || zoomDelta > 0)) {
      val zoomFactor = scala.math.pow(2, -zoomDelta * Preferences.double("zoomSpeed"))
      if ((zoom > 0.000001 || zoomDelta < 0)) {
          zoom *= zoomFactor
        }
      pan = (pan - point) * zoomFactor + point

      // Render a new model.
      render()
    }
  }

}