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

package com.siigna.app.controller.remote

import com.siigna.app.Siigna
import com.siigna.app.model.{Drawing => SiignaDrawing, Model}
import RemoteConstants._
import com.siigna.app.model.action.{RemoteAction, LoadDrawing, Action}
import com.siigna.app.controller.remote.RemoteConstants.Action
import collection.mutable
import com.siigna.util.Log
import com.siigna.app.controller.remote.RemoteConstants.Drawing
import com.siigna.app.view.View
import com.siigna.util.geom.Vector2D

/**
 * Controls any remote connection(s).
 * If the client is not online or no connection could be made we simply wait until a connection can be
 * re-established before pushing all the received events/requests in the given order.
 */
object RemoteController {

  // All the ids of the actions that have been executed on the client
  protected val actionIndices = mutable.BitSet()

  // A map of local ids mapped to their remote counterparts
  protected var localIdMap : Map[Int, Int] = Map()

  // A boolean flag to signal whether to close down
  private var shouldExit = false

  // The remote server
  //private val remote = new Server("80.71.132.98", Mode.Production)
  private val remote = new Server("app.siigna.com", Mode.Http)
  //val remote = new Server("localhost", Mode.Production)
  //val remote = new Server("localhost", Mode.http)

  private var mailbox : Seq[(Action, Boolean)] = Seq()

  // -- Initiate connection to server -- //
  // Run a remote thread
  val t = new Thread() {
    override def run() {

      // Wait until we are live
      while (!isLive) Thread.sleep(500)

      Log.debug("Remote: Initiating connection.")

      try {
        def drawingId : Option[Long] = SiignaDrawing.attributes.long("id")

        // If we have a drawing we need to fetch it if we don't we need to reserve it
        drawingId match {
          case Some(i) => {
            remote(Get(Drawing, i, session), handleGetDrawing)
          }
          case None    => {
            // We need to ask for a new drawing
            remote(Get(DrawingId, null, session), _ match {
              case Set(DrawingId, id : Long, _) => {
                // Gotcha! Set the drawing id
                SiignaDrawing.attributes += "id" -> id
                Log.success("Remote: Successfully reserved a new drawing " + id)
              }
              case Set(DrawingId, id : Int, _) => {
                // Gotcha! Set the drawing id
                SiignaDrawing.attributes += "id" -> id.toLong
                Log.success("Remote: Successfully reserved a new drawing" + id)
              }
              case e => {
                Log.error("Remote: Could not reserve new drawing, shutting down.", e)
                shouldExit= true
              }
            })
          }
        }

        // Loooopsin' for actions to send
        while(!shouldExit) {
          // Query for new actions
          remote(Get(ActionId, null, session), handleGetActionId)

          if (!mailbox.isEmpty) {
            val (action, undo) = mailbox.head
            mailbox = mailbox.tail

            // Parse the local action to ensure all the ids are up to date
            val updatedAction = parseLocalAction(action, undo)

            // Dispatch the data
            remote(Set(Action, updatedAction, session), handleSetAction)
          }

          // Sleep for a bit to avoid pinging continuously
          Thread.`yield`()
          Thread.sleep(2000)
        }
      } catch {
        case e : Throwable => Log.error("Error when running remote controller: " + e)
      }
    }
  }

  // Set a priority and start
  t.setPriority(Thread.MIN_PRIORITY)
  t.start()

  // -- Common boring methods -- //

  /**
   * Stops the remote controller
   */
  def exit() { shouldExit = true }

  /**
   * Determines whether the controller should broadcast to the server.
   * @return  True is Siigna is 'live', false otherwise.
   */
  protected def isLive = Siigna.get("isLive") match {
    case Some(true) => true
    case _ => false
  }

  /**
   * Sends the given action to the server on a non-specified time in the future (async).
   * @param action
   * @param undo
   */
  def sendActionToServer(action : Action, undo : Boolean) {
    // Only react if we are live
    if (!isLive) {
      Log.debug("Remote: Server is offline, not sending action " + action)
    } else {
      mailbox :+= action -> undo
    }
  }

  /**
   * Handles requests for action ids. These requests are performed once in a while to make sure the client
   * has received the latest actions from the server.
   * @param any  The result of the request.
   */
  protected def handleGetActionId(any : Any) {
    any match {
      case Error(code, message, _) => Log.error("Remote: Error when retrieving action: " + code + ": " + message)
      case Set(ActionId, id : Int, _) => {
        Log.debug("Remote: Got latest action id " + id)

        // Store the id if it's the first we get
        if (actionIndices.isEmpty) {
          actionIndices += id
        // If the id is above the action indices then we have a gap to fill!
        } else if(id > actionIndices.last) {

          for (i <- actionIndices.last + 1 to id) { // Fetch actions one by one TODO: Implement Get(Actions, _, _)
            remote(Get(Action, i, session), _ match {
              case Set(Action, action : RemoteAction, _) => {
                try {

                  action.undo match {
                    case true  => SiignaDrawing.undo(action.action, remote = false)
                    case false => SiignaDrawing.execute(action.action, remote = false)
                  }

                  // Store the id in the action indices
                  // Note to self: "+=" and NOT "+"... Sigh...
                  actionIndices += i
                } catch {
                  case e: Throwable => Log.error("Remote: Error when reading data from server", e)
                }
              }
              case e : Error => Log.error("Remote: Unexpected format: " + e)
            })
          }
        }

        // After the check it should be fine to add the index to the set of action indices
        // Note to self: "+=" and NOT "+"... Sigh...
        actionIndices += id
      }
      case e => {
        Log.error("Remote: Error when updating ActionId: Expected Set(ActionId, Int, _), got: " + any)
      }
    }
  }

  /**
   * Handles requests to set an action, initiated by the client. These requests store the actions made by the
   * clients on the server.
   * @param any  The data received from the server
   */
  protected def handleSetAction(any : Any) {
    any match {
      case Error(code, message, _) => {
        Log.error("Remote: Error when sending action: " + message)
        // TODO: Correctly handle errors
      }
      case Set(ActionId, id : Int, _) => {
        actionIndices += id
        Log.success("Remote: Received and updated action id")
      }
      case Set(ActionId, id : Long, _) => {
        actionIndices += id.toInt
        Log.success("Remote: Received and updated action id")
      }
    }
  }

  /**
   * set the pan and zoom to enclose the entire drawing
   * @param drawing the drawing which is being loaded
   */
  def zoomExtends(implicit drawing : SiignaDrawing) {
    View.zoom = math.max(View.width, View.height) / math.max(drawing.boundary.width, drawing.boundary.height) * 0.5 // 20% margin
    View.pan = Vector2D(-drawing.boundary.center.x * View.zoom, drawing.boundary.center.y * View.zoom)

    // Notify the listeners
    View.listenersPan.foreach(_(View.pan))
    View.listenersZoom.foreach(_(View.zoom))
  }


  /**
   * Handles the request for a drawing whose id is specified in the <code>session</code> of this client.
   */
  protected def handleGetDrawing(any : Any) {
    any match {
      case Error(404, message, _) => {
        Log.error("Remote: Cannot find drawing with id " + session.drawing + ". Requesting empty drawing")
        remote(Get(Drawing, null, session), handleGetDrawing)
      }
      case Error(code, message, _) => Log.error("Remote: Unknown error when loading drawing: [" + code + "]" + message)
      case Set(Drawing, model : Model, _) => {

        // Read the bytes
        try {
          // Implement the model
          SiignaDrawing.execute(LoadDrawing(model), remote = false)

          // Search for the lastAction attribute, or retrieve it manually,
          // which sets the last executed action on the drawing
          SiignaDrawing.attributes.int("lastAction") match {
            case Some(i : Int) => actionIndices += i
            case _ => remote(Get(ActionId, null, session), handleGetActionId)
          }
          Log.success("Remote: Successfully received drawing #" + session.drawing + " from server")
          println("zoom extends in remote controller")
          zoomExtends
        } catch {
          case e : Throwable => Log.error("Remote: Error when reading data from server", e)
        }
      }
    }
  }

  /**
   * Defines whether the client is connected to a remote server or not.
   * @return true if connected, false if not.
   */
  def isOnline = remote.isConnected

  /**
   * Parses a given local action to a remote action by checking if there are any local ids that we need
   * to update. If so the necessary requests are made to the server and the local model is updated
   * with the new ids. In case of irreversible errors we throw an UnknownException. You are warned.
   *
   * @throws UnknownError  If the server returned something illegible
   * @return A RemoteAction with updated ids, if any.
   */
  protected def parseLocalAction(action : Action, undo : Boolean) : RemoteAction = {
    // Parse the action to an updated version
    val updated : Action = if (action.isLocal) {
      val localIds = action.ids.filter(_ < 0).toSeq

      // Map the ids with existing key-pairs
      val ids = localIds.map(i => localIdMap.getOrElse(i, i))

      // Do we still have local ids?
      if (ids.exists(_ < 0)) {
        // Find the local ids
        val localIds = ids.filter(_ < 0)

        var updatedAction : Option[Action] = None

        // .. Then we need to query the server for ids
        remote(Get(ShapeId, localIds.size, session), _ match {
          case Set(ShapeId, i : Range, _) => {

            // Find out how the ids map to the action
            val map = for (n <- 0 until localIds.size) yield localIds(n) -> i(n)

            // Update the map in the remote controller
            localIdMap ++= map

            // Update the model
            SiignaDrawing.execute(UpdateLocalActions(localIdMap), remote = false)

            // Return the updated action
            updatedAction = Some(action.update(localIdMap))
          }
          case e => {
            throw new UnknownError("Remote: Expected Set(ShapeId, Range, _), got: " + e)
          }
        })

        updatedAction.getOrElse(throw new IllegalArgumentException("Remote: Server did not return expected value."))
      } else { // Else give the action the new ids
        action.update(localIds.zip(ids).toMap)
      }
    } else { // No local ids
      action
    }

    // Return the updated action as a remote action
    RemoteAction(updated, undo)
  }

  /**
   * Attempts to fetch the session for the current client.
   * @return A session
   */
  def session : Session = {
    Session(SiignaDrawing.attributes.long("id").getOrElse(-1), Siigna.user)
  }
}
