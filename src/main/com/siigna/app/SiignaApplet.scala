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

import controller.command.{ForwardTo, Preload}
import java.awt.event.{MouseWheelListener, MouseMotionListener, MouseListener, KeyListener, KeyEvent => AWTKeyEvent, MouseEvent => AWTMouseEvent, MouseWheelEvent}

import java.applet.Applet
import com.siigna.app.controller.Controller
import com.siigna.app.view.event._
import com.siigna.util.logging.Log
import com.siigna.util.geom.{Vector2D}
import java.lang.Thread
import java.awt.{BorderLayout, Dimension}
import view.View
import com.siigna
import com.siigna.app.controller.AppletParameters

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

  private var mouseButtonLeft   = false
  private var mouseButtonMiddle = false
  private var mouseButtonRight  = false
  var hasTitle: Boolean = false

  private val paintThread = new Thread(View, "Siigna view")

  /**
   * Closes down relevant actors and destroys the applet.
   */
  override def destroy() {
    // Put down the paint loop
    paintThread.interrupt()

    // Stop the controller by interruption so we're sure the controller shuts it
    Controller ! 'exit

    // Stop the applet
    super.destroy()

    // Stop the system
    System.exit(0)
  }

  def returnHasTitleStatus = {
    (hasTitle)
  }

  /**
   * Initializes the view. Sets panning to the center of the screen and
   * adds EventListeners.
   */
  override def init() {
    // Sætter siigna applet til aktiv applet i applet parameters, så data herfra kan hentes:
    AppletParameters.setApplet(this)

    //get the active user, if a log in was performed at www.siigna.com
    AppletParameters.getContributorNameFromHomepage
    //gets the active drawing, if one was selected at www.siigna.com, or None if none was received
    AppletParameters.getDrawingIdFromHomepage

    if (AppletParameters.getDrawingId.isDefined)
    {
      AppletParameters.drawingIdReceivedAtStartup = true
    } else {
      AppletParameters.drawingIdReceivedAtStartup = false
    }
    // Set the layout
    setLayout(new BorderLayout())

    // Add the view to the applet
    add(View, BorderLayout.CENTER)

    // Misc initialization
    setVisible(true); setFocusable(true); requestFocus(); validate()

    // Add event listeners
    View.addKeyListener(new KeyListener {
      override def keyPressed (e : AWTKeyEvent) { handleKeyEvent(e, KeyDown) }
      override def keyReleased(e : AWTKeyEvent) { handleKeyEvent(e, KeyUp)   }
      override def keyTyped   (e : AWTKeyEvent) { false }
    })
    View.addMouseListener(new MouseListener {
      override def mouseClicked (e : AWTMouseEvent) { false }
      override def mouseEntered (e : AWTMouseEvent) { handleMouseEvent(e, MouseEnter) }
      override def mouseExited  (e : AWTMouseEvent) { handleMouseEvent(e, MouseExit) }
      override def mousePressed (e : AWTMouseEvent) { handleMouseEvent(e, MouseDown) }
      override def mouseReleased(e : AWTMouseEvent) { handleMouseEvent(e, MouseUp) }
    })
    View.addMouseMotionListener(new MouseMotionListener {
      override def mouseDragged(e : AWTMouseEvent) { handleMouseEvent(e, MouseDrag) }
      override def mouseMoved  (e : AWTMouseEvent) { handleMouseEvent(e, MouseMove) }
    })
    View.addMouseWheelListener(new MouseWheelListener {
      override def mouseWheelMoved(e : MouseWheelEvent) {
        // getPreciseWheelRotation is only available in 1.7
        val scroll = try {e.getPreciseWheelRotation} catch { case _ => e.getUnitsToScroll}
        handleMouseEvent(e, MouseWheel(scroll))
      }
    })

    // Allows specific KeyEvents to be detected.
    setFocusTraversalKeysEnabled(false)

    // Start the controller - ends up with calling act() method in Controller.
    Controller.start()

    // Start the paint-loop
    paintThread.start()

    // Preload modules
    Preload('Default)
    Preload('Menu)
    Preload('Selection)
    ForwardTo('Default)
  }

  /**
   * Handles and dispatches KeyEvents.
   */
  private def handleKeyEvent(e : AWTKeyEvent, constructor : (Int, ModifierKeys) => Event)
  {
    // Sets the correct casing of the character - i.e. make it uppercase if shift is pressed and vice versa
    def getCorrectCase(c : Int) = if (e.isShiftDown) c.toChar.toUpper.toInt else c.toChar.toLower.toInt

    // Set the event-values
    val keys = ModifierKeys(e isShiftDown, e isControlDown, e isAltDown)
    val code = e.getKeyCode
    val isModifier = (code == 16 || code == 17 || code == 18)

    // If they key-code equals a modifier key, nothing bad can happen..
    if (isModifier) {
      Controller ! constructor(getCorrectCase(code), keys)

    // If it doesn't check if a key is being hid by
    // a modifier key and return the key it that's the case
    } else  {
      val array  =
        if      (e.isControlDown && !isModifier) AWTKeyEvent.getKeyText(code).toCharArray
        else if (e.isAltDown && !isModifier) AWTKeyEvent.getKeyText(code).toCharArray
        else Array[Char]()
      if (!array.isEmpty) { // If there are elements in the character-array pass them on
        Controller ! constructor(getCorrectCase(array(0)), keys)
      } else { // Otherwise we accept the original char for the event
        Controller ! constructor(getCorrectCase(code), keys)
      }
    }
  }

  /**
   * Takes care of mouse-events by making sure the correct mouse button is
   * dispatched even in MouseMove and MouseDrag, and dispatches them to
   * EventHandler by filling in the correct constructor-parameters.
   */
  private def handleMouseEvent(e : AWTMouseEvent, constructor : (Vector2D, MouseButton, ModifierKeys) => Event) {
    // Converts a physical point to a virtual one.
    def toVirtual(physical : Vector2D) = {
      val r = physical.transform(View.virtual.inverse)
      Siigna.mousePosition = r
      r
    }

    // Saves the position of the mouse
    val position = Vector2D(e getX, e getY)

    // Saves the last mouse-button as a boolean in the case of a MouseDown event,
    // to be used later on.
    if (constructor == MouseDown) {
      if (e.getButton == AWTMouseEvent.BUTTON1) mouseButtonLeft   = true
      if (e.getButton == AWTMouseEvent.BUTTON2) mouseButtonMiddle = true
      if (e.getButton == AWTMouseEvent.BUTTON3) mouseButtonRight  = true
    }

    // Sets up the correct mouse-button and tests if MouseButtonMiddle is used,
    // even on computers that have none, by checking if left and right button
    // is down at the same time.
    val button = (mouseButtonLeft, mouseButtonMiddle, mouseButtonRight) match {
      case (false, false, false) => MouseButtonNone
      case (false, false,  true) => MouseButtonRight
      case (false,  true, false) => MouseButtonMiddle
      case (false,  true,  true) => MouseButtonMiddle
      case ( true, false, false) => MouseButtonLeft
      case ( true, false,  true) => MouseButtonMiddle
      case ( true,  true, false) => MouseButtonMiddle
      case ( true,  true,  true) => MouseButtonMiddle
    }

    // Resets the last mouse-button in the case of a MouseUp event, to start
    // from scratch when the next event is fired.
    if (constructor == MouseUp) {
      if (e.getButton == AWTMouseEvent.BUTTON1) mouseButtonLeft   = false
      if (e.getButton == AWTMouseEvent.BUTTON2) mouseButtonMiddle = false
      if (e.getButton == AWTMouseEvent.BUTTON3) mouseButtonRight  = false
    }

    // Setup the modifier-keys
    val keys = ModifierKeys(e isShiftDown, e isControlDown, e isAltDown)

    // Create mouse event with physical coordinates.
    val event = constructor(position, button, keys)

    // Pan (using middle button) and zoom (using wheel) or otherwise dispatch
    // to dispatchEvent.
    val option : Option[Event] = event match {

      case MouseWheel (point, _, _, delta)         => if (Siigna.navigation) { View.zoom(point, delta); None }
                                                       else Some(MouseWheel(toVirtual(point), button, keys, delta))
      case MouseDown  (point, MouseButtonMiddle, _) => View.startPan(point); None
      case MouseDrag  (point, MouseButtonMiddle, _) => View.pan(point); None
      case MouseUp    (point, MouseButtonMiddle, _) => View.pan(point); None
      case MouseEnter (point, _, _) => Some(MouseEnter(toVirtual(point), button, keys))
      case MouseExit  (point, _, _) => Some(MouseExit(toVirtual(point), button, keys))
      case MouseDown  (point, _, _) => Some(MouseDown(toVirtual(point), button, keys))
      case MouseUp    (point, _, _) => Some(MouseUp(toVirtual(point), button, keys))
      case MouseDrag  (point, _, _) => Some(MouseDrag(toVirtual(point), button, keys))
      case MouseMove  (point, _, _) => Some(MouseMove(toVirtual(point), button, keys))
      case _ => Log.error("Did not expect event: " + event); None
    }

    // Dispatch the event if it wasn't caught above
    if (option.isDefined) dispatchEvent(option.get)
  }

  /**
   * The overall event-handler. Dispatches event on to the controller.
   */
  private def dispatchEvent(event : Event) {
    Controller ! event
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

