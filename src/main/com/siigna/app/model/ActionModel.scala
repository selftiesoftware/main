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

import action.{VolatileAction, Action}
import shape.Shape
import com.siigna.util.logging.Log
import com.siigna.util.collection.Attributes
import com.siigna.app.Siigna
import com.siigna.app.controller.remote._
import com.siigna.app.view.View
import com.siigna.app.controller.remote.{Get, RemoteAction}

/**
 * A Model capable of executing, undoing and redoing [[com.siigna.app.model.action.Action]]s.
 */
trait ActionModel extends SelectableModel with HasAttributes {

  type T = ActionModel

  /**
   * An action that is only executed locally.
   * @param shapes  The shapes mapped to local ids.
   * @param f  A function that executes the action with the given shapes and ids.
   */
  sealed private case class LocalAction(shapes : Map[Int, Shape], f : Map[Int, Shape] => Action) extends Action {
    def execute(model : Model) = f(shapes).execute(model)
    def undo(model : Model) = f(shapes).undo(model)
  }

  /**
   * The attributes of the model containing name, title, owner and other attributes
   * fetched from the server, necessary for Siigna.
   */
  var attributes = Attributes()

  /**
   * The underlying immutable model of Siigna.
   */
  @volatile protected var model = new Model(Map[Int, Shape]())

  /**
   * The [[com.siigna.app.model.action.Action]]s that have been executed on this model.
   */
  protected var executed = Seq[Action]()

  /**
   * The [[com.siigna.app.model.action.Action]]s that have been undone on this model.
   */
  protected var undone = Seq[Action]()

  /**
   * A stream of a negative integers used for local ids.
   */
  protected var localIdStream = Stream.iterate(-1)(i => if (i - 1 > 0) -1 else i - 1).iterator

  /**
   * A queue of unique ids received from the server.
   */
  protected var remoteIds : Seq[Int] = Seq()

  /**
   * Adds a number of remote ids to the id-pool.
   * @param xs  The remote ids to add.
   */
  def addRemoteIds(xs : Seq[Int]) {
    // Store the ids
    var ids = remoteIds ++ xs

    // A method that updates the local action in a given collection
    def updateLocalActions(cs : Seq[Action], undo : Boolean) = {
      cs.map(action => action match {
        // Update a local action only if there's ids enough
        case LocalAction(shapes, f) if (shapes.size <= ids.size) => {
          // Retrieve the ids
          val (remote, remainder) = ids.splitAt(shapes.size)
          ids = remainder

          // Replace the ids og the shapes with the remote ids
          val remoteAction = f(remote.zip(shapes).map(t => {
            val id = t._1
            val (local, shape) = t._2

            // Replace the id in the model (if it exists)
            if (model.shapes.contains(local))
              model = model.remove(local).add(id, shape)

            // Return the new remote id mapped to the same old shape
            id -> shape
          }).toMap)

          // Send to server
          if (Siigna.client.isDefined) {
            RemoteAction(Siigna.client.get, remoteAction, undo)
            Log.debug("Model: Sending action to server.")
          }

          // Return
          remoteAction
        }
        case LocalAction(shapes, _) => {
          // Request more ids!
          if (Siigna.client.isDefined) {
            Get(ShapeIdentifier, Some(math.max(shapes.size, 5)), Siigna.client.get)
          }
          action
        }
        case a => a
      })
    }

    // Check the undone actions for instances of LocalAction
    undone = updateLocalActions(undone, true)

    // Check the executed actions for instances of LocalAction
    if (ids.size > 0) executed = updateLocalActions(executed, false)

    // Update the remote ids
    remoteIds = ids
  }

  def execute(action : Action) { execute(action, true) }

  /**
   <p>Executes an action, lists it as executed and clears the undone stack to make way for new actions
   * (if they are not instances of [[com.siigna.app.model.action.VolatileAction]]) and sends it to .</p>
   * es
   * <p>If the local flag is set the action is not distributed to the server. If the flag is false the
   * action is not store in the model either.</p>
   * @param action  The action to execute.
   * @param remote  A flag indicating if the action executed should not be distributed to other clients.
   */
  def execute(action : Action, remote : Boolean) {
    Log.debug("Model: Executing action: " + action)

    // Deselect any selections made
    deselect()

    // Execute in the model
    model = action.execute(model)

    // Store the action if it is a local action,
    // but not if it is volatile or remote (no need to store non-remote)
    if (action.isInstanceOf[LocalAction] || (!action.isInstanceOf[VolatileAction] && remote)) {
      executed +:= action
      undone = Vector()
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
   * If the assigned ids are from local ids, then the action is stored as a local action until further
   * ids have been received from the pool.
   *
   * @param shapes  The shapes to be given ids.
   * @param f  A function transforming a map with the ids and shapes into the action to be executed.
   *
   * TODO: Create an executeWithId
   */
  def executeWithIds(shapes : Iterable[Shape], f : Map[Int, Shape] => Action) {
    // Do we have enough ids?
    if (remoteIds.size >= shapes.size) { // Yes!
      // Retrieve the ids
      val (ids, bank) = remoteIds.splitAt(shapes.size)
      remoteIds = bank

      // Execute the action with the remote ids
      execute(f(ids.zip(shapes).toMap))
    } else { // ... No ...
      // Decrement the counter by the number of shapes (avoid collision with server ids)
      val ids = localIdStream.take(shapes.size).toSeq

      // Execute the action wrapped in a local action
      execute(new LocalAction(ids.zip(shapes).toMap, f), false)

      // Send a request for more ID's
      if (Siigna.client.isDefined) {
        Get(ShapeIdentifier, Some(math.max(shapes.size, 5)), Siigna.client.get)
      }
    }
  }

  def redo {
    if (undone.size > 0) {
      // Retrieve the event
      val action = undone.head
      undone = undone.tail

      Log.debug("Model: Redoing action: " + action)

      // Deselect any selections made
      deselect()

      // Execute the event and add it to the executed list
      model = action.execute(model)
      executed +:= action

      // Send to server
      if (Siigna.client.isDefined && !action.isInstanceOf[LocalAction]) {
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
   * The shapes currently in the model.
   * @return A Map containing shapes.
   */
  def shapes = model.shapes

  def setAttributes(attributes : Attributes) = {
    this.attributes = attributes
    this
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

      // Deselect any selections made
      deselect()

      // Undo it
      model = action.undo(model)

      // ... and add it to the undone list, but only if it is local
      if (remote.isEmpty) undone +:= action

      // Send to server if the client is defined and the action isn't set
      if (Siigna.client.isDefined && remote.isEmpty && !action.isInstanceOf[LocalAction]) {
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