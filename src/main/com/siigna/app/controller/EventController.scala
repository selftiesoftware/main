package com.siigna.app.controller

import java.awt.Canvas
import com.siigna.app.view.View
import java.awt.event.{KeyEvent => AWTKeyEvent, MouseEvent => AWTMouseEvent, _}
import com.siigna.app.view.event._
import com.siigna.util.geom.Vector2D
import com.siigna.app.Siigna
import com.siigna.app.view.event.MouseDown
import com.siigna.app.view.event.KeyDown
import com.siigna.app.view.event.MouseExit
import scala.Some
import com.siigna.app.view.event.MouseEnter
import com.siigna.app.view.event.KeyUp
import com.siigna.app.view.event.MouseMove
import com.siigna.app.view.event.MouseUp
import com.siigna.app.view.event.MouseDrag
import com.siigna.util.logging.Log

/**
 * The EventController is responsible for setting up event-listeners on the view
 */
trait EventController {

  private var mouseButtonLeft   = false
  private var mouseButtonMiddle = false
  private var mouseButtonRight  = false

  /**
   * Describes the current mouse-location when panning.
   */
  private var panPointMouse =  Vector2D(0, 0)

  /**
   * Describes the old panning-point, so the software can tell how much
   * the current panning has moved relative to the old.
   */
  private var panPointOld   = Vector2D(0, 0)

  /**
   * Parses key events by filtering modifier keys (shift, alt, control etc.) and converting them to a
   * format Siigna can understand.
   * @param e  The AWT event to parse
   * @param constructor  The constructor for the Siigna key event, to distinguish between KeyDown and KeyUp
   * @return A [[com.siigna.app.view.event.KeyEvent]] with information about the modifier keys, unless it has been
   *         eaten by Siigna
   */
  private def parseKeyEvent(e : AWTKeyEvent, constructor : (Int, ModifierKeys) => KeyEvent) : Option[KeyEvent] = {
    // Sets the correct casing of the character - i.e. make it uppercase if shift is pressed and vice versa
    def getCorrectCase(c : Int) = if (e.isShiftDown) c.toChar.toUpper.toInt else c.toChar.toLower.toInt

    // Set the event-values
    val keys = ModifierKeys(e isShiftDown, e isControlDown, e isAltDown)

    // If the key is numeric then retrieve the Char value, otherwise get the int code.
    // Numeric keys aren't interpreted as digits if the int code is used (silly!)
    val code = if (e.getKeyChar.isDigit) e.getKeyChar else e.getKeyCode

    // Tests true if shift-, control- or alt key is pressed
    val isModifier = (code == 16 || code == 17 || code == 18)

    // If they key-code equals a modifier key, nothing bad can happen..
    val event = if (isModifier) {
      constructor(getCorrectCase(code), keys)

      // If it doesn't check if a key is being hid by
      // a modifier key and return the key it that's the case
    } else  {
      val array  =
        if      (e.isControlDown && !isModifier) AWTKeyEvent.getKeyText(code).toCharArray
        else if (e.isAltDown && !isModifier) AWTKeyEvent.getKeyText(code).toCharArray
        else Array[Char]()
      if (!array.isEmpty) { // If there are elements in the character-array pass them on
        constructor(getCorrectCase(array(0)), keys)
      } else { // Otherwise we accept the original char for the event
        constructor(getCorrectCase(code), keys)
      }
    }

    event match {
      case KeyDown(Key.Plus, ModifierKeys.Control) => View.zoom(View.center, -5); None // Zoom in
      case KeyDown(Key.Minus, ModifierKeys.Control) => View.zoom(View.center, 5); None // Zoom out
      case KeyDown(Key.ArrowDown, ModifierKeys.Control) => View.panY(-1)
      case KeyDown(Key.ArrowLeft, ModifierKeys.Control) => View.panX(-1)
      case KeyDown(Key.ArrowRight, ModifierKeys.Control) => View.panX(1)
      case KeyDown(Key.ArrowUp, ModifierKeys.Control) => View.panY(-1)
      case _ => Some(event) // Return
    }
  }

  /**
   * Parses mouse-events by making sure the correct mouse button is pressed even in MouseMove and MouseDrag,
   * and filters away events that Siigna uses to zoom and pan.
   *
   * @param e  The AWT event to parse
   * @param constructor  The constructor for a Siigna event so we can distinguish between differet mouse event types
   * @return
   */
  private def parseMouseEvent(e : AWTMouseEvent, constructor : (Vector2D, MouseButton, ModifierKeys) => Event) : Option[MouseEvent] = {
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

    // Pan (using middle button) and zoom (using wheel) or otherwise return the optional event
    event match {
      case MouseWheel (point, _, _, delta)          => {
        if (Siigna.navigation) { View.zoom(point, delta); None }
        else Some(MouseWheel(toVirtual(point), button, keys, delta))
      }
      case MouseDown  (point, MouseButtonMiddle, _) => None
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
  }

  /**
   * Sets up the event-listeners for the given canvas and forwards every event to the
   * [[com.siigna.app.controller.Controller]] for further processing.
   * @param canvas  The [[java.awt.Canvas]] from which the events originate.
   */
  def setupEventListeners(canvas : Canvas) {
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
  }

}
