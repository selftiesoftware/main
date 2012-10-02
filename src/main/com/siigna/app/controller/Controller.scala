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

import collection.mutable.Stack

import com.siigna.app.controller.command._
import com.siigna.app.view.event.Event
import com.siigna.module.Module
import com.siigna.util.logging.Log
import remote._
import com.siigna.app.model.action.Action
import com.siigna.app.view.View
import com.siigna.app.model.Drawing

/**
 * The Controller controls the core of the software. Basically that includes
 * dealing with the event-flow to the modules.
 *
 * $controlHierarchy
 */
object Controller extends CommandController {

  protected val moduleBank = new ModuleBank()

  protected val modules = new Stack[Module]()

  /**
   * <p>The running part of the controller.</p>
   *
   * <p>It consists of a loop that exists until 'exit is given, or the system is exiting (naturally).</p>
   *
   * <p>In the loop we first examine whether there is pending events. If so we:
   * <ol>
   *   <li>Set the state of the active module.</li>
   *   <li>React on the given event by executing the state given by the state machine.</li>
   *   <li>Close a possibly ending module and ask the loop to repeat so the "parent" can answer.</li>
   *   <li>If the ending module returns a <code>ModuleEvent</code> then we put it back into the event queue, so other
   *       modules can react on it.</li>
   * </ol>
   *
   * The actor also handles commands and the 'exit symbol.</p>
   */
  def act() {
    // Start RemoteController
    RemoteController.start

    // Loop and react on incoming messages
    loop {
      react {

        // Handle actions (execute, not undo)
        case action : Action                   => remote ! (action, false)
          
        // Handle actions with an undo flag
        case (action : Action, undo : Boolean) => remote ! (action, undo)

        // Handle commands
        case command : Command => {
          Log.debug("Controller: Received command: " + command)
          // Handle it through the CommandController
          this(command)
        }

        // Handle events
        case event : Event => {
          Log.debug("Controller: Received event: " + event)
          this(event)
        }

        // Exit
        case 'exit => {
          Log.info("Controller is shutting down")

          // Quit the thread
          exit()
        }

        // Unknown
        case e => Log.warning("Controller: Received unknown input: " + e)
      }

      // Draw the view
      View.repaint()
    }
  }

  /**
   * Examines whether this client is connected with the server.
   * @return True if the connection has been established correctly, false otherwise.
   */
  def isOnline = remote.isOnline

}