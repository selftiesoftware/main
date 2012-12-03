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

package com.siigna.util.event

import java.awt.event.{KeyEvent => AWTKeyEvent}

import com.siigna.app.Siigna
import com.siigna.app.view.{View, Graphics}
import collection.parallel.immutable.{ParMap, ParIterable}
import com.siigna.util.geom.{Vector2D, TransformationMatrix}
import com.siigna.app.model.{Drawing, Model}
import com.siigna.app.model.shape.{CircleShape, Shape}

/**
 * <p>
 *   An <code>EventParser</code> that analyses a given list of events, and returns an
 *   optimized and edited version in accordance with the given track and snap
 *   settings.
 * </p>
 *
 * <p>
 *   For each module an <code>EventParser</code> is attached, which filters every event
 *   that comes in. The events are filtered in each module in the stack to make sure the events are parsed
 *   according to the rules of the previous modules.
 * </p>
 *
 * <p>
 *   Event parses often analyses shapes close to the position of the mouse/pointer. These shapes are retrieved
 *   from the [[com.siigna.app.model.Drawing]] which is only altered through [[com.siigna.app.model.action.Action]]s.
 *   If you wish to add shapes that has not yet been stored in the drawing it's possible to add the tracking
 *   of shapes via the <code>snapTo</code> method. Given a function that returns a shape (the shape might depend on
 *   various stuff from a module - hence a function) the modules can add shapes to the snap and track parsers.
 * </p>
 * @see [[com.siigna.app.view.ModuleInterface]] - Graphical interface for modules
 *      [[com.siigna.module.Module]] - The module trait
 */
class EventParser {

  // The default snap-modules.
  protected val defaultSnap : Seq[EventSnap] = Seq(CenterPoints, MidPoints, EndPoints)

  // A boolean value of whether the EventParser is enable or not. Defaults to true.
  protected var enabled : Boolean = true

  /**
   * The events attached to the event parser. These events have been parsed one-by-one which means that they are
   * parsed in accordance with what the module want, but probably does not correspond to the events originated
   * from the user.
   * @return  A list of parsed events
   */
  var events : List[Event] = Nil

  // The most recent MouseMove or MouseDrag event received by the event-parser.
  var mousePosition : Vector2D = Vector2D.empty

  /**
   * The margin of the graphical query done by the parser i. e. how large a space is included in the search
   * for relevant shapes. Defaults to 5.
   */
  var margin : Double = 5

  /**
   * The size of the list the EventParser returns. Defaults to 10.
   * Edit this if you need larger event-lists for very complex modules. But note
   * that the size of the event-list given when the modules initiates depends on
   * the <code>listSize</code> variable in the previous module's EventParser.
   * Thus it's only inside the current module that the list-sizing is applicable.
   *
   * This value should be positive.
   */
  var maxNumberOfEvents : Int = 10

  // The current EventTracker.
  private var track : EventTrack = Track

  /**
   * The current EventSnaps.
   * Since it's possible that the user wants to snap to various kind of points,
   * we allow the user to have several snap-settings with a HashSet.
   * Defaults to: CenterPoints, MidPoints and EndPoints.
   */
  private var snap = defaultSnap

  // A snap model for snapping to temporary or not-yet-created shapes
  private var snapModel : Seq[() => Traversable[Shape]] = Nil

  /**
   * Clears any snap-modules that is not a part of the default snap set.
   */
  def clearSnap() {
    snap = defaultSnap
  }

  /**
   * Disables the EventParser. The EventParser is enabled per default.
   */
  def disable() { enabled = false }

  /**
   * Enables the EventParser. The EventParser is enabled per default.
   */
  def enable() { enabled = true }

  /**
   * Examines whether the EventParser is snapping to the given Snap.
   *
   * @param snapper  The snap we are examining.
   */
  def isSnapping(snapper : EventSnap) = snap.exists(_ == snapper)

  /**
   * Examines whether the EventParser is tracking or not.
   */
  def isTracking = track.isTracking

  /**
   * Let the track and the snappers paint. This is handy if a track, for instance, wishes to show some kind
   * of guideline as to where the current events are tracked to.
   */
  def paint(graphics : Graphics, transformation : TransformationMatrix) {
    if(Snap.snapEnabled == true) snap.foreach(_.paint(graphics, transformation))
  }

  /**
   * Parse a given list of Events.
   * The events are analysed in the following order:
   * <ol>
   *   <li>
   *     Merges any sequences of events that can be merged without loss of
   *     data (e. g. two MouseMove events in a row) into a more optimized
   *     version.
   *     This optimization is done in every module by default, since every
   *     module has their own specific list of events.
   *
   *     In specifics we remove unknown KeyEvents
   *     (<code>java.awt.KeyEvent.CHAR_UNDEFINED</code>) and merges two
   *     <code>MouseMove</code>/<code>MouseDrag</code> events into one (to
   *     avoid endless lists of <code>MouseMove</code>/<code>MouseDrag</code>).
   *     This is done to ease case matching on the event-stream.
   *   </li>
   *   <li>Run the event-list through the EventTrack.</li>
   *   <li>Run the event-list through the EventSnap.</li>
   *   <li>Slice the list down to the size defined in <code>maxNumberOfEvents</code>
   *     and return the list.</li>
   *   <li>Set the mouse position of the current parser to the (possibly) altered event.</li>
   * </ol>
   */
  def parse(event : Event) : List[Event] = {
    // Store the event as the head of the events (max 10)
    events = (event :: events).take(maxNumberOfEvents)

    // Merges any sequences of events that doesn't provide additional info.
    events = events match {
      // Removes unknown KeyEvents.
      case KeyDown(AWTKeyEvent.CHAR_UNDEFINED, _) :: tail => tail
      case KeyUp(AWTKeyEvent.CHAR_UNDEFINED,_ ) :: tail   => tail
      // Merges mouse events to avoid ridiculously long lists of MouseMove or MouseDrag.
      case (e : MouseMove) :: (_ : MouseMove) :: tail => e :: tail
      case (e : MouseDrag) :: (_ : MouseDrag) :: tail => e :: tail
      case _ => events
    }

    if (enabled) {
      events = {
        // Perform 2D query and add any custom additions
        val model = Drawing(View.mousePosition.transform(View.deviceTransformation), margin).values ++ snapModel.flatMap(_())

        // Parse the track
        var newEvent = track.parse(events, model)

        // Parse the snap
        if(Snap.snapEnabled == true) {
          snap foreach {a => newEvent = a.parse(newEvent, model)}
        }

        // Return the edited list and slice the list to the size defined in <code>listSize</code>.
        newEvent :: events.tail.slice(0, maxNumberOfEvents - 1)
      }
    }

    // Set (the snapped) mouse position
    events match {
      case (e : MouseMove) :: tail => mousePosition = e.position
      case (e : MouseDrag) :: tail => mousePosition = e.position
      case _ =>
    }

    events
  }

  /**
   * Stop snapping to the given event snap. If the module is nowhere to be found in the snap
   * Seq, nothing happens.
   */
  def removeSnap(snap : EventSnap) {
    this.snap = this.snap.filterNot(_ == snap)
  }

  /**
   * Add an EventSnap to the end of the event parser. Every event passing through the event-parser will
   * perform the snap-functionality given in the snap.
   */
  def snapTo(snap : EventSnap) { this.snap = this.snap :+ snap }

  /**
   * Snaps to the given function, returning a shape. Useful for adding shapes that has not yet been saved in the
   * Drawing to the dynamic snap method.
   * @param shape  The function returning the shape the event parser should snap to.
   */
  def snapTo(shape : () => Traversable[Shape]) { snapModel :+= shape }

  /**
   * Track to a given track module.
   */
  def trackTo(track : EventTrack) { this.track = track }

}

/**
 * The abstract class for every <code>EventSnaps</code>.
 * An EventSnap takes a single event, runs it up against a given sequence of shapes
 * and returns a new event according to the snap-setting.
 */

abstract class EventSnap {

  /**
   * The margin of the graphical query done by the parser i. e. how large a space is included in the search
   * for relevant shapes. Defaults to 5.
   */
  var margin = 5

  /**
   * Paint stuff.
   * @param graphics  A [[com.siigna.app.view.Graphics]] object that can paint shapes and objects.
   * @param transformation  A [[com.siigna.util.geom.TransformationMatrix]] that can transform shapes to and from
   *                        device and drawing coordinates (see [[com.siigna.app.view.View]]).
   */
  def paint(graphics : Graphics, transformation : TransformationMatrix) {}

  /**
   * Parses an event by the given snap-settings.
   * @param event  The event to parse.
   * @param model  A Model containing shapes the parser can choose to react to.
   * @return  An event that might have been changed by the parser to fit the parsing rules.
   */
  def parse(event : Event, model : Traversable[Shape]) : Event
}

/**
 * The abstract class for every <code>EventTracks</code>.
 * An EventTrack takes an entire list and searches for a series of events that
 * can apply to any of the track-setting. If that is the case, the track sets
 * <code>isTracking</code> to true and returns a new event that fits the track.
 */
abstract class EventTrack {

  /**
   * Whether or not the EventTrack is tracking.
   */
  def isTracking : Boolean

  /**
   * Paint stuff.
   * @param graphics  A [[com.siigna.app.view.Graphics]] object that can paint shapes and objects.
   * @param transformation  A [[com.siigna.util.geom.TransformationMatrix]] that can transform shapes to and from
   *                        device and drawing coordinates (see [[com.siigna.app.view.View]]).
   */
  def paint(graphics : Graphics, transformation : TransformationMatrix) {}

  /**
   * Parses a list into a single event.
   * @param events  The events to parse.
   * @param model  A Model containing shapes the tracker can choose to react to.
   * @return  A single event (representing the latest event in the given list) that might have been
   *          changed by the parser to fit the parsing rules.
   */
  def parse(events : List[Event], model : Traversable[Shape]) : Event

}