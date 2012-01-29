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
import com.siigna.app.view.event.EventHandler
import com.siigna.app.view.event.EventParser
import com.siigna.util.geom.TransformationMatrix
import com.siigna.app.view.{Display, ModuleInterface}

/**
 * Defines the parent class for all modules.
 * Modules are basically immutable descriptions of what to do in case of certain events. Based on a state map and a
 * state machine.
 * <br />
 * The <b>state map</b> describes where to go when a certain event pops in in a certain state. Format: State -> Event -> State.
 * <br />
 * The <b>state machine</b> describes what actions to take in each state.
 * 
 * <br />
 * Modules are serialized so they can be sent, received and correctly read.
 */
trait Module extends Serializable {
  
  /**
   * The EventParser that parses the event-stream from the given snap- and
   * track-settings.
   */
  final val eventParser : EventParser = new EventParser()

  /**
   * An interface the module can utilize to communicate with the controller and
   * view.
   */
  var interface : ModuleInterface = new ModuleInterface()

  /**
   * A boolean value stating whether the module is active.
   */
  var isActive = false

  /**
   * The current state of the module given by a symbol. Every module always starts in 'Start.
   */
  final var state : Symbol = 'Start

  /**
   * The method defines the event handler the module want to use. It is sent
   * to the controller that "walks" the state diagram and executes actions
   * associated with each state.
   */
  def eventHandler : EventHandler

  /**
   * A function available for all Modules to paint on.
   *
   * @param graphics  The graphics object available to paint on.
   * @param transformation  The transformationMatrix on which the current screen-transformations are saved.
   */
  def paint(graphics: Graphics, transformation : TransformationMatrix) { }

  /**
   * Returns the simple name of the module.
   */
  override def toString = this.getClass.getSimpleName

}