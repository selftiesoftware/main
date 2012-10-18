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
import com.siigna.app.view.event._
import com.siigna.util.logging.Log
import com.siigna.util.geom.{Vector2D}
import java.lang.Thread
import java.awt.{BorderLayout}
import model.Drawing
import model.server.User
import view.View

/**
 * The main class of Siigna.
 * The applet is first and foremost responsible for setting up event listeners
 * and painting. The painting part is being handled by the <code>View</code> trait.
 * The events are forwarded to the <code>Controller</code> and the painting is primarily
 * done by painting the <code>DOM (Document Object Model)</code> and then allowing
 * the modules to paint additional graphics. The modules do not have direct access to
 * the view, but the <code>Interface</code> is designed to utilize access to it.
 */
class SiignaApplet extends Applet {

  /**
   * Closes down relevant actors and destroys the applet.
   */
  override def destroy() {
    // Stop the controller by interruption so we're sure the controller shuts it
    Controller ! 'exit

    // Stop the applet
    super.destroy()

    // Stop the system
    System.exit(0)
  }

  /**
   * Initializes the view. Sets panning to the center of the screen and
   * adds EventListeners.
   */
  override def init() {
    Siigna display("loading modules..please wait..for us to get better upload from the server. ")
    // Init parent
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
      //val drawingId = getParameter("drawingId")
      val drawingId = 1

      if (drawingId != null) try {
        val id = drawingId.toLong
        Drawing.setAttribute("id", id)
        Log.success("Applet: Found drawing: " + id)
      }
    } catch { case e => Log.info("No user or drawing found. Siigna will be running in local mode.")}

    // Set the layout
    setLayout(new BorderLayout())

    // Add the view to the applet
    add(View, BorderLayout.CENTER)

    // Misc initialization
    setVisible(true); setFocusable(true); requestFocus(); validate()



    // Allows specific KeyEvents to be detected
    setFocusTraversalKeysEnabled(false)

    // Start the controller - ends up with calling act() method in Controller.
    Controller.start()
  }



  /**
   * Overrides resize to force the underlying View to resize.
   * @param width  The width of the entire frame
   * @param height  The height of the entire frame
   */
  override def resize(width : Int, height : Int) {
    super.resize(width, height)
    View.resize(width, height)
    View.render()
  }

}

