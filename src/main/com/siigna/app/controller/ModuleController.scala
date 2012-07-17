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

import com.siigna.app.view.event.Event
import collection.mutable.Stack
import com.siigna.module.Module
import remote.RemoteController
import com.siigna.app.Siigna
import com.siigna.util.logging.Log
import actors.Actor

/**
 * A trait containing definitions for variables and methods needed in various controllers such as
 * the [[com.siigna.app.controller.CommandController]] or the [[com.siigna.app.controller.EventController]].
 *
 * @define controlHierarchy
 * The control-hierarchy is as follows:
 * <pre>
 *         ModuleController
 *                 |
 *          EventController
 *                 |
 *         CommandController
 *                 |
 *             Controller
 * </pre>
 */
trait ModuleController extends Actor {

  /**
   * The unique identifier for this client.
   */
  var client : Option[Client] = None  

  /**
   * The last 10 events
   */
  protected var events : List[Event] = List()

  /**
   * The Module Bank used to fetch modules from external sources.
   */
  protected def moduleBank : ModuleBank

  /**
   * The stack of active Modules, ranging from the "oldest" in the bottom of the stack to the newest and active module
   * in the top. LIFO.
   */
  protected def modules : Stack[Module]

  /**
   * The remote controller to whom we can send actions.
   */
  protected def remote = RemoteController

  /**
   * Return the last 10 parsed events currently stored in the controller.
   */
  def getEvents = events

  /**
   * Initializes a module by setting the starting state to 'Start and chaining the modules
   * interface to the other interfaces (among other things this is the chain for painting -
   * see the <code>Interface</code> trait for more info.
   *
   * @param module  The module to initialize.
   * @param state  The state to start the module in. Defaults to 'Start
   * @return A Boolean value indicating whether the module was successfully initialized.
   */
  protected def startModule(module : Module, state : Symbol = 'Start) : Boolean = try {
    // Set the state to start!
    module.state = state

    // Chain the interface higher up in the hierarchy with the new interface
    // or set the active interface in Siigna if the interface hasn't been defined
    if (modules.size > 1) {
      modules(modules.size - 1).interface.chain(module.interface)
    } else if (Siigna.getInterface.isEmpty) {
      Siigna.setInterface(module.interface)
    }

    // Unchain the module if it happens to be chained
    if (module.interface.isChained)
      module.interface.unchain()

    // Tell the module that it's active!
    module.isActive = true

    // Return success!
    Log.success("Controller: Successfully initialized module: " + module)
    true
  } catch {
    // Log the failure
    case e => Log.warning("Controller: Failed to initialize the interface of " + module + ". The module will (probably) still run, but without a graphical output.", e)
    false
  }

  /**
   * Stops a module and enqueues the latest event for use in the module that takes over.
   * In other words we remove the most recent module and store it's events back in the queue so the next
   * control-loop if initiated and so the "parent" gets a chance to act.
   */
  protected def stopModule(module : Module, continue : Boolean) {
    if (modules.size > 1) {
      // Tell the module that it's no longer active
      module.isActive = false

      // Remove the ending module from the module stack.
      modules.pop()

      // Store the head of the event-list in the event-queue.
      if (continue) {
        this ! events.head

        // Store the tail of the events in the next module in the module stack.
        events = events.tail
      }

      // Initialize the module
      if (startModule(modules.top, modules.top.state))
        Log.success("Controller: Successfully ended " + module + ". Current module: "+modules.top)
      else
        Log.error("Controller: Sucessfully ended " + module + ", but unable to initialize the parent: " + modules.top)
    } else Log.error("Controller: Unable to stop module " + modules.head + " - it's the only module left.")
  }

  
}
