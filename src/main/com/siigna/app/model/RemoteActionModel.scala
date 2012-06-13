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

package com.siigna.app.model

import action.{VolatileAction, Action, CreateShape, DeleteShape}
import com.siigna.app.controller.Controller
import com.siigna.app.controller.remote._
import com.siigna.app.Siigna
import shape.Shape
import com.siigna.util.logging.Log
import com.siigna.app.view.View

/**
 * <p>A RemoteActionModel with the responsibilities to keep track of actions (and information regarding
 * the actions) relevant in the communications between the client and the server.</p>
 * <p>An example is the unique shape id necessary for each shape, which can not be served locally before the
 * server has approved the id. To solve this the action is only applied locally, but not sent remotely.
 */
trait RemoteActionModel extends ActionModel {

  /**
   * An action that is only executed locally.
   * @param shapes  The shapes with local ids.
   * @param f  A function transforming the action to a
   */
  sealed private class LocalAction(shapes : Map[Int, Shape], f : Map[Int, Shape] => Action) extends Action {
    def execute(model : Model) = f(shapes).execute(model)
    def undo(model : Model) = f(shapes).undo(model)
  }

  /**
   * An integer to keep track of the local ids.
   */
  protected var localCounter = 0

  /**
   * A queue of unique ids received from the server.
   */
  protected var idBank : Seq[Int] = Seq()

  def execute(action : Action) { execute(action, true) }

  /**
   <p>Executes an action, lists it as executed and clears the undone stack to make way for new actions
   * (if they are not instances of [[com.siigna.app.model.action.VolatileAction]]) and sends it to .</p>
   * es
   * <p>If the local flag is set the action is not distributed to the server. If the flag is false the
   * action is not store in the model either.<
   * /p>
   * @param action  The action to execute.
   * @param remote  A flag indicating if the action executed should not be distributed to other clients.
   */
  def execute(action : Action, remote : Boolean) {
    Log.debug("Model: Executing action: " + action)

    // Execute in the model
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
      Log.debug("Forwarding action to server: " + action)
    }
  }

  /**
   * Assigns the given number of elements an id before giving them to the action and executing it.
   * If the assigned ids are from the local
   *
   * @param shapes  The shapes to be given ids.
   * @param f  A function transforming a map with the ids and shapes into the action to be executed.
   * @tparam T  The type of the elements.
   */
  def executeWithIds(shapes : Seq[Shape], f : Map[Int, Shape] => Action) {
    if (idBank.size >= id) {
      val id = idBank.head
      idBank = idBank.tail
      id
    } else {
      localCounter -= 1 // Decrement by one (avoid collision with server ids)
      localIds = localIds :+ localCounter

      // Send a request for more ID's
      if (Siigna.client.isDefined) {
        Get(ShapeIdentifier, Some(localIds.size + 5), Siigna.client.get)
      }

      localCounter
    }
  }
  
  def redo {
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
  
  def setIdBank(xs : Seq[Int]) {
    // If there are no local ids, then just save the ids
    if (localIds.isEmpty) {
      idBank ++= xs

    // Otherwise we can use them to replace local ids
    } else {
      xs.foreach(id => {
        // Store the local id if we can't use it
        if (localIds.isEmpty) idBank :+ id
        // Otherwise store it for the local id
        else {
          // Get the id and the shape
          val localId = localIds.head
          val shape = ActionModel(localId)

          // Remove the used id
          localIds = localIds.tail
          
          // Delete the shape (locally)
          Model execute(DeleteShape(localId, shape), false)
          
          // Add the shape with the new id (remotely)
          Model execute(CreateShape(id, shape), true)
        }
      })
    }
  }
  
  def undo { undo(None) }
  
  /**
   * <p>Undo an action and put it in the list of undone actions.</p>
   * 
   * <p>If the method receives an action it assumes the action is received from the server, 
   * and it will not be propagated back to the server.</p>
   */
  def undo(remote : Option[Action]) {
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
        Log.debug("Forwarding undoing action to server: " + action)
      }
      
      // Render the view
      View.render()
    } else {
      Log.warning("Model: No more actions to undo.")
    }
  }
  
}
