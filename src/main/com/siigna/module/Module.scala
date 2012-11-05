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

import com.siigna.app.view.{ModuleInterface, Graphics}
import com.siigna.util.event.{Start, Event, EventParser}
import com.siigna.util.geom.TransformationMatrix

/**
 * Defines the parent class for all modules
 * <h2>Modules</h2>
 * <p>
 *   Modules are basically immutable descriptions of what to do in case of certain events based on the state map
 *   (finite state machine/automaton). In other words a Module tells Siigna exactly what to do in which situations,
 *   depending on the events the Module is (and has been) given. This is very handy because modules needs to be able
 *   to react differently depending on what the user does. So, for instance, a <code>Distance</code>  module needs
 *   to measure distances in different places based on the given [[com.siigna.util.event.MouseEvent]]s.
 *   <a href="#stateMap">Read more below</a>.
 * </p>
 * <p>
 *   Apart from the state map a module is also able to paint on a canvas to display useful information and fancy
 *   graphics. This is achieved through the <code>paint</code> method which modules <i>may</i> choose to implement.
 *   <a href="#painting">Read more below</a>.
 * </p>
 * <p>
 *   Last but not least modules needs to be able to parse events before they enter the module since
 *   [[com.siigna.util.event.Snap]] or [[com.siigna.util.event.Track]] might be enabled.
 *   <a href="#eventParser">Read more below</a>.
 * </p>
 *
 * <h3 id="stateMap">The state map</h3>
 * <p>
 *   The <b>state map</b> is a map of states and functions that describes where to go when a certain event is received
 *   by the module. So, depending on what state the module is currently in, different functionality is executed.
 *   <br>
 *   To be correctly defined, a state map should contain at least a <code>'Start</code> state,
 *   so the [[com.siigna.app.controller.Controller]] can start the module. Modules can be ended by using the
 *   [[com.siigna.util.event.End]] class to signal that it would like to stop. A value can be passed on to
 *   [[com.siigna.util.event.End]] so the module can end with or without a return value. If the user presses the
 *   escape key the module is ended, no matter which state it is in.
 * </p>
 * <p>
 *   The format of the state map is: <code>State -> List[Event] -> State</code>.
 *   See [[com.siigna.module.State]] for more information.
 * <p>
 *
 * <h3 id="painting">Painting</h3>
 * <p>
 *  Painting in Siigna is controlled from the [[com.siigna.app.view.View]] and outwards due to various reasons.
 *  This means that the module does not have any control over when the paint method is being called, so the module
 *  should make sure to continuously update the artifacts it needs to print!
 * </p>
 * <p>
 *   To avoid the ugly Java API we have constructed our own [[com.siigna.app.view.Graphics]] object which has a
 *   number of methods to paint different graphical objects. Along with the Graphics object, a
 *   [[com.siigna.util.geom.TransformationMatrix]] is sent to the method as a parameter which can be used to
 *   transform the shapes, so they fit on the screen. More on this in the documentation of the
 *   [[com.siigna.app.view.View]] and [[com.siigna.app.view.Graphics]].
 * </p>
 *
 * <h3 id="eventParser">The [[com.siigna.util.event.EventParser]]</h3>
 * <p>
 *  There are two different kinds of parsing: [[com.siigna.util.event.Track]]ing and snapping
 *  ([[com.siigna.util.event.EventSnap]]). Snap is a way to "glue" the cursor to object already on the
 *  screen if the cursor is close. This is used to make it easier for the user to make coherent drawings.
 *  Track is a way to remember which points the user has snapped to, so we can snap to lines expanding from these
 *  points.
 * </p>
 * <p>
 *   Both types of parsing happens before the event is given to the module. Both snap and track can be disabled in
 *   the module if you wish.
 * </p>
 *
 * <h2>Overriding or replacing default behaviour</h2>
 * <p>
 *   It is possible to override the default behaviour for modules and [[com.siigna.module.ModulePackage]]s. Every
 *   time a ModulePackage is loaded we check for a class called <code>ModuleInit</code> in the
 *   <code>com.siigna.module</code> package. If this class exist we assume that it can work as the base of the package
 *   and will override every other init-modules previously implemented.
 * </p>
 * <p>
 *   So: If a [[com.siigna.module.ModulePackage]] wishes to define or override the
 *   initializing module, <b>a module needs to be placed in the <code>com.siigna.module</code>
 *   package under the name <code>ModuleInit</code>!</b>. If it is not, the init module will not work as intended.
 * </p>
 *
 * @todo Elaborate documentation
 * @see http://en.wikipedia.org/wiki/Finite-state_machine
 * @see http://en.wikipedia.org/wiki/State_(computer_science)
 * @see http://docs.oracle.com/javase/tutorial/2d/index.html
 */
trait Module {

  /**
   * The EventParser that parses the event-stream from the given snap- and
   * track-settings.
   */
  final val eventParser : EventParser = new EventParser()

  /**
   * An interface the module can utilize to communicate with the controller and
   * view.
   */
  final var interface : ModuleInterface = new ModuleInterface(this)

  /**
   * The current mouse position interpreted by the module. This coordinate takes snap and track
   * into account and is thus not the same as the mouse position seen on the screen.
   * @return  A [[com.siigna.util.geom.Vector2D]] describing the current position of the cursor
   *          in the context of this module
   */
  final def mousePosition = eventParser.mousePosition

  /**
   * <p>The state map and "heart" of the module. This state map describes all the states that exists in the module
   * and all the ways the user can leave these states and enter new ones via [[com.siigna.util.event.Event]]s.
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
  def stateMap : StateMap

  /**
   * A function available for all Modules to paint their content. Override this method if you wish to paint
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

/**
 * A companion object to Module, capable of creating [[com.siigna.module.ModuleInstance]]s which is needed to
 * instantiate [[com.siigna.module.Module]]s.
 */
object Module {

  /**
   *
   * Creates a [[com.siigna.module.ModuleInstance]] of a module with the given name and class-path and
   * returns it, so the controller can load and the new module. This is useful when modules needs to
   * wrap the underlying understanding of [[com.siigna.module.ModuleInstance]]s and
   * [[com.siigna.module.ModulePackage]]s away and maintain the simple module semantic.
   *
   * @param name  The name of the module
   * @param classPath  The class path of the module
   * @return A [[com.siigna.module.ModuleInstance]] to be read by the controller.
   */
  def apply(name : Symbol, classPath : String) : ModuleInstance = ModuleInstance(name, classPath)


}