/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.controller

import java.awt.Canvas
import com.siigna.app.view.View
import java.awt.event.{KeyEvent => AWTKeyEvent, MouseEvent => AWTMouseEvent, _}
import com.siigna.util.event._
import com.siigna.util.geom.Vector2D
import com.siigna.app.Siigna
import com.siigna.util.logging.Log
import com.siigna.util.event.MouseDown
import com.siigna.util.event.KeyDown
import com.siigna.util.event.MouseExit
import scala.Some
import com.siigna.util.event.MouseEnter
import com.siigna.util.event.KeyUp
import com.siigna.util.event.MouseMove
import com.siigna.util.event.MouseUp
import com.siigna.util.event.MouseDrag
import actors.Actor

/**
 * The EventController is responsible for setting up event-listeners on the view
 */
trait EventController extends Actor {

  private var mouseButtonLeft   = false
  private var mouseButtonMiddle = false
  private var mouseButtonRight  = false

  /**
   * Parses key events by filtering modifier keys (shift, alt, control etc.) and converting them to a
   * format Siigna can understand.
   * @param e  The AWT event to parse
   * @param constructor  The constructor for the Siigna key event, to distinguish between KeyDown and KeyUp
   * @return A [[com.siigna.util.event.KeyEvent]] with information about the modifier keys, unless it has been
   *         consumed while panning and zooming
   *
   * @todo Arrow-keys are not handled properly
   */
  private def parseKeyEvent(e : AWTKeyEvent, constructor : (Int, ModifierKeys) => KeyEvent) : Option[KeyEvent] = {
    // Sets the correct casing of the character - i.e. make it uppercase if shift is pressed and vice versa
    def getCorrectCase(c : Int) = if (e.isShiftDown) c.toChar.toUpper.toInt else c.toChar.toLower.toInt

    // Set the event-values
    val keys = ModifierKeys(e isShiftDown, e isControlDown, e isAltDown)

    // If the key is numeric then retrieve the Char value, otherwise get the int code.
    // Numeric keys aren't interpreted as digits if the int code is used (silly!)
    val code = if (e.getKeyChar.isDigit) e.getKeyChar else e.getExtendedKeyCode

    // Tests true if shift-, control- or alt key is pressed
    val isModifier = (code == 16 || code == 17 || code == 18)

    // If they key-code equals a modifier key, nothing bad can happen..
    val event = if (isModifier) {
      constructor(getCorrectCase(code), keys)

      // If it doesn't, check if a key is being hid away by
      // a modifier key and return the key it that's the case
    } else  {
      val result : Option[Int] =
        if      (e.isControlDown && !isModifier) Some(AWTKeyEvent.getKeyText(code).charAt(0))
        else if (e.isAltDown && !isModifier) Some(AWTKeyEvent.getKeyText(code).charAt(0))
        else None

      constructor(getCorrectCase(result.getOrElse(code)), keys)
    }

    event match {
      case KeyDown(Key.Plus, ModifierKeys.Control) => View.zoom(View.center, -5); None // Zoom in
      case KeyDown(Key.Minus, ModifierKeys.Control) => View.zoom(View.center, 5); None // Zoom out
      case KeyDown(Key.ArrowDown, ModifierKeys.Control) => View.panY(-1); None
      case KeyDown(Key.ArrowLeft, ModifierKeys.Control) => View.panX(-1); None
      case KeyDown(Key.ArrowRight, ModifierKeys.Control) => View.panX(1); None
      case KeyDown(Key.ArrowUp, ModifierKeys.Control) => View.panY(-1); None
      case _ => Some(event) // Return
    }
  }

  /**
   * Parses mouse-events by making sure the correct mouse button is pressed even in MouseMove and MouseDrag,
   * and filters away events that Siigna uses to zoom and pan.
   *
   * @param e  The AWT event to parse
   * @param constructor  The constructor for a Siigna event so we can distinguish between different mouse event types
   * @return Some[MouseEvent] if the event was parsed successfully and it wasn't consumed to pan or zoom
   */
  private def parseMouseEvent(e : AWTMouseEvent, constructor : (Vector2D, MouseButton, ModifierKeys) => Event) : Option[MouseEvent] = {
    // Saves the position of the mouse
    val point = Vector2D(e getX, e getY)

    // Retrieves and updates the previous point
    val delta = point - View.mousePositionScreen
    View.setMousePosition(point)

    // Saves the last mouse-button as a boolean in the case of a MouseDown event,
    // to be used to recognize MouseUp events (which normally aren't associated with buttons).
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
    val event = constructor(point, button, keys)

    // Pan (using middle button) and zoom (using wheel) or otherwise return the optional event
    event match {
      case MouseWheel(_, _, _, d)          => {
        if (Siigna.navigation) { View.zoom(point, d); None }
        else Some(MouseWheel(point, button, keys, d))
      }
      case MouseDown  (_, MouseButtonMiddle, _)    => None
      case MouseDrag  (_, MouseButtonMiddle, _)    => View.pan(delta); None
      case MouseMove  (_, _, ModifierKeys.Control) => View.pan(delta); None
      case MouseUp    (_, MouseButtonMiddle, _)    => View.pan(delta); None
      case MouseEnter (_, _, _) => Some(MouseEnter(point, button, keys))
      case MouseExit  (_, _, _) => Some(MouseExit(point, button, keys))
      case MouseDown  (_, _, _) => {
        /*if (ModuleMenu.isHighlighted(point)) {
          ModuleMenu.onMouseDown(point) // Give the event to the module menu
          None
        } else */ Some(MouseDown(point, button, keys))
      }
      case MouseUp    (_, _, _) => Some(MouseUp(point, button, keys))
      case MouseDrag  (_, _, _) => Some(MouseDrag(point, button, keys))
      case MouseMove  (_, _, _) => Some(MouseMove(point, button, keys))
      case _ => Log.error("EventController: Did not expect event: " + event); None
    }
  }

  /**
   * Sets up the event-listeners for the given canvas and forwards every event to the
   * [[com.siigna.app.controller.Controller]] for further processing.
   * @param canvas  The [[java.awt.Canvas]] from which the events originate.
   */
  def setupEventListenersOn(canvas : Canvas) {
    // Dispatches events to the controller
    def dispatch(e : Option[Event]) { e.foreach(this.!) }

    // Add event listeners
    canvas.addKeyListener(new KeyListener {
      override def keyPressed (e : AWTKeyEvent) { dispatch(parseKeyEvent(e, KeyDown)) }
      override def keyReleased(e : AWTKeyEvent) { dispatch(parseKeyEvent(e, KeyUp))   }
      override def keyTyped   (e : AWTKeyEvent) { }
    })
    canvas.addMouseListener(new MouseListener {
      override def mouseClicked (e : AWTMouseEvent) { }
      override def mouseEntered (e : AWTMouseEvent) { dispatch(parseMouseEvent(e, MouseEnter)) }
      override def mouseExited  (e : AWTMouseEvent) { dispatch(parseMouseEvent(e, MouseExit)) }
      override def mousePressed (e : AWTMouseEvent) { dispatch(parseMouseEvent(e, MouseDown)) }
      override def mouseReleased(e : AWTMouseEvent) { dispatch(parseMouseEvent(e, MouseUp)) }
    })
    canvas.addMouseMotionListener(new MouseMotionListener {
      override def mouseDragged(e : AWTMouseEvent) { dispatch(parseMouseEvent(e, MouseDrag)) }
      override def mouseMoved  (e : AWTMouseEvent) { dispatch(parseMouseEvent(e, MouseMove)) }
    })
    canvas.addMouseWheelListener(new MouseWheelListener {
      override def mouseWheelMoved(e : MouseWheelEvent) {
        // getPreciseWheelRotation is only available in 1.7
        val scroll = try {e.getPreciseWheelRotation} catch { case _ => e.getUnitsToScroll}
        dispatch(parseMouseEvent(e, MouseWheel(scroll)))
      }
    })
  }

}
