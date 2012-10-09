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

import com.siigna.app.view.event.{KeyUp, Key, KeyDown, Event}
import com.siigna.app.controller.modules.ModuleClassLoader
import actors.Future
import actors.Futures._
import com.siigna.util.logging.Log

/**
 * <p>
 *   A ModuleInstance is an entry in a [[com.siigna.module.ModulePackage]].
 *   This class is made to make sure that we can identify different Modules with same name from each other. A
 *   ModuleInstance allows us to plug several modules from different packages into the same running instance
 *   of Siigna.
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
  val module : Future[Module] = future { ModuleClassLoader.load(this) }

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
  def apply(events : List[Event]) {
    // Forward events if a child-module is available
    if (child.isDefined) {
      child.get.apply(events)

      // End the module if it's in state 'End
      if (child.get.state == 'End) {
        child = None
        module().interface.unchain()
      }

    // Otherwise we handle the events inside this module
    } else {
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
              case m : ModuleInstance => {
                child = Some(m)
                module.interface.chain(m.module().interface)
              }
              // Set the state
              case s : Symbol if (module.stateMap.contains(s))
                              => state = s
              case _ => // Function return value does not match: Do nothing
            }
          }
          case None => // No state defined: Do nothing
        }
      } catch {
        case e : Exception => {
          Log.error(toString() + ": Error when executing state " + state + " with events " + parsedEvents + ".", e)
        }
      }
    }
  }

  /**
   * Gets the full class path for the module.
   * @return  The class path concatenated with the class name with a "."
   */
  override def toString = classPath + "." + className

}
