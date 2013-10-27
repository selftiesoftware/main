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

import action.{LoadDrawing, VolatileAction, Action}
import shape.Shape
import com.siigna.util.collection.{HasAttributes, Attributes}
import com.siigna.util.Log
import com.siigna.app.view.View
//import com.sun.javaws.WinExtensionInstallHandler
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * A Model capable of executing, undoing and redoing [[com.siigna.app.model.action.Action]]s.
 */
trait ActionModel extends SpatialModel with HasAttributes {

  type T = ActionModel

  /**
   * The attributes of the model containing name, title, owner and other attributes
   * fetched from the server, necessary for Siigna.
   */
  def attributes = model.attributes

  /**
   * Sets the attributes of the model to the given attributes by completely replacing all current attributes.
   * @param attr  The attributes to set.
   */
  def attributes_=(attr : Attributes) { setAttributes(attr) }

  /**
   * A stream of a negative integers used for local ids.
   */
  protected var localIdStream = Stream.iterate(-1)(i => if (i - 1 > 0) -1 else i - 1).iterator

  /**
   * Listeners that receives events whenever an action is executed, undone or redone.
   */
  protected var listeners : Seq[(Action, Boolean) => Unit] = Nil

  /**
   * The underlying immutable model of Siigna.
   */
  protected var _model = new Model()

  def model = _model

  def model_=(m : Model) = _model = m

  /**
   * A listener for remote actions.
   */
  protected var remoteListener : (Action, Boolean) => Unit = (_, _) => Unit

  /**
   * Adds listeners that will be executed whenever an action is executed, undone or redone on the model.
   * @param listener  The function to be executed receiving an action that has been executed and a boolean
   *                  value indicating whether the action was undone (true) or executed (false; includes redo).
   */
  def addActionListener(listener : (Action, Boolean) => Unit) {
    listeners :+= listener
  }

  /**
   * Adds a remote listener that will send actions to a remote sink whenever they are executed, undone or redone
   * on the model.
   * @param listener  The function to be executed receiving an action that has been executed and a boolean
   *                  value indicating whether the action was undone (true) or executed (false; includes redo).
   */
  def addRemoteListener(listener : (Action, Boolean) => Unit) {
    remoteListener = listener
  }

  /**
   * <p>Executes an action, lists it as executed and clears the undone stack to make way for new actions
   * and sends it to the controller for remote handling.</p>
   *
   * @param action  The action to execute.
   * @param remote  Whether or not to send the action to the server.
   */
  def execute(action : Action, remote : Boolean = true) {
    try {
      // Execute in the model
      model = action.execute(model)

      // Store the action if it is not a VolatileAction
      action match {
        case v : VolatileAction => // Do nothing here!
        case _ => { // Store the action
          model = new Model(model.shapes, model.executed.+:(action), Seq(), model.attributes)
        }
      }

      // Notify listeners
      notifyListeners(action, undo = false, remote)

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
  def getIds(t : Traversable[Shape]) : Map[Int, Shape] = t.map(i => getId -> i).toMap

  /**
   * Notifies the listeners that an action has been executed, undone or redone.
   * @param action  The action that has been executed or undone
   * @param undo  Whether the action has been undone (true) or executed (false).
   * @param remote  Whether the action should go to the remote source as well.
   */
  protected def notifyListeners(action : Action, undo : Boolean, remote : Boolean) {

    model.tree.onSuccess {
      case x => {
        PRT = Some(x)
        listeners.foreach(_(action, undo))
        if (remote) remoteListener(action, undo)
        //If a drawing has been loaded, zoom to the extends of the drawing
        action match {
          case a : LoadDrawing => {
            com.siigna.app.controller.remote.RemoteController.zoomExtends
          }
        }
      }
    }(concurrent.ExecutionContext.Implicits.global)

  }

  /**
   * Redo the latest undone action by re-executing it on the model.
   */
  def redo() {
    if (undone.size > 0) {
      // Retrieve the event
      val action = model.undone.head
      val undone = model.undone.tail

      try {
        // Execute the event and add it to the executed list
        model = action.execute(model)
        model = new Model(model.shapes, model.executed.+:(action), undone, model.attributes)

        // Notify listeners
        notifyListeners(action, undo = false, remote = true)

        Log.success("ActionModel: Action successfully redone: " + action)
      } catch {
        case e : Exception => Log.error("ActionModel: Error when redoing action " + action, e)
      }
    } else {
      Log.debug("ActionModel: No more actions to redo.")
    }
  }

  def setAttributes(newAttributes : Attributes) = {
    model = model.copy(attributes = newAttributes)
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
      case Some(action) => undo(action, remote = true)
      case None => Log.debug("ActionModel: No more actions to undo.")
    }
  }

  /**
   * Undo the given action and put it in the list of undone actions.
   */
  def undo(action : Action, remote : Boolean = false) {
    try {
      // Undo it
      model = action.undo(model)

      // Move the executed action from the executed list to the undone list
      model = model.copy(executed = model.executed.tail, undone = model.undone.+:(model.executed.head))

      // Send the action to server with the undone flag set to true!
      notifyListeners(action, undo = true, remote)

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