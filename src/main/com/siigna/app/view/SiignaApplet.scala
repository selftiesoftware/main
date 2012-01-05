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

import java.awt.event.{MouseWheelListener, MouseMotionListener, MouseListener, KeyListener, KeyEvent => AWTKeyEvent, MouseEvent => AWTMouseEvent, MouseWheelEvent}

import com.siigna.app.Siigna
import com.siigna.app.controller.Control
import com.siigna.app.view.event._
import com.siigna.util.logging.Log
import com.siigna.util.collection.Preferences
import java.awt.Dimension
import com.siigna.util.geom.{Vector2D, Rectangle}
import java.lang.Thread

/**
 * The main class of Siigna.
 * The applet is first and foremost responsible for setting up event listeners
 * and painting. The painting part is being handled by the <code>View</code> trait.
 * The events are forwarded to the <code>Controller</code> and the painting is primarily
 * done by painting the <code>DOM (Document Object Model)</code> and then allowing
 * the modules to paint additional graphics. The modules do not have direct access to
 * the view, but the <code>Interface</code> is designed to utilize access to it.
 */
class SiignaApplet extends View
{

  private var mouseButtonLeft   = false
  private var mouseButtonMiddle = false
  private var mouseButtonRight  = false

  /**
   * The active rendering-loop. Avoids paint-events from native Java.
   * See: <a href="http://download.oracle.com/javase/tutorial/fullscreen/rendering.html">download.oracle.com/javase/tutorial/fullscreen/rendering.html</a>.
   */
  private val paintLoop = new Thread("PaintLoop") {
    override def run() { try {
      var init = false;
      while(true) {

        // This is added inside the loop to make sure siigna's painting
        // TODO: Find out why this is needed!
        if (!init) {
          Log.success("View: Initiating paint-loop.")
          init = true
        }

        // Start the paint-loop
        if (isShowing) {
          val graphics = getGraphics;
          if (graphics != null) {
            draw(graphics)
            graphics.dispose()
          } else {
            Log.debug("View: Could not create graphics-object.")
          }
        }

        // Terminate the thread if it's been interrupted
        if (Thread.currentThread().isInterrupted)
          throw new InterruptedException()
      }
    } catch {
      case e : InterruptedException => Log.info("View has been terminated.")
      case e => Log.error("View has been terminated with unexpected error.", e)
    } }
  }

  /**
   * Closes down relevant actors and destroys the applet.
   */
  override def destroy() {
    // Put down the paint loop
    paintLoop.interrupt()
    
    // Stop the controller by interruption so we're sure the controller shuts it
    Control.interrupt()
    
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
    // Add event listeners
    addKeyListener(new KeyListener {
      override def keyPressed (e : AWTKeyEvent) { handleKeyEvent(e, KeyDown) }
      override def keyReleased(e : AWTKeyEvent) { handleKeyEvent(e, KeyUp)   }
      override def keyTyped   (e : AWTKeyEvent) { false }
    })
    addMouseListener(new MouseListener {
      override def mouseClicked (e : AWTMouseEvent) { false }
      override def mouseEntered (e : AWTMouseEvent) { handleMouseEvent(e, MouseEnter) }
      override def mouseExited  (e : AWTMouseEvent) { handleMouseEvent(e, MouseExit) }
      override def mousePressed (e : AWTMouseEvent) { handleMouseEvent(e, MouseDown) }
      override def mouseReleased(e : AWTMouseEvent) { handleMouseEvent(e, MouseUp) }
    })
    addMouseMotionListener(new MouseMotionListener {
      override def mouseDragged(e : AWTMouseEvent) { handleMouseEvent(e, MouseDrag) }
      override def mouseMoved  (e : AWTMouseEvent) { handleMouseEvent(e, MouseMove) }
    })
    addMouseWheelListener(new MouseWheelListener {
      override def mouseWheelMoved(e : MouseWheelEvent) { handleMouseEvent(e, MouseWheel(e getUnitsToScroll)) }
    })

    // Misc initialization
    setVisible(true)
    setFocusable(true)
    requestFocus()

    // Allows specific KeyEvents to be detected.
    setFocusTraversalKeysEnabled(false)

    // Set the correct position of the screen
    val dimension : Dimension = Preferences("defaultScreenSize").asInstanceOf[Dimension]
    setPreferredSize(dimension)

    pan(Vector2D(dimension.width >> 1, dimension.height >> 1))

    // Start the applet
    start()

    // Start the paint-loop
    paintLoop.start()

    // Start the controller
    Control.start()
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
      dispatchEvent( constructor(getCorrectCase(code), keys) )

    // If it doesn't check if a key is being hid by
    // a modifier key and return the key it that's the case
    } else  {
      val array  =
        if      (e.isControlDown && !isModifier) AWTKeyEvent.getKeyText(code).toCharArray
        else if (e.isAltDown && !isModifier) AWTKeyEvent.getKeyText(code).toCharArray
        else Array[Char]()
      if (!array.isEmpty) { // If there are elements in the character-array pass them on
        dispatchEvent( constructor(getCorrectCase(array(0)), keys) )
      } else { // Otherwise we accept the original char for the event
        dispatchEvent( constructor(getCorrectCase(code), keys) )
      }
    }
  }

  /**
   * Takes care of mouse-events by making sure the correct mouse button is
   * dispatched even in MouseMove and MouseDrag, and dispatches them to
   * EventHandler by filling in the correct constructor-parameters.
   */
  private def handleMouseEvent(e : AWTMouseEvent, constructor : (Vector2D, MouseButton, ModifierKeys) => Event)
  {
    // Converts a physical point to a virtual one.
    def toVirtual(physical : Vector2D) = {
      val r = physical.transform(Siigna.virtual.inverse)
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

      case MouseWheel (point, button, keys, delta)  => if (Siigna.navigation) { zoom(point, delta); None }
                                                       else Some(MouseWheel(toVirtual(point), button, keys, delta))
      case MouseDown  (point, MouseButtonMiddle, _) => startPan(point); None
      case MouseDrag  (point, MouseButtonMiddle, _) => pan(point); None
      case MouseUp    (point, MouseButtonMiddle, _) => pan(point); None
      case MouseEnter (point, button, keys)         => Some(MouseEnter(toVirtual(point), button, keys))
      case MouseExit  (point, button, keys)         => Some(MouseExit(toVirtual(point), button, keys))
      case MouseDown  (point, button, keys)         => Some(MouseDown(toVirtual(point), button, keys))
      case MouseUp    (point, button, keys)         => Some(MouseUp(toVirtual(point), button, keys))
      case MouseDrag  (point, button, keys)         => Some(MouseDrag(toVirtual(point), button, keys))
      case MouseMove  (point, button, keys)         => Some(MouseMove(toVirtual(point), button, keys))
      case _ => Log.error("Did not expect event: " + event); None
    }

    // Dispatch the event if it wasn't caught above
    if (option.isDefined) dispatchEvent(option.get)
  }

  /**
   * The overall event-handler. Dispatches event on to the controller.
   */
  private def dispatchEvent(event : Event) {
    Control dispatchEvent(event)
  }

  /**
   * Resizes the view.
   */
  override def resize(width : Int, height : Int) {
    // Resize the view
    super.resize(width, height)

    // Since the size of the component in some cases vary from the actual size (think menus and
    // title-bars etc.) we set the size according to the actual width and height
    Siigna.screen = Rectangle(Vector2D(0, 0), Vector2D(width, height))
  }

}

