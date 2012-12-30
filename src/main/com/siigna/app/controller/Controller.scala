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

import com.siigna.util.event.Event
import com.siigna.module.{Module, ModuleLoader}
import com.siigna.util.logging.Log
import com.siigna.app.model.action.Action
import remote.RemoteController
import actors.Actor
import com.siigna.app.Siigna

/**
 * <p>
 *   This is the controller part of the
 *   <a href="http://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller">Model-View-Controller</a> pattern.
 *   The Controller contains the core application logic in Siigna. Fortunately the only logic in Siigna is to forward
 *   incoming events on to the [[com.siigna.module.Module]]s and forward [[com.siigna.app.model.action.Action]]s from
 *   the [[com.siigna.app.model.Drawing]] onto the main server and from there to other clients. Simple, right?
 * </p>
 *
 * <p>
 *  The Controller is implemented as its own actor, since to separate the view and control-threads. Since
 *  [[com.siigna.module.Module]]s are designed to be made by third-parties, we can not ensure their quality, so
 *  we might encounter some unexpected thread blocking. If that happens the modules are screwed but the rest of the
 *  application carries on. Hurray!
 * </p>
 */
object Controller extends Actor with EventController {

  // The private init module.. ssshhh
  private var _initModule : Option[Module] = None

  /**
   * The init [[com.siigna.module.Module]] that we're sending events to.
   * Call <code>initModule_=()</code> if you want to change the behavior.
   * @see [[com.siigna.module.Module]]
   */
  def initModule : Option[Module] = None

  /**
   * Sets the init module so every events from the Controller will be forwarded to the init module instead.
   * This also means the the previous init module does not get any events.
   * @param instance  The [[com.siigna.module.Module]] to use as the default module.
   * @see [[com.siigna.module.Module]]
   */
  def initModule_=(instance : Module) {
    Siigna.setInterface(instance.interface)
    _initModule = Some(instance)
  }

  /**
   * <p>
   *   The running part of the controller handling [[com.siigna.app.model.action.Action]]s and
   *   [[com.siigna.util.event.Event]]s. If an action is sent to the controller it is forwarded to the
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

    // Init ModuleLoader
    ModuleLoader

    // Loop and react on incoming messages
    loop {
      react {

        // Handle actions (execute, not undo)
        case action : Action                   => RemoteController ! (action, false)
          
        // Handle actions with an undo flag
        case (action : Action, undo : Boolean) => RemoteController ! (action, undo)

        // Handle events
        case event : Event => {
          // Send the event on to the modules!
          initModule.foreach(_ apply event)
        }

        // Exit
        case 'exit => {
          Log.info("Controller is shutting down")

          // Quit the RemoteController
          RemoteController ! 'exit

          // Quit the thread
          exit()
        }

        // Unknown
        case e => Log.warning("Controller: Received unknown input: " + e)
      }
    }
  }

  /**
   * Examines whether this client is connected with the server.
   * @return True if the connection has been established correctly, false otherwise.
   */
  def isOnline = RemoteController.isOnline

}