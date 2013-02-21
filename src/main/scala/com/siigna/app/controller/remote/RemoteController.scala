/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.controller.remote

import com.siigna.app.Siigna
import actors.{Actor, TIMEOUT}
import RemoteConstants._
import com.siigna.app.model.action.{RemoteAction, LoadDrawing, Action}
import com.siigna.app.controller.remote.RemoteConstants.Action
import actors.remote.RemoteActor
import collection.mutable
import com.siigna.util.Log
import com.siigna.app.model.Model
import com.siigna.app.controller.remote.RemoteConstants.Drawing

/**
 * Controls any remote connection(s).
 * If the client is not online or no connection could be made we simply wait until a connection can be
 * re-established before pushing all the received events/requests in the given order.
 */
protected[controller] object RemoteController extends Actor {

  val SiignaDrawing = com.siigna.app.model.Drawing // Use the right namespace

  // Listen to the drawing
  SiignaDrawing.addRemoteListener((a, u) => this.!(a, u))

  // Set the class loader for the remote actor - important to avoid class not found exceptions on the receiving end.
  // See http://stackoverflow.com/questions/3542360/why-is-setting-the-classloader-necessary-with-scala-remoteactors
  RemoteActor.classLoader = getClass.getClassLoader

  // All the ids of the actions that have been executed on the client
  protected val actionIndices = mutable.BitSet()

  // A map of local ids mapped to their remote counterparts
  protected var localIdMap : Map[Int, Int] = Map()

  // Ping-time in ms
  var pingTime = 2000

  // Timeout to the server
  var timeout = 10000

  // The remote server
  val remote = new Server("62.243.118.234", Mode.Production)
  //val remote = new Server("localhost", Mode.Testing)
  //val remote = new Server("localhost", Mode.Production)

  /**
   * Determines whether the controller should broadcast to the server.
   * @return  True is Siigna is 'live', false otherwise.
   */
  protected def isLive = Siigna.get("isLive") match {
    case Some(true) => true
    case _ => false
  }

  /**
   * The acting part of the RemoteController.
   */
  def act() {
    // Log if we are not live
    if (!isLive) Log.info("Remote: Siigna is set to work offline. Waiting for the flag to turn green.")

    // Wait until we are live
    while (!isLive) Thread.sleep(timeout)

    // Log if we are live
    Log.info("Remote: Siigna is online: Initializing remote connection.")

    try {
      def drawingId : Option[Long] = SiignaDrawing.attributes.long("id")

      // If we have a drawing we need to fetch it if we don't we need to reserve it
      drawingId match {
        case Some(i) => remote(Get(Drawing, i, session), handleGetDrawing)
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
              exit()
            }
          })
        }
      }

      loop {
        // Only react if we are live
        while (!isLive) Thread.sleep(timeout)

        // Query for new actions
        remote(Get(ActionId, null, session), handleGetActionId)

        reactWithin(pingTime) {
          // Set an action to the server
          case (action : Action, undo : Boolean) => {
            // Parse the local action to ensure all the ids are up to date
            val updatedAction = parseLocalAction(action, undo)

            // Dispatch the data
            remote(Set(Action, updatedAction, session), handleSetAction)
          }

          // Timeout
          case TIMEOUT => // Do nothing

          // Exit
          case 'exit => {
            remote.disconnect()
            exit()
          }

          // We can't handle any other commands actively...
          case message => {
            Log.warning("Remote: Unknown input '" + message + "', expected a remote action.")
          }
        }
      }
    } catch {
      case e : Exception => Log.error("Remote: Unknown error, shutting down.", e)
    }
  }

  /**
   * Defines whether the client is connected to a remote server or not.
   * @return true if connected, false if not.
   */
  def isOnline = remote.isConnected

  /**
   * Handles requests for action ids. These requests are performed once in a while to make sure the client
   * has received the latest actions from the server.
   * @param any  The result of the request.
   */
  def handleGetActionId(any : Any) {
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
  def handleSetAction(any : Any) {
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
        } catch {
          case e : Throwable => Log.error("Remote: Error when reading data from server", e)
        }
      }
    }
  }

  /**
   * Parses a given local action to a remote action by checking if there are any local ids that we need
   * to update. If so the necessary requests are made to the server and the local model is updated
   * with the new ids. In case of irreversible errors we throw an UnknownException. You are warned.
   *
   * @throws UnknownError  If the server returned something illegible
   * @return A RemoteAction with updated ids, if any.
   */
  def parseLocalAction(action : Action, undo : Boolean) : RemoteAction = {
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
   * @return A session if possible.
   */
  def session : Session =
    Session(SiignaDrawing.attributes.long("id").getOrElse(-1), Siigna.user)

}