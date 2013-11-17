/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */
package com.siigna.app.controller

import com.siigna.util.event.Event
import com.siigna.module.ModuleLoader
import com.siigna.app.controller.remote.{RESTGateway, RemoteController}
import com.siigna.app.model.Drawing
import com.siigna.util.Log

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
 *  The Controller is implemented as its own actor, to separate the view and control-threads. Since
 *  [[com.siigna.module.Module]]s are designed to be made by third-parties, we can not ensure their quality, so
 *  we might encounter some unexpected thread blocking. If that happens the modules are screwed but the rest of the
 *  application carries on. Hurray!
 * </p>
 *
 * @param drawing  The drawing to control.
 */
class Controller(drawing : Drawing) extends EventController {

  // Start the remote controller
  protected val remote = new RemoteController(drawing, new RESTGateway("http://app.siigna.com"))
  remote.init()

  // Listen to the drawing
  drawing.addRemoteListener((a, u) => remote.sendActionToServer(a, u))

  /**
   * <p>
   *   The running part of the controller handling [[com.siigna.app.model.action.Action]]s and
   *   [[com.siigna.util.event.Event]]s. If an action is sent to the controller it is forwarded to the
   *   RemoteController to be passed on to the remote system and other clients. Actions can be sent as a
   *   <code>(Action, Boolean)</code> [[scala.Tuple2]]. The second boolean parameter indicates whether the action
   *   should be undone (true) or simply executed (false).
   * </p>
   *
   * <p>
   *   The Controller does not quit until 'exit is sent to it, or the system is exiting (thread death).
   * </p>
   *
   * The actor also handles commands and the 'exit symbol.</p>
   */
  def act() {

    ModuleLoader.init()

    // Loop and react on incoming messages
    loop {
      react {

        // Handle events
        case event : Event => {
          // Send the event on to the modules!
          ModuleLoader.initModule.foreach(_ apply event)
        }

        // Exit
        case 'exit => {
          Log.info("Controller is shutting down")

          // Quit the RemoteController
          remote.exit()

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
  def isOnline = remote.isOnline

  /**
   * Examines if client and server sync is in progress.
   * @return True if sync is going on, false otherwise.
   */

  def isSyncronizing = remote.isSynchronising

}