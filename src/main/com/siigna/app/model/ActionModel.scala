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
import com.siigna.app.view.View
import com.siigna.app.controller.Controller

/**
 * A Model capable of executing, undoing and redoing [[com.siigna.app.model.action.Action]]s.
 */
trait ActionModel extends SelectableModel with HasAttributes {

  type T = ActionModel
  
  /**
   * The attributes of the model containing name, title, owner and other attributes
   * fetched from the server, necessary for Siigna.
   */
  var attributes = Attributes() 
  
  /**
   * A stream of a negative integers used for local ids.
   */
  protected var localIdStream = Stream.iterate(-1)(i => if (i - 1 > 0) -1 else i - 1).iterator

  /**
   * The underlying immutable model of Siigna.
   */
  protected var model = new Model(Map[Int, Shape](), Seq(), Seq())
  
  /**
   * <p>Executes an action, lists it as executed and clears the undone stack to make way for new actions
   * and sends it to the controller for remote handling.</p>
   * 
   * @param action  The action to execute.
   * @param remote  Whether or not to send the action to the server.
   */
  def execute(action : Action, remote : Boolean = true) {
    try {
      // Deselect any selections made
      deselect()

      // Execute in the model
      model = action.execute(model)

      // Store the action if it is not a VolatileAction
      action match {
        case v : VolatileAction => // Do nothing here!
        case _ => { // Store the action
          model = new Model(model.shapes, model.executed.+:(action), Seq())
        }
      }

      // Send it to the server
      if (remote) Controller ! action

      Log.success("ActionModel: Successfully executed action: " + action)
    } catch {
      case e : Exception => Log.error("ActionModel: Error when executing action " + action, e)
    }
  }

  /**
   * Retrieves a unique id for a shape.
   * @return A number unique for the given shape.
   */
  def getId : Int = localIdStream.next()

  /**
   * Retrieves a number of unique ids for a number of shapes.
   * @param i  The number of ids to retrieve.
   * @return  An iterator with length <i>i</i>.
   */
  def getIds(i : Int) = localIdStream.take(i)

  /**
   * Retrieves a number of unique ids for the shapes given in the collection.
   * @param t  A number of shapes in dire need of an id.
   * @return  A map with ids for the now not-so-needy shapes.
   */
  def getIds(t : Traversable[Shape]) : Map[Int, Shape] = t.map(getId -> _).toMap

  /**
   * Redo the latest undone action by re-executing it on the model.
   */
  def redo() {
    if (undone.size > 0) {
      // Retrieve the event
      val action = model.undone.head
      val undone = model.undone.tail

      try {
        // Deselect any selections made
        deselect()

        // Execute the event and add it to the executed list
        model = action.execute(model)
        model = new Model(model.shapes, model.executed.+:(action), undone)

        // Send to server
        Controller ! action

        Log.success("ActionModel: Action successfully redone: " + action)
      } catch {
        case e : Exception => Log.error("ActionModel: Error when redoing action " + action, e)
      }
    } else {
      Log.debug("ActionModel: No more actions to redo.")
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

  /**
   * Undo the lasted executed action in the model by executing
   * its [[com.siigna.app.model.action.Action.undo]]-method on the current model.
   * If no action have been executed, nothing happens.
   *
   */
  def undo() { 
    model.executed.headOption match {
      case Some(action) => undo(action, true)
      case None => Log.debug("ActionModel: No more actions to undo.")
    } 
  }

  /**
   * Undo the given action and put it in the list of undone actions.
   */
  def undo(action : Action, remote : Boolean = false) {
    try {
      // Deselect any selections made
      deselect()

      // Undo it
      model = action.undo(model)

      // Send the action to server with the undone flag set to true!
      if (remote) Controller ! (action, true)

      Log.success("ActionModel: Action successfully undone: " + action)
    } catch {
      case e : Exception => Log.error("ActionModel: Unable to undo the given action " + action, e)
    }
  }

  /**
   * The actions that have been undone on the model.
   * @return  A sequence of undone actions.
   */
  def undone = model.undone

}