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

package com.siigna.module

import com.siigna.app.view.Graphics
import com.siigna.app.view.event.{Event, EventParser}
import com.siigna.util.geom.TransformationMatrix
import com.siigna.app.view.ModuleInterface

/**
 * Defines the parent class for all modules.
 * Modules are basically immutable descriptions of what to do in case of certain events based on the state map.
 * The <b>state map</b> describes where to go when a certain event pops in in a certain state.
 * Format: State -> List[Event] -> State.
 * 
 * <br />
 * Modules are serialized so they can be sent, received and correctly read.
 */
trait Module extends Serializable {
  
  /**
   * The EventParser that parses the event-stream from the given snap- and
   * track-settings.
   */
  val eventParser : EventParser = new EventParser()

  /**
   * An interface the module can utilize to communicate with the controller and
   * view.
   */
  var interface : ModuleInterface = new ModuleInterface(this)

  /**
   * A boolean value stating whether the module is active.
   */
  var isActive = false

  /**
   * The current mouse position interpreted by the module. This coordinate takes snap and track
   * into account and is thus not the same as the mouse position seen on the screen.
   * @return
   */
  def mousePosition = eventParser.mousePosition

  /**
   * The current state of the module given by a symbol. Every module always starts in 'Start.
   */
  final var state : Symbol = 'Start

  /**
   * <p>The state map and "heart" of the module. This state map describes all the states that exists in the module
   * and all the ways the user can leave these states and enter new ones via [[com.siigna.app.view.event.Event]]s.
   * A StateMap is defined as various states, describes as symbols, that are mapped to a PartialFunction. This
   * function various code-blocks that are executed only if the input are right (think case matches).</p>
   *
   * <p>The old implementation didn't allow functions to execute (no conditionality) which proved to be
   * insufficient - we simply did not have enough control. That lead us to implement a Goto statement. That did not
   * end well. This implementation provides a much better control for the event-flow and thus much richer
   * interactions between the states.</p>
   *
   * <p>The method thus defines the way it wishes to handle the events given. The stateMap is used in the
   * controller where we "walk" the state map and executes the functions associated with each state.</p>
   */
  def stateMap : Map[Symbol, PartialFunction[List[Event], Symbol]]

  /**
   * A function available for all Modules to paint their content.
   *
   * @param graphics  The graphics object available to paint on.
   * @param transformation  The transformationMatrix on which the current screen-transformations are saved.
   */
  def paint(graphics: Graphics, transformation : TransformationMatrix) { }

  /**
   * Returns the simple name of the module.
   */
  override def toString = this.getClass.getSimpleName.replace("$", "")

}