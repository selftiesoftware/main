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

import com.siigna.util.Log
import com.siigna.app.controller.remote.RemoteConstants._
import com.siigna.app.model.action.RemoteAction
import scala.annotation.tailrec

/**
 * <p>
 *   An instance of a specific server located at the given host and the given port that can
 *   send messages synchronously.
 * </p>
 *
 * <p>
 *   We use the [[http://ubjson.org UBJSON]] (Universal Binary JSON) format to (un-)marshal data.
 * </p>
 *
 * @param host  The URL of the host.
 * @param port The port of the connection
 */
class RESTEndpoint(host : String, port : Int) {

  // The remote server
  private val client = new Client("http://"+host+":"+port)

  /**
   * An int that shows how many retries have been made AND is used to signal connectivity.
   * -1 means that we have not connected yet
   * 0  means that we are online
   * 1  and above means that we have tried <code>_retries</code> number of times.
   */
  protected var _retries = -1

  // A boolean flag to indicate that the we should cut the connection
  protected var shouldExit = false

  /**
   * A boolean value to indicate whether a connection has been successfully made to this server.
   * @return  True if a connection is available, false otherwise
   */
  def isConnected = _retries == 0

  /**
   * A method that sends a remote command, represented as a byte array, synchronously with an associated callback
   * function with side effects. The method repeats the procedure until something is received.
   * @param message  The message to send as a remote command
   */
  def apply(message : RemoteCommand) : Any = {
    try {
      dispatch(message).merge
    } catch {
      case e : StackOverflowError => {
        shouldExit = true
        Log.error(s"Remote: Too many retries $retries means stack overflow :-( Shutting down!")
      }
    }
  }

  /**
   * Disconnects from the server and stops any future attempts to send messages so the thread can quit.
   */
  def disconnect() {
    shouldExit = true
  }

  /**
   * Dispatches the given message and returns either the reply or an error string.
   * @param message  The message to dispatch
   * @return  The obj if it was successfully returned, otherwise an error message.
   */
  @tailrec protected final def dispatch(message : RemoteCommand) : Either[Any, String] = {
    if (shouldExit) {
      Right("Server: Cannot dispatch message; closing.")
    } else {

      handle(message) match {
        case Some(obj) => { // Call the callback function
          Log.debug(s"Received: $obj")

          // We're now connected for sure
          if (_retries > 0) Log.debug("Server: Connection (re)established after " + retries + " attempts.")

          Log.debug(s"SESSION: ${message.session}")
          _retries = 0 // Reset retries

          Left(obj)
        }
        case e => { // Connection issue or unexpected reply
          // Increment retries
          _retries += 1

          if (_retries % 10 == 0) {
            Log.warning(s"Server: Connection to '$host:$port' failed after ${retries + 1} attempt(s), retrying: $message.")
          }

          // Retry
          dispatch(message)
        }
      }
    }
  }

  /**
   * Handle a server call to the remote endpoint by dispatching the command and retrieving the reply as 
   * the expected type.
   * @param r  The remote command to send.
   * @return Option[Any] if the connection was successful and the reply was the expected type, None otherwise.
   */
  protected def handle(r: RemoteCommand) : Option[Any] = {
    r match {
      case Get(DrawingId,v,s) => client.getDrawingId(s)

      // Specific drawing is what we are after
      case Get(Drawing,v:Long,s) => client.getDrawing(v,s)

      // A new drawing is what we are after
      case Get(Drawing,v,s) => client.getDrawing(s)

      case Get(ShapeId,v:Int,s) => client.getShapeIds(v,s)

      case Set(Action,v:RemoteAction,s) => client.setAction(v,s)

      case Set(Actions, v : Seq[RemoteAction], s) => client.setActions(v,s)

      case Get(Action,v:Int,s) => client.getAction(v,s)

      case Get(Actions,v:Seq[Int],s) => client.getActions(v,s)

      case Get(ActionId,v,s) => client.getActionId(s)

      case x => Log.warning("Remote: Error when communicating with server: " + x); None

    }

  }

  /**
   * An integer describing how many times we have attempted to send the latest message. This can be quite large
   * since the server never gives up.
   * @return An integer > 0 describing the number of times the latest message has been sent without a reply.
   */
  def retries = math.max(_retries, 0)

}