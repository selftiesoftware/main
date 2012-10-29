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
 * @param pack  The [[com.siigna.module.ModulePackage]] in which the module lies
 * @param classPath  The class path to the module (e. g. ''com.siigna.module.base'')
 * @param className  The name of the class (e. g. ''Default'' - without .class)
 */
final case class ModuleInstance(pack : ModulePackage, classPath : String, className : Symbol) {

  /**
   * The [[java.util.jar.JarFile]] represented as a [[scala.actors.Future]]. Be careful to force-load the value
   * since it might block the calling thread.
   */
  val module : Future[Module] = future { ModuleLoader.load(this) }

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
  def apply(events : List[Event]) : List[Event] = {
    var endedChild = false

    def endChild() {
      val name = child.get.toString
      // Reset the state
      child.get.state = 'Start

      // Remove the child
      child = None

      // Stop painting the child
      this.module().interface.unchain()

      // Set flag
      // TODO: Do this in a smarter way
      endedChild = true

      Log.info("Module '" + this.module() + "': Ended module " + name)
    }

    // Forward events if a child-module is available
    val childEvents : List[Event] = if (child.isDefined) {
      // Give the events to the child and match on the output
      child.get.apply(events) match {
        case (m : End[_]) :: tail => {
          endChild()
          tail
        }
        case KeyUp(Key.Escape, _) :: KeyDown(Key.Escape, _) :: tail => {
          endChild()
          tail
        }
        case rest => rest // Do nothing
      }
    } else events

    // Otherwise we handle the events inside this module
    // This is separate from the previous if-statement because the child could have exited,
    // on which the parent (might) need to act
    if (child.isEmpty)
      parse(childEvents)
    else if (!endedChild) childEvents
    else Nil
  }

  /**
   * Parses the given events inside the current module
   * @param events The list of events to use
   */
  protected def parse(events : List[Event]): List[Event] = {
    // Force-load the module
    val module : Module = this.module()

    // Catch any Escape-keys to change the state to 'End (quits the module)
    events match {
      case KeyUp(Key.Escape, _) :: KeyDown(Key.Escape, _) :: tail => state = 'End
      case _ => // Do nothing
    }

    // Examine if the module has not yet been imported
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
            case Start(m) => {
              // Try to load the module
              child = Some(m)
              module.interface.chain(m.module().interface)
              Log.info("Module '" + this.module() + "': Forwarded to " +m)
            }
            // Set the state
            case s : Symbol if (module.stateMap.contains(s)) => {
              state = s
            }
            // If module returns an event, we append the given event. All will be returned
            case e: ModuleEvent => return e :: parsedEvents
            case e => // Function return value does not match: Do nothing
          }
        }
        case e => // No state defined: Do nothing
      }
    } catch {
      case e : Exception => {
        Log.error(toString() + ": Error when executing state " + state + " with events " + parsedEvents + ".", e)
      }
    }
    parsedEvents
  }

  /**
   * Gets the full class path for the module.
   * @return  The class path concatenated with the class name with a "."
   */
  override def toString = classPath + "." + className.name

}
