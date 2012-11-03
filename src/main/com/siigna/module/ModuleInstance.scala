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

import com.siigna.util.event._
import actors.Future
import actors.Futures._
import com.siigna.util.logging.Log
import com.siigna.util.event.KeyUp
import com.siigna.util.event.KeyDown
import scala.Some


/**
 * <p>
 *   A ModuleInstance is an entry in a [[com.siigna.module.ModulePackage]].
 *   This class is made to make sure that we can identify different Modules with same name from each other. A
 *   ModuleInstance allows us to plug several modules from different packages into the same running instance
 *   of Siigna.
 * </p>
 *
 * <p>
 *   In effect the ModuleInstance works as a wrapper to [[com.siigna.module.Module]]s, so we can stow all runtime
 *   information away from the modules. The ModuleInstance is responsible for making sure the module will run and
 *   is loaded from the right resources. The [[com.siigna.util.event.Event]]s that the modules process are also
 *   given to the ModuleInstance before handed over to the module since the ModuleInstance might forward
 * </p>
 *
 * <p>
 *   To provide the right information make sure the class path is given as the exact folder-location in the .jar file,
 *   nothing else. Similarly the class name should be the name of the class. So if I were to load a class with the full
 *   name of ''com.siigna.module.base.Default'' the class path would be ''com.siigna.module.base'' and the
 *   class name would be ''Default''.
 * </p>
 *
 * @param name  The name of the class (e. g. ''Default'' - without .class)
 * @param classPath  The class path to the module (e. g. ''com.siigna.module.base'')
 */
final case class ModuleInstance(name : Symbol, classPath : String) {

  // The module class itself
  private var _module : Option[Module] = None

  /**
   * The [[com.siigna.module.Module]] loaded via the given [[com.siigna.module.ModulePackage]]. If the module has
   * not been loaded before, we attempt to do so. This can result in a small load-time, but it can't really be avoided.
   * If something goes wrong we return a dummy-module to prevent everything from breaking.
   * @return  A [[com.siigna.module.Module]] that can either contain the information we wanted or a simple
   *          dummy-module that does... nothing.
   */
  def module : Module = {
    if (_module.isDefined) _module.get
    else {
      _module = Some(ModuleLoader.load(this))
      _module.get
    }
  }

  /**
   * The forwarding module, if any
   */
  protected var child : Option[ModuleInstance] = None

  /**
   * The current state of the module given by a symbol. Every module always starts in 'Start.
   */
  var state : Symbol = 'Start

  /**
   * Passes the given events on to the underlying module(s) and processes them as described in their state machine.
   * @param events  The events from the user
   */
  def apply(events : List[Event]) : Option[ModuleEvent] = {
    // Proceed if the child is defined
    if (child.isDefined) {
      // Give the events to the child
      child.get.apply(events) match {
        // A message was received for us to pass on
        case Some(m : Message[_]) => {
          // Give the message to this module to act upon
          parse(m :: events)

          // Return None so the parent doesn't react on the message too
          None
        }
        // The child ended with a message
        case Some(m : End[_]) => {
          val name = child.get.toString

          // Remove the child
          child = None

          // Stop painting the child
          module.interface.unchain()

          // Log it
          Log.debug("Module '" + module + "': Ended module " + name + " with message " + m.message)

          // Continue to run the current module so it can react
          parse(m :: events)

          // Return None so the parent doesn't react on the message too
          None
        }
        // The return value was not recognized, nothing should happen
        case _ => None
      }
    // Otherwise we handle the events inside this module
    } else parse(events)
  }

  /**
   * Parses the given events inside the current module
   * @param events The list of events to use
   */
  protected def parse(events : List[Event]) : Option[ModuleEvent] = {
    // The event to return
    var event : Option[ModuleEvent] = None

    // Quit if the user presses escape
    events match {
      case KeyUp(Key.Escape, _) :: KeyDown(Key.Escape, _) :: tail => return Some(End)
      case _ =>
    }

    // Parse the events
    val parsedEvents = module.eventParser.parse(events)

    // React on the event parsed and execute the function associated with the state;
    // These lines are in a try-catch loop in case anything goes wrong in a module.
    // Since modules are prone to error we need to make sure they don't break the entire program.
    try {
      // Retrieve the function from the map and apply them if they exist
      module.stateMap.get(state) match {
        case Some(f) if (f.isDefinedAt(parsedEvents)) => {
          f(parsedEvents) match {
            // Forward to a module
            case s : Start[_] => {
              // Try to load the module
              child = Some(s.module)

              // Start painting the module
              module.interface.chain(s.module.module.interface)

              // Send the start message on to the child
              child.get.apply(s :: events)

              // Log success
              Log.debug("Module '" + module + "': Forwarded to " + s.module)
            }
            // Set the state
            case s : Symbol if (module.stateMap.contains(s)) => state = s
            // If module returns a ModuleEvent (e. g. End), return it immediately
            case e : ModuleEvent => event = Some(e)
            case e => // Function return value does not match: Do nothing
          }
        }
        case e => // No state defined: Do nothing
      }
    } catch {
      case e : Exception => {
        Log.error(toString + ": Error when executing state " + state + " with events " + parsedEvents + ".", e)
      }
    }

    // Return the event
    event
  }

  /**
   * Returns the <code>name</code> parameter of the ModuleInstance as a String.
   * @return  A String. Neat, right? :-)
   */
  override def toString = classPath + "." + name.name

}
