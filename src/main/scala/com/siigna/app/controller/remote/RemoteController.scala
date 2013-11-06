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

import com.siigna.app.model.ActionModel
import com.siigna.app.model.Model
import com.siigna.app.model.action.{RemoteAction, LoadDrawing, Action}
import com.siigna.util.Log
import com.siigna.app.Siigna
import scala.concurrent.Lock

/**
 * A RemoteController which synchronises the drawing in the given [[com.siigna.app.model.ActionModel]] with the
 * given remote server.
 * <p>
 *   <b>Important: </b> To correctly initialise the connection with the server and activate the synchronisation
 *   the method <code>init()</code> should be called after instantiating the RemoteController.
 * </p>
 * If the client is not online or no connection could be made we simply wait until a connection can be
 * re-established before pushing all the received events/requests in the given order.
 * @param model  The model to execute the actions on synchronise with the server.
 * @param gateway  Gateway to the specific transport layer
 *
 */
class RemoteController(protected val model : ActionModel, protected val gateway : RESTGateway, sleepTime : Int = 2000) {

  // -- Initiate connection to server -- //
  // Run a remote thread
  val t = new Thread() {
    override def run() {

      try {
        // Loooopsin' for actions to send
        while(!shouldExit) {

          // Tell the world we are synchronising
          _sync = true

          // Synchronise actions
          syncActions()

          if (!mailbox.isEmpty) {
            var actionsToSend : Seq[(Action, Boolean)] = null

            // Store the actions from the mailbox - and lock it!
            mailboxLock.acquire()
            // Store the actions to send
            actionsToSend = mailbox

            // Empty mailbox
            mailbox = Nil
            mailboxLock.release()

            // Zænd eet!
            sendActions(actionsToSend)
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

  /**
   * Initialises the Remote Controller by asking for a drawing (or finding a new one if no ID have been defined in
   * the model) and starting a thread that asynchronously listens on incoming actions to synchronise.
   */
  def init() {
    // Tell the world that we are doing stuff
    _sync = true

    // Retrieve the drawing
    getDrawing(model.attributes.long("id")) match  {

      case Some(newModel) => {
        // Implement the model
        model.execute(LoadDrawing(newModel), remote = false)

        // Log the success
        Log.success("Remote: Successfully received and loaded drawing #" + session.drawing + " from server")

        // Query for new actions
        syncActions()

        // Set a priority and start
        t.setPriority(Thread.MIN_PRIORITY)
        t.start()
      }
      case _ => Log.error("Remote: Failed to fetch drawing from the server.")
    }
  }

  // A map of local shape ids mapped to their remote counterparts
  protected[remote] var localIdMap : Map[Int, Int] = Map()

  // A boolean flag to signal whether to close down
  private var shouldExit = false

  // The mailbox where pending actions are stored
  protected[remote] var mailbox : Seq[(Action, Boolean)] = Seq()

  // The mailbox lock
  protected val mailboxLock : Lock = new Lock

  /**
   * A boolean that indicates whether we are currently communicating with the server.
   */
  protected var _sync = false

  /**
   * A boolean to indicate if we have pending actions to synchronise with the server. If the flag resolves true the
   * connection should not be closed since any data not synchronised might disappear.
   * @return True if we are currently communicating and synchronising with the server, false if there are no
   *         pending communication.
   */
  def isSynchronising = !mailbox.isEmpty | _sync

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
   * Retrieves a new drawing from the server. If the id parameter is set we query the server for that specific drawing.
   * If not, then we ask for a unique global id for this drawing.
   * @param id  The id of the drawing to retrieve from the server, if any.
   * @return  Some[Model] with an id and lastAction attribute set, if the model or drawing-id was successfully
   *          retrieved. None if an error occurred.
   */
  protected def getDrawing(id : Option[Long]) : Option[Model] = {
    id match {
      case Some(i) => {
        gateway.getDrawing(i, session) match {
          case Left(newModel : Model) => Some(newModel) // Return the model
          case Right(message)=> Log.debug("Remote: ", message); None
        }
      }
      case None    => {
        // We need to ask for a new drawing
        gateway.getNewDrawingId(session) match {
          case Left(id : Long)  => {
            // Gotcha! Set the drawing id
            model.attributes += "id" -> id
            model.attributes.int("lastAction") match {
              case None => model.attributes += "lastAction" -> 0
              case _ =>
            }
            Some(model.model)
          }
          case Right(m) => {
            Log.debug("Remote: ", m)
            shouldExit = true
            None
          }
        }
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
        model.attributes += "lastAction" -> s.max
        Log.success("Remote: Received and updated action ids")
      }
      case Right(message) => {
        Log.error("Remote: Error when sending actions: " + message)
      }
    }
  }

  /**
   * Defines whether the client is connected to a remote server or not.
   * @return true if connected, false if not.
   */
  def isOnline = gateway.isConnected

  /*protected*/ def mapRemoteIDs(seq: Seq[Int],session: Session):Map[Int,Int] ={
    // .. Then we need to query the server for ids
    gateway.getShapeIds(seq.size,session) match {
      case Left(i : Range) => {
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

        // Update the map in the remote controller
        localIdMap ++= mapRemoteIDs(localIds,session)

        // Update the model
        model.execute(UpdateLocalActions(localIdMap), remote = false)

        // Return the updated action
        Some(action.update(localIdMap))
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
   * Send a number of actions to the server by first polling for a remote and unique ID for the actions, assigning
   * it to the actions and then dispatching them to the given gateway.
   * @param actionSeq  A sequence of actions to send
   */
  protected def sendActions(actionSeq :Seq[(Action, Boolean)]) {
    // Parse the local action to ensure all the ids are up to date
    val actions = actionSeq.map(t => parseLocalAction(t._1, t._2)).collect { case Some(a) => a }

    // Dispatch the data, if any
    if (!actions.isEmpty) {
      handleSetActions(gateway.setActions(actions, session))
    }
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
      mailboxLock.acquire()
      mailbox :+= action -> undo
      mailboxLock.release()
    }
  }

  /**
   * Attempts to fetch the session for the current client.
   * @return A session
   */
  def session : Session = {
    Session(model.attributes.long("id").getOrElse(-1), Siigna.user)
  }

  /**
   * Synchronises actions on the given drawing with the server by asking for the latest action-id, comparing that to
   * the latest action id currently in the drawing, and then fetching the actions that have not been executed locally.
   */
  def syncActions() {
    (model.attributes.long("id"), model.attributes.int("lastAction")) match {
      case (Some(drawingId), Some(currentActionId)) => {
        gateway.getActionId(session) match {
          case Left(id) => {
            Log.debug("Remote: Got latest action id " + id)

            // If the id is above the action indices then we have a gap to fill!
            if(id > currentActionId) {
              val range = (currentActionId + 1) to id

              gateway.getActions(range,session) match {
                case Left(actions : Seq[RemoteAction]) => {
                  // Execute the received actions
                  actions.foreach { action =>
                    action.undo match {
                      case true  => model.undo(action.action, remote = false)
                      case false => model.execute(action.action, remote = false)
                    }
                  }

                  // Add the id of the last action
                  model.attributes += "lastAction" -> id
                }
                case Right(message) => {
                  shouldExit = true
                  Log.error("Remote: Failed to retrieve actions from drawing. Aborting. ", message)
                }
              }
            }
          }
          case Right(m) => Log.warning("Remote: Failed to read action id from server: ", m)
        }
      }
      case (Some(x), None) => Log.error("Remote: Could not read last action from model.")
      case (_, Some(x)) => Log.error("Remote: Could not read drawing id from model.")
      case _ => Log.error("Remote: Could not read drawing id or last action from the model")
    }
  }
}
