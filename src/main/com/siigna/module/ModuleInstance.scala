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
import com.siigna.util.logging.Log
import com.siigna.util.event.KeyUp
import com.siigna.util.event.KeyDown
import scala.Some
import com.siigna.app.view.View

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
 * @param module  The module that the instance communicates with
 */
final case class ModuleInstance(name : Symbol, module : Module) {

  /**
   * The forwarding module, if any
   */
  protected var _child : Option[ModuleInstance] = None

  /**
   * The current state of the module given by a symbol. Every module always starts in 'Start.
   */
  var state : Symbol = 'Start

  /**
   * Passes the given events on to the underlying module(s) and processes them as described in their state machine.
   * @param event  The events from the user
   */
  def apply(event : Event) : Option[ModuleEvent] = {
    // Parse the events
    val events = module.eventParser.parse(event)

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
   * @return  Some[ModuleInstance] if the module is forwarding, None otherwise
   */
  def child = _child

  /**
   * Copies the ModuleInstance to avoid using old duplicates of modules.
   * @return  A new ModuleInstance
   */
  def copy = {
    new ModuleInstance(name, module.getClass.newInstance().asInstanceOf[Module])
  }

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
      module.stateMap.get(state) match {
        case Some(f) if (f.isDefinedAt(events)) => {
          f(events) match {
            // Forward to a module
            case s : Start[_] => {
              // Try to load the module
              _child = Some(s.module)

              // Start painting the module
              module.interface.chain(s.module.module.interface)

              // Set the events of the new event parser
              s.module.module.eventParser.events = events

              // Set the mouse position of the new event parser
              s.module.module.eventParser.mousePosition = View.mousePosition

              // Log success
              Log.debug("Module '" + module + "': Forwarding to " + s.module)

              // Let the child react on the start
              moduleEvent = parseChild(s :: events)
            }
            // Set the state
            case s : Symbol if (module.stateMap.contains(s)) => state = s
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
      module.interface.unchain()

      Log.debug("Module '" + module + "': Ended module " + name +
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
   * Returns the <code>name</code> parameter of the ModuleInstance as a String.
   * @return  A String. Neat, right? :-)
   */
  override def toString = name.name

}

/**
 * Companion object for easy loading of [[com.siigna.module.Module]]s.
 */
object ModuleInstance {

  /**
   * Creates a ModuleInstance with the given name and class-path. If the module could not be found, a dummy-module
   * is inserted instead...
   * @param name  The name of the module, e. g. 'Menu
   * @param classPath  The path of the module, e. g. "com.siigna.module.base"
   * @return  A ModuleInstance
   */
  def apply(name : Symbol, classPath : String) : ModuleInstance =
    new ModuleInstance(name, ModuleLoader.load(name, classPath))

}
