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

import java.awt.event.{MouseWheelListener, MouseMotionListener, MouseListener, KeyListener, KeyEvent => AWTKeyEvent, MouseEvent => AWTMouseEvent, MouseWheelEvent}

import java.applet.Applet
import com.siigna.app.controller.Controller
import com.siigna.util.logging.Log
import java.lang.Thread
import java.awt.{Canvas, BorderLayout}
import model.Drawing
import model.server.User
import view.View

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

  // The paint thread
  private val paintThread = new Thread() { override def run() { paintLoop() } }

  // A boolean value indicating whether to exit the paint-loop
  private var shouldExit = false

  override def destroy() {
    // Exit the paint-loop
    shouldExit = true

    // Stop the controller by interruption so we're sure the controller shuts it
    Controller ! 'exit
              
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

    // Start by reading the applet parameters
    try {
      // Get the active user, if a log in was performed at www.siigna.com
      //val userName = getParameter("contributorName")
      val userName = "anonymous"
      if (userName != null) {
        // TODO: Refine this
        Siigna.user = User(0, userName, util.Random.nextString(20))
        Log.success("Applet: Found user: " + userName)
      }

      // Gets the active drawing id, if one was selected at www.siigna.com, or None if none was received
      val drawingId = getParameter("drawingId")

      if (drawingId != null) try {
        val id = drawingId.toLong
        Drawing.setAttribute("id", id)
        Log.success("Applet: Found drawing: " + id)
      }
    } catch { case e => Log.info("No user or drawing found. Siigna will be running in anonymous mode.")}

    // Set the layout
    setLayout(new BorderLayout())

    // Add the view to the applet
    canvas = new Canvas()
    add(canvas, BorderLayout.CENTER)
    canvas.setIgnoreRepaint(true)
    canvas.requestFocus()
    canvas.setSize(getSize)
    View.setCanvas(canvas)

    // Misc initialization
    setVisible(true); setFocusable(true); requestFocus()

    // Allows specific KeyEvents to be detected
    setFocusTraversalKeysEnabled(false)

    Controller.setupEventListenersOn(canvas)

    // Start the controller
    Controller.start()

    // Paint the view
    new Thread() {
      override def run() { paintLoop() }
    }.start()
  }

  /**
   * Overrides resize to force the underlying View to resize.
   * @param width  The width of the entire frame
   * @param height  The height of the entire frame
   */
  override def resize(width : Int, height : Int) {
    super.resize(width, height)
    if (canvas != null)
      View.resize(width, height)
  }

  /**
   * This is the actual paint-loop for the applet. The loop stops when <code>shouldExit</code> is set to true.
   *
   * The code is stolen from Java's api-entry on
   * <a href="http://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferStrategy.html">BufferStrategy</a>.
   */
  private def paintLoop() {
    // Create a double buffer strategy
    canvas.createBufferStrategy(2)

    // Get the strategy
    val strategy = canvas.getBufferStrategy

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
          View.paint(graphics)

          // Dispose of the graphics
          graphics.dispose()
        } while (strategy.contentsRestored())

        // Make the next buffer available
        strategy.show()
      } while (strategy.contentsLost())
    }
  }

}

