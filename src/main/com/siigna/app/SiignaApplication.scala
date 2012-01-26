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
import java.awt.{Dimension, Frame}
import com.siigna.util.collection.Preferences

/**
 * This object represents the main class of the Siigna application, when run on
 * the desktop.
 *
 * <p>
 * This object must not be called the same as any Scala class. If you do the
 * Scala compiler does some strange things and you might loose the right
 * definition of the main method (remember "public static void" from Java). You
 * can also not use the object to extend Frame. All methods in an object are
 * static, so the Scala compiler will re-define all methods found in Frame as
 * static, which is not the right way to extend a class. This could create some
 * weird bugs, and finally ProGuard chucks on the class files. Just keep the
 * way it's made right now, or know what you do.
 * </p>
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
 * This is the window, when you run Siigna as a desktop application.
 */
class ApplicationWindow extends Frame
{

  // The applet is also an AWT Panel (the Java applet class extends it), so we
  // just create a new instance and add it to our window.
  val applet = new SiignaApplet

  // Add the applet to the application.
  add(applet)
  setTitle("Siigna")
  applet.init()
  // Setup event handler for when the window is closed (user press the X
  // button). In this case we dispose the window, which in the end terminates
  // the program.
  addWindowListener(new WindowListener {
    override def windowActivated(e: WindowEvent)   { }
    override def windowDeactivated(e: WindowEvent) { }
    override def windowDeiconified(e: WindowEvent) { }
    override def windowIconified(e: WindowEvent)   { }
    override def windowClosed(e: WindowEvent)      { }
    override def windowOpened(e: WindowEvent)      { }
    override def windowClosing(e: WindowEvent)     { applet.destroy(); dispose() }
  })

  // TODO: Add full screen mode.
  addComponentListener(new ComponentListener {
    override def componentHidden  (e : ComponentEvent) { }
    override def componentMoved   (e : ComponentEvent) { }
    override def componentShown   (e : ComponentEvent) { }
    override def componentResized (e : ComponentEvent) {
      applet.resize(getSize.getWidth.toInt, getSize.getHeight.toInt)
      false
    }
  })

  // Set preferred size
  setPreferredSize(Preferences("defaultScreenSize").asInstanceOf[Dimension])

  // Pack the elements of this window. The panel requests a certain size.
  pack()
  // Show the window. The program is running.
  setVisible(true)
  // The initialization itself is done in the view...

}

