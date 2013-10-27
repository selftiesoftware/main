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
import com.siigna.app.model.{Model, ActionModel}
import RemoteConstants._
import com.siigna.app.model.action.{RemoteAction, LoadDrawing, Action}
import collection.mutable
import com.siigna.util.Log
import com.siigna.app.controller.remote.RemoteConstants.Drawing

/**
 * Establishes a connection to the given remote server to synchronise the given model.
 * If the client is not online or no connection could be made we simply wait until a connection can be
 * re-established before pushing all the received events/requests in the given order.
 * @param model  The model to execute the actions on synchronise with the server.
 * @param gateway  Gateway to the specific transport layer
 *
 */
class RemoteController(protected val model : ActionModel, protected val gateway : RESTGateway, sleepTime : Int = 2000) {

  // All the ids of the actions that have been executed on the client
  protected[remote] val actionIndices = mutable.BitSet()

  // A map of local ids mapped to their remote counterparts
  protected[remote] var localIdMap : Map[Int, Int] = Map()

  // A boolean flag to signal whether to close down
  private var shouldExit = false

  // The mailbox where pending actions are stored
  private var mailbox : Seq[(Action, Boolean)] = Seq()

  /**
   * A boolean that indicates whether we are currently communicating with the server.
   */
  protected var _sync = false

  /**
   * A boolean to indicate if we have pending actions to synchronise with the server. If the flag resolves true the
   * connection should not be closed since any data not synchronised might disappear.
   * @return True if we are currently communicating and synchronising with the server, false if there are no
   *         pending TCP communication.
   */
  def isSynchronising = {
    !mailbox.isEmpty | _sync
  }
  def sendActions(actionSeq :Seq[(Action, Boolean)])={

    // Parse the local action to ensure all the ids are up to date
    val actions = actionSeq.map(t => parseLocalAction(t._1, t._2)).collect { case Some(a) => a }

    // Dispatch the data, if any
    if (!actions.isEmpty) {
      handleSetActions(gateway.setActions(actions, session))
    }
  }
  // -- Initiate connection to server -- //
  // Run a remote thread
  val t = new Thread() {
    override def run() {

      // Wait until we are live
      while (!isLive) Thread.sleep(500)

      Log.debug("Remote: Initiating connection.")

      try {
        // Loooopsin' for actions to send
        while(!shouldExit) {

          // Query for new actions
          handleGetActionId(gateway.getActionId(session))

          // Tell the world that we are doing stuff
          _sync = true

          if (!mailbox.isEmpty) {
           sendActions(mailbox)
            // Empty mailbox
            mailbox = Nil
          }

          // Tell the world that we are done doing stuff
          _sync = false

          // Sleep for a bit to avoid pinging continuously
          Thread.`yield`()
          Thread.sleep(sleepTime)
        }

        Log.info("Remote controller exiting.")
      } catch {
        case e : Throwable => {
          Log.error("Error when running remote controller: " + e)
        }
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
   * Initialises the connection by querying for a drawing.
   */
  def init() {
    def drawingId : Option[Long] = model.attributes.long("id")

    // If we have a drawing we need to fetch it if we don't we need to reserve it
    drawingId match {
      case Some(i) => {
        handleGetDrawing(gateway.getDrawing(i, session))
      }
      case None    => {
        // We need to ask for a new drawing
        gateway.getNewDrawingId(session) match {
          case Left(id : Long)  => {
            // Gotcha! Set the drawing id
            model.attributes += "id" -> id
            Log.success("Remote: Successfully reserved a new drawing " + id)
          }
          case Right(m) => {
          Log.error("Remote: Could not reserve new drawing, shutting down.", m)
            shouldExit = true
          }
        }
      }
    }

    // Query for new actions
    handleGetActionId(gateway.getActionId(session))
  }

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
   * @param action  The action to execute
   * @param undo  Whether or not the action should be undone
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
   * @param response  The result of the request.
   */
  protected def handleGetActionId( response: Either [Int, String] ) {
    response match {
      case Left(id : Int ) => {
        Log.debug("Remote: Got latest action id " + id)

        // Store the id if it's the first we get
        if (actionIndices.isEmpty) {
          actionIndices += id
        // If the id is above the action indices then we have a gap to fill!
        } else if(id > actionIndices.last) {
          val range = actionIndices.last to id

          gateway.getActions(range,session) match {
            case Left(actions : Seq[RemoteAction]) => {
              actions.foreach { action =>
                try {
                  action.undo match {
                    case true  => model.undo(action.action, remote = false)
                    case false => model.execute(action.action, remote = false)
                  }
                } catch {
                  case e: Throwable => Log.error("Remote: Error when reading data from server", e)
                }
              }

              // Store the ids in the action indices
              // Note to self: "+=" and NOT "+"... Sigh...
              actionIndices ++= range
            }
            case Right(message) => Log.error("Remote: Unexpected format: " + message)
          }
        }

        // After the check it should be fine to add the index to the set of action indices
        // Note to self: "+=" and NOT "+"... Sigh...
        actionIndices += id
      }
      case Right(message) => {
        Log.error(s"Remote: Error when updating ActionId: Expected Set(ActionId, Int, _): $message")
      }
    }
  }

  /**
   * Handles requests to set one or more actions, initiated by the client. These requests store the actions
   * made by the clients on the server.
   * @param response  The data received from the server
   */
  protected def handleSetActions(response : Either[Seq[Int], String]) {
    response match {
      case Left(s : Seq[Int]) => {
        actionIndices ++= s
        Log.success("Remote: Received and updated action ids")
      }
      case Right(message) => {
        Log.error("Remote: Error when sending actions: " + message)
      }
    }
  }

  /**
   * Handles the request for a drawing whose id is specified in the <code>session</code> of this client.
   */
  protected def handleGetDrawing(result: Either[Model, String]) {
    result match {
      case Left(newModel : Model )=> {
        // Read the bytes
        try {
          // Implement the model
          model.execute(LoadDrawing(newModel), remote = false)

          // Search for the lastAction attribute, or retrieve it manually,
          // which sets the last executed action on the drawing
          model.attributes.int("lastAction") match {
            case Some(i : Int) => actionIndices += i
            case _ => handleGetActionId(gateway.getActionId(session))
          }
          Log.success("Remote: Successfully received drawing #" + session.drawing + " from server")
        } catch {
          case e : Throwable => Log.error("Remote: Error when reading data from server", e)
        }
      }
      case Right(message)=> Log.error("Remote: Unknown error when loading drawing: " + message)
    }
  }

  /**
   * Defines whether the client is connected to a remote server or not.
   * @return true if connected, false if not.
   */
  def isOnline = gateway.isConnected

  def mapRemoteIDs(seq: Seq[Int],session: Session):Map[Int,Int] ={
    // .. Then we need to query the server for ids
    gateway.getShapeIds(seq.size,session) match {
    case Left(i : Range )=> {
    // Find out how the ids map to the action
    val map = for (n <- 0 until seq.size) yield seq(n) -> i(n)
    map.toMap
  }
    case Right (message)=>{Log.error("Remote: Could not get new ShapeID's from server",message)
    Map()
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
  protected def parseLocalAction(action : Action, undo : Boolean): Option[RemoteAction] = {
    // Parse the action to an updated version
    val updated = if (action.isLocal) {
      val localIds = action.ids.filter(_ < 0).toSeq

      // Map the ids with existing key-pairs
      val ids = localIds.map(i => localIdMap.getOrElse(i, i))

      // Do we still have local ids?
      if (ids.exists(_ < 0)) {
        // Find the local ids
        val localIds = ids.filter(_ < 0)

        var updatedAction : Option[Action] = None

        // Update the map in the remote controller
        localIdMap ++= mapRemoteIDs(localIds,session)

        // Update the model
        model.execute(UpdateLocalActions(localIdMap), remote = false)

        // Return the updated action
        updatedAction = Some(action.update(localIdMap))
        updatedAction
      } else { // Else give the action the new ids
        Some(action.update(localIds.zip(ids).toMap))
      }
    } else { // No local ids
      Some(action)
    }

    // Return the updated action as a remote action
    updated.map(a => RemoteAction(a, undo))
  }

  /**
   * Attempts to fetch the session for the current client.
   * @return A session
   */
  def session : Session = {
    Session(model.attributes.long("id").getOrElse(-1), Siigna.user)
  }
}
