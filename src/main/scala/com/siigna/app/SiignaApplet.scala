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

import java.applet.Applet
import com.siigna.app.controller.Controller
import java.lang.Thread
import java.awt.{Canvas, BorderLayout}
import model.Drawing
import model.server.User
import view.native.SiignaRenderer
import view.View
import com.siigna.util.Log

/**
 * The entry-point of Siigna.
 *
 * The applet is first and foremost responsible for setting up the environment for Siigna.
 * That means placing the view in the context of the applet so it's actually run, telling the
 * controller to setup event listeners and get the view to start painting. The events are
 * forwarded to the [[com.siigna.app.controller.Controller]]. The actual painting part is being
 * handled by the [[com.siigna.app.view.View]] object by painting the [[com.siigna.app.model.Drawing]]
 * and then allowing the modules to paint additional graphics.
 */
class SiignaApplet extends Applet {

  // The canvas on which we can paint
  private var canvas : Canvas = null

  // The controller to whom we send events
  private var controller : Controller = null

  // The paint thread
  private val paintThread = new Thread() { override def run() { paintLoop() } }

  // A boolean value indicating whether to exit the paint-loop
  private var shouldExit = false

  override def destroy() {
    // Exit the paint-loop
    shouldExit = true

    // Stop the controller by interruption so we're sure the controller shuts it
    if (controller != null) controller ! 'exit
              
    // Wait for the paint thread (max 500ms)
    paintThread.join(500)
    
    // Quit the system
    System.exit(0)
  }

  /**
   * Initializes the view. Sets panning to the center of the screen and
   * adds EventListeners.
   */
  override def init() {
    // Init parent - this should be the first line in Siigna... Ever!
    super.init()

    Siigna("isLive") = false

    // Start by reading the applet parameters
    try {
      // Gets the active drawing id, if one was selected at www.siigna.com, or None if none was received
      val drawingId = getParameter("drawingId")

      if (drawingId != null) try {
        val id = drawingId.toLong
        Drawing.setAttribute("id", id)
        Log.success("Applet: Found drawing: " + id)
      }

      // Get the active user, if a log in was performed at www.siigna.com
      val userName = getParameter("contributorName")

      if (userName != null) {
        // TODO: Refine this
        Siigna.user = User(0, userName, util.Random.nextString(20))
        Log.success("Applet: Found user: " + userName)
      }
    } catch { case _ : Throwable => Log.info("Applet: No user or drawind-id found.")}

    // Set the layout
    setLayout(new BorderLayout())

    // Add the view to the applet
    canvas = new Canvas()
    add(canvas, BorderLayout.CENTER)
    canvas.setIgnoreRepaint(true)
    canvas.requestFocus()
    View.setCanvas(canvas)
    canvas.setSize(getSize)


    // Misc initialization
    setVisible(true); setFocusable(true); requestFocus()

    // Allows specific KeyEvents to be detected
    setFocusTraversalKeysEnabled(false)

    // Get the controller and setup event-listeners
    controller = new Controller()
    controller.setupEventListenersOn(canvas)

    // Start the controller
    controller.start()

    // Paint the view
    new Thread() {
      override def run() { paintLoop() }
    }.start()
  }

  /**
   * This is the actual paint-loop for the applet. The loop stops when <code>shouldExit</code> is set to true.
   *
   * The code is inspired by Java's api-entry on
   * <a href="http://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferStrategy.html">BufferStrategy</a>.
   */
  private def paintLoop() {
    // Create a double buffer strategy
    canvas.createBufferStrategy(2)

    // Get the strategy
    val strategy = canvas.getBufferStrategy

    // Add the Siigna renderer
    // This have to be done here to avoid ExceptionInInitializer error
    // - adding the code in View will mess up the initialization-order, causing the calls to the View to happen
    // before the View has been initialized
    View.renderer = SiignaRenderer

    // Run, run, run
    while(!shouldExit) {

      // Render a single frame
      if (strategy != null) do {

        // The following loop ensures that the contents of the drawing buffer
        // are consistent in case the underlying surface was recreated
        do {
          // Fetch the buffer graphics
          val graphics = strategy.getDrawGraphics

          // Paint the view
          View.paint(graphics, Some(Siigna))

          // Dispose of the graphics
          graphics.dispose()
        } while (strategy.contentsRestored())

        // Make the next buffer available
        strategy.show()
      } while (strategy.contentsLost())
    }
  }

  /**
   * Overrides resize to force the underlying View to resize.
   * @param width  The width of the entire frame
   * @param height  The height of the entire frame
   */
  override def resize(width : Int, height : Int) {
    super.resize(width, height)
    View.resize(width, height)
  }

}

