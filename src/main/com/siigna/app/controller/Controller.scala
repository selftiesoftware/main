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
import com.siigna.module.{ModulePackage, ModuleInstance}
import com.siigna.util.logging.Log
import com.siigna.app.model.action.Action
import com.siigna.app.view.View
import remote.RemoteController
import actors.Actor
import com.siigna.app.Siigna

/**
 * <p>
 *   The Controller contains the core application logic in Siigna. Fortunately the only logic in Siigna is to forward
 *   events on to the [[com.siigna.module.Module]]s and forward [[com.siigna.app.model.action.Action]]s from the
 *   [[com.siigna.app.model.Drawing]] and onto the main server and to other clients. Simply, right?
 * </p>
 */
object Controller extends Actor {

  /**
   * <p>
   *   The running part of the controller handling [[com.siigna.app.model.action.Action]]s and
   *   [[com.siigna.app.view.event.Event]]s. If an action is sent to the controller it is forwarded to the
   *   RemoteController to be passed on to the remote system and other clients. Actions can be sent as a
   *   <code>(Action, Boolean)</code> [[scala.Tuple2]]. The second boolean parameter indicates whether the action
   *   should be undone (true) or simply executed(false).
   * </p>
   *
   * <p>
   *   The Controller does not quit until 'exit is sent to it, or the system is exiting (thread death).
   * </p>
   *
   * The actor also handles commands and the 'exit symbol.</p>
   */
  def act() {
    // Start RemoteController
    RemoteController.start()

    // Set the base modules to local (TEST)
    ModuleLoader.base = ModulePackage('base, "c:/workspace/siigna/main/out/artifacts", "base.jar", true)

    def defaultModule = ModuleInstance(ModuleLoader.base, "com.siigna.module.base", 'Default)

    Siigna.setInterface(defaultModule.module().interface)

    var events : List[Event] = Nil

    // Loop and react on incoming messages
    loop {
      react {

        // Handle actions (execute, not undo)
        case action : Action                   => RemoteController ! (action, false)
          
        // Handle actions with an undo flag
        case (action : Action, undo : Boolean) => RemoteController ! (action, undo)

        // Handle events
        case event : Event => {
          // Concatenate the event (to a max of 10)
          events = (event :: events).take(10)

          // Send the events on to the modules!
          defaultModule(events)

          // Never allow it to step into the 'End state
          if (defaultModule.state == 'End) {
            defaultModule.state = 'Start
          }
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
  def isOnline = RemoteController.isOnline

}