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

import com.siigna.app.view.{View, ModuleInterface, Graphics}
import com.siigna.util.event._
import com.siigna.util.geom.TransformationMatrix
import com.siigna.util.logging.Log
import scala.Some
import scala.Some
import scala.Some
import com.siigna.util.event.KeyUp
import com.siigna.util.event.KeyDown
import scala.Some

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
 * <p>
 *   Modules and collection of modules (called [[com.siigna.module.ModulePackage]]s) can be loaded in an out of
 *   Siigna at runtime. <a href="#moduleLoading">Read more below</a>.
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
 * <h2>Module Loading</h2>
 * <p>
 *   We define a Module as an entry in a [[com.siigna.module.ModulePackage]]. A Package can have many modules and
 *   these modules can be used and referenced in other packages. A Module can be loaded dynamically to import
 *   functionality into Siigna only when user needs it.
 * </p>
 *
 * <p>
 *   To load a module from a package, the module package needs to be loaded at first. This operation can be done via
 *   the [[com.siigna.module.ModulePackage]]. When that has been imported correctly a module can be referenced via the
 *   [[com.siigna.module.Module]] object. If you provide the Module object with the package in which the module lies
 *   and the name for the Module, you'll receive a Module instance.
 * </p>
 *
 * <h3>Naming convention</h3>
 * <p>
 *   The naming convention for the Modules and [[com.siigna.module.ModulePackage]]s are that each package should be
 *   placed within the package <code>com.siigna.module</code>. So if I were to have a package by the name of
 *   ''test'' I would place it at <code>com.siigna.module.test</code>. Each module can then be placed beneath that
 *   namespace. The naming of [[com.siigna.module.ModulePackage]]s should be camel case (initially lower case),
 *   to resemble regular Java / Scala packages. The naming of Modules should be pascal case (initial capital letter) to
 *   resemble the name of regular Java / Scala classes.
 * </p>
 *
 * @todo Elaborate documentation
 * @see http://en.wikipedia.org/wiki/Finite-state_machine
 * @see http://en.wikipedia.org/wiki/State_(computer_science)
 * @see http://docs.oracle.com/javase/tutorial/2d/index.html
 */
trait Module {

  /**
   * The forwarding [[com.siigna.module.Module]], if any.
   */
  final private var _child : Option[Module] = None

  /**
   * The EventParser that parses the event-stream from the given snap- and
   * track-settings.
   */
  lazy val eventParser : EventParser = new EventParser()

  /**
   * An interface the module can utilize to communicate with the controller and
   * view.
   */
  lazy val interface : ModuleInterface = new ModuleInterface(this)

  val count = 0

  /**
   * The current state of the module given by a symbol, representing the current node in the
   * [[com.siigna.module.Module#stateMap]]. Every module always starts in 'Start.
   */
  final var state : Symbol = 'Start

  /**
   * Passes the given events on to the underlying module(s) and processes them as described in their state machine.
   * @param event  The events from the user
   */
  def apply(event : Event) : Option[ModuleEvent] = {
    // Parse the events
    val events = eventParser.parse(event)

    if (_child.isDefined) {
      // Parse the child if it has been defined
      parseChild(events)
    } else {
      // Otherwise we handle the events inside this module
      parse(events)
    }
  }

/**
   * The child (if any) this module is forwarding to.
   * @return  Some[Module] if the module is forwarding, None otherwise.
   */
  final def child = _child

  /**
   * The current mouse position interpreted by the module. This coordinate takes snap and track
   * into account and is thus not (always) the same as the mouse position seen on the screen.
   * @return  A [[com.siigna.util.geom.Vector2D]] describing the current position of the cursor
   *          in the context of this module
   */
  def mousePosition = eventParser.mousePosition

  /**
   * Returns a new instance of the current module.
   * @return  A new instance of the same module.
   */
  protected def newInstance : Module = getClass.newInstance().asInstanceOf[Module]

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
   * Parses the given events inside the current module
   * @param events The list of events to use
   */
  protected def parse(events : List[Event]) : Option[ModuleEvent] = {
    // The event to return
    var moduleEvent : Option[ModuleEvent] = None

    // React on the event parsed and execute the function associated with the state;
    // These lines are in a try-catch loop in case anything goes wrong in a module.
    // Since modules are prone to error we need to make sure they don't break the entire program.
    try {
      // Retrieve the function from the map and apply them if they exist
      stateMap.get(state) match {
        case Some(f) if (f.isDefinedAt(events)) => {
          f(events) match {
            // Forward to a module
            case s : Start[_] => {
              // Try to load a new instance of the module
              _child = Some(s.module)

              // Start painting the module
              interface.chain(s.module.interface)

              // Set the events of the new event parser
              s.module.eventParser.events = events

              // Set the mouse position of the new event parser
              s.module.eventParser.mousePosition = View.mousePosition

              // Log success
              Log.debug("Module '" + toString+ "': Forwarding to " + s.module)

              // Let the child react on the start
              moduleEvent = parseChild(s :: events)
            }
            // Set the state
            case s : Symbol if (stateMap.contains(s)) => state = s
            // If module returns a ModuleEvent (e. g. End), return it immediately
            case e : ModuleEvent => moduleEvent = Some(e)
            case e => // Function return value does not match: Do nothing
          }
        }
        case e => // No state defined: Do nothing
      }
    } catch {
      case e : Exception => {
        Log.error(toString + ": Error when executing state " + state + " with events " + events + ".", e)
      }
    }

    // Return the module event
    moduleEvent
  }

  /**
   * Parses the events to the child. Assumes the child has been defined
   * @param events  The events to give to the child
   * @return Some[ModuleEvent] if something interesting occurred, None otherwise
   */
  protected def parseChild(events : List[Event]) = {
    // Stops the child
    def endChild(message : String = null){
      val name = child.get.toString

      // Remove the child
      _child = None

      // Stop painting the child
      interface.unchain()

      Log.debug("Module '" + toString + "': Ended module " + name +
        (if (message != null) " with message [" + message + "]." else ".") )
    }

    // Catch escape events
    events match {
      // Force-quit any module if we get two escape keys. Safety precaution if any module spins out of control
      // We chose to match on two clicks and not one to let the modules catch the single escape key
      case KeyUp(Key.Escape, _) :: KeyDown(Key.Escape, _) :: KeyUp(Key.Escape, _) :: KeyDown(Key.Escape, _) :: tail => {
        endChild("Caught Escape")
        None
      }
      // Otherwise we give the events to the child and match on the result
      case _ => _child.get.apply(events.head) match {
        // The child ended without a message
        // - also catches escape events
        case Some(End) => {
          endChild()

          // Continue to run the current module and return the result
          parse(End :: events)
        }

        // The child ended with a message
        case Some(m : End[_]) => {
          endChild(m.message.toString)

          // Continue to run the current module and return the result
          parse(m :: events)
        }
        // The return value was not recognized, nothing should happen
        case x => None
      }
    }
  }

  /**
   * Returns the simple name of the module.
   * @return  The simple name of the module without package information.
   */
  override def toString = this.getClass.getSimpleName.replace("$", "")

}

/**
 * A companion object to Module, capable of loading [[com.siigna.module.Module]]s from various locations and
 * [[com.siigna.module.ModulePackage]]s loaded into Siigna.
 *
 * To refer to modules, the convention is to use the relative path to the [[com.siigna.module.ModulePackage]] only.
 * So if a module "X" lies in the package ''test'' with the full class-path ''com.siigna.module.test.X'' we would write
 * <code>test</code> as the package and <code>X</code> as the class-path. If the full class-path were
 * ''com.siigna.module.test.sub.X'' the package would be the same, but the class-path to ''X'' would be
 * <code>sub.X</code>.
 */
object Module {

  /**
   * Finds and returns a [[com.siigna.module.Module]] with a given name located in the given package.
   * <br>
   * To load the module with the class path com.siigna.module.test.Test, stored in the package ''\'test'':
   * {{{
   *   Start('test, "Test")
   * }}}
   * The first ''\'test'' stands for the package, while the second ''"Test"'' stands for the name of the class itself.
   * Since the class is inside the ''test'' package we should not need to give further class-path information.
   *
   * <br>
   * To load a module with the class-path com.siigna.module.test.sub.Text, stored in the package ''\'test'':
   * {{{
   *   Start('test, "sub.Test")
   * }}}
   * Again the ''\'test'' stands for the package, but notice the addition of ''"sub"'' to the module class-path.
   *
   * @param packageName  The name of the package to search for the module.
   * @param classPath  The class-path to the module, relative to the package path.
   * @return Some[Module] if the module could be found, None otherwise.
   */
  def apply(packageName : Symbol, classPath : String) : Option[Module] = ModuleLoader.load(packageName, classPath)


}