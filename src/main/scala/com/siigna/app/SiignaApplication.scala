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

import java.awt.event.{ComponentEvent, ComponentListener, WindowEvent, WindowListener}
import java.awt.{BorderLayout, Dimension, Frame}

/**
 * This object represents the main class of the Siigna application, when run in
 * a desktop-environment. The object uses the [[com.siigna.app.ApplicationWindow]] class to initialize as
 * an actual java.awt.Frame.
 */
object SiignaApplication
{
  /**
   * This is the main entrance of the of Siigna application, when run on the
   * desktop.
   *
   * @param  args  a string array of command-line arguments.
   */
  def main(args : Array[String])
  {
    new ApplicationWindow
  }

}

/**
 * This is the parent Frame (window), when running Siigna as a desktop application. The class embeds the
 * [[com.siigna.app.SiignaApplet]] in the Frame to re-use the code from the Applet class.
 */
class ApplicationWindow extends Frame
{

  // The applet is also an AWT Panel (the Java applet class extends it), so we
  // just create a new instance and add it to our window.
  val applet = new SiignaApplet

  // Set the layout
  setLayout(new BorderLayout())

  // Add the applet to the application.
  add(applet, BorderLayout.CENTER)

  // Set the title of the application
  setTitle("Siigna " + Siigna.string("version").getOrElse("unknown"))



  // Setup event handlers for when the window is closed.
  // We dispose the window, which in the end terminates
  // the program.
  addWindowListener(new WindowListener {
    override def windowActivated(e: WindowEvent)   { }
    override def windowDeactivated(e: WindowEvent) { }
    override def windowDeiconified(e: WindowEvent) { }
    override def windowIconified(e: WindowEvent)   { }
    override def windowClosed(e: WindowEvent)      { }
    override def windowOpened(e: WindowEvent)      { }
    override def windowClosing(e: WindowEvent)     {
      applet.destroy()
      dispose()
    }
  })

  addComponentListener(new ComponentListener {
    override def componentHidden  (e : ComponentEvent) { }
    override def componentMoved   (e : ComponentEvent) { }
    override def componentShown   (e : ComponentEvent) { }
    override def componentResized (e : ComponentEvent) {
      applet.resize(getSize.getWidth.toInt, getSize.getHeight.toInt)
    }
  })

  // Set preferred size
  setPreferredSize(
    Siigna.get("defaultScreenSize") match {
      case Some(d : Dimension) => d
      case _ => new Dimension(600, 400)
    }
  )

  // Show the window. The program is running.
  setVisible(true)

  // Request focus
  requestFocus()

  // Pack the elements of this window. The panel requests a certain size.
  pack()



  // Start the applet
  applet.init()

}

