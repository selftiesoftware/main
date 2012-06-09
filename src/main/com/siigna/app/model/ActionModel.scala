package com.siigna.app.model

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

import com.siigna.app.model.action.{VolatileAction, Action}
import com.siigna.util.logging.Log
import shape.Shape
import com.siigna.app.view.View
import com.siigna.app.controller.{Controller}
import com.siigna.app.controller.remote.RemoteAction
import com.siigna.app.Siigna

/**
 * A Model capable of executing, undoing and redoing [[com.siigna.app.model.action.Action]]s.
 */
trait ActionModel {

  /**
   * The underlying immutable model of Siigna.
   */
  @volatile protected var model = new Model(Map[Int, Shape]())

  /**
   * The [[com.siigna.app.model.action.Action]]s that have been executed on this model.
   */
  private var executed = Seq[Action]()

  /**
   * The [[com.siigna.app.model.action.Action]]s that have been undone on this model.
   */
  private var undone = Seq[Action]()

  /**
   * <p>Execute an action, list it as executed and clear the undone stack to make way for a new actions
   * (if it is not a [[com.siigna.app.model.action.VolatileAction]]).</p>
   *
   * <p>If the local flag is set the action is not distributed to the server. If the flag is false the
   * action is not store in the model either.</p>
   *
   * @param action  The action to execute.
   * @param remote  A flag indicating if the action executed should not be distributed to other clients.
   */
  def execute(action: Action, remote : Boolean = true) {
    Log.debug("Model: Executing " + (if (remote) "remote" else "local") + " action: " + action)
    
    // Execute in model 
    model = action.execute(model)

    // Only store the action if it is not volatile and remote (no need to store non-remote)
    if (!action.isInstanceOf[VolatileAction] && remote) {
      executed +:= action
      undone = Seq()
    }
    
    // Render the view
    View.render()

    // Create the remote command and dispatch it
    if (remote && Siigna.isOnline) {
      RemoteAction(Siigna.client.get, action)
    }
  }

  /**
   * Redo an action, by executing the last function that's been undone.
   */
  def redo() {
    if (undone.size > 0) {
      // Retrieve the event
      val action = undone.head
      undone = undone.tail
      
      Log.debug("Model: Redoing action: " + action)

      // Execute the event and add it to the executed list
      model = action.execute(model)
      executed +:= action
      
      // Send to server
      if (Siigna.client.isDefined) {
        RemoteAction(Siigna.client.get, action)
        Log.debug("Model: Sending action to server.")
      }
      
      // Render the view
      View.render()
    } else {
      Log.warning("Model: No more actions to redo.")
    }
  }

  /**
   * <p>Undo an action and put it in the list of undone actions.</p>
   * 
   * <p>If the method receives an action it assumes it is received from the server, 
   * since local actions can be undone by fetching them from the executed actions.</p>
   */
  def undo(remote : Option[Action] = None) {
    if (executed.size > 0 || remote.isDefined) {
      // Retrieve the action
      val action = if (remote.isDefined) remote.get else {
        val a = executed.head
        executed = executed.tail
        a
      }
      
      Log.debug("Model: Undoing " + (if (remote.isDefined) "remote" else "local") + " action " + action)

      // Undo it and add it to the undone list
      model = action.undo(model)
      undone +:= action
      
      // Send to server if the client is defined and the action isn't set
      if (Siigna.client.isDefined && remote.isEmpty) {
        RemoteAction(Siigna.client.get, action, true)
      }
      
      // Render the view
      View.render()
    } else {
      Log.warning("Model: No more actions to undo.")
    }
  }

}