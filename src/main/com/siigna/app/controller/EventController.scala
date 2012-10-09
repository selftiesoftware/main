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

package com.siigna.app.controller

import command.ForwardTo
import com.siigna.util.logging.Log
import com.siigna.app.view.event.{Key, KeyDown, ModuleEvent, Event}
import com.siigna.module.Module

/**
 * A controller for events.
 *
 * $controlHierarchy
 */
trait EventController extends ModuleController {

  def apply(event : Event) {
    if (modules.size > 0) {

      // Retrieve module
      val module : Module = modules.top

      // Catch any Escape-key downs to change the state to 'end (quits the module)
      event match {
        case KeyDown(Key.Escape, _) => module.state = 'End
        case _ => // Do nothing
      }

      // Examine if the module has not yet been imported
      // Parse the events
      events = module.eventParser.parse(event :: events)

      // React on the event parsed and execute the function associated with the state;
      // These lines are in a try-catch loop in case anything goes wrong in a module.
      // Since modules are prone to error we need to make sure they don't break the entire program.
      val result : Any = try {
        // Retrieve the function from the map and apply them if they exist
        module.stateMap.get(module.state) match {
          case Some(f) if (f.isDefinedAt(events)) => {
            f(events) match {
              case s : Symbol => module.state = s
              case _ => // Do nothing
            }
          }
          case None => // Do nothing
        }
      } catch {
        case e => {
          Log.error("Error in retrieving state map from module " + module + ". Shutting down!", e)
          stopModule(module, false)
        }
      }

      // If the module is ending then stop the module and match on the resulting event
      // If it was a ModuleEvent then send it back into the event-queue for other modules
      // to respond on.
      if (module.state == 'End) {
          val continue = result match {
          // Put a module event back in the event queue
          case moduleEvent : ModuleEvent => {
            this ! moduleEvent
            false
          }
          case unknown => {
            Log.debug("Controller: Received object " + unknown + " from the ending module " + module +
              ", but not reacting since it is not a Module Event.")
            true
          }
        }

        // Stop the module
        stopModule(module, continue)
      }
    } else {
      Log.warning("Controller: No modules in the controller. Trying to forward to Default.")
      ForwardTo('Default)
    }
  }

}
