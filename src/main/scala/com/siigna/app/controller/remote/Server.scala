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

/**
 * A rest endpoint
 * @param client The endpoint to talk to.
 */
class RestEndpoint(client: Client) {

  def handle(r: RemoteCommand) : Option[Any] = {
    r match {

      case Get(DrawingId,v,s) => client.getDrawingId(s).map(Set(DrawingId,_,s))

      // Specific drawing is what we are after
      case Get(Drawing,v:Long,s) => client.getDrawing(v,s).map(Set(Drawing,_,s))

      // A new drawing is what we are after
      case Get(Drawing,v,s) => client.getDrawing(s).map(Set(Drawing,_,s))

      case Get(ShapeId,v:Int,s) => client.getShapeIds(v,s).map(Set(ShapeId,_,s))

      case Set(Action,v:RemoteAction,s) => client.setAction(v,s).map(Set(ActionId,_,s))

      case Get(Action,v:Int,s) => client.getAction(v,s).map(Set(Action,_,s))

      case Get(ActionId,v,s) => client.getActionId(s).map(Set(ActionId,_,s))

      case _ => None

    }

  }

}

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
 * @param mode  The mode of the connection, can be in production or testing mode
 */
class Server(host : String, mode : Mode.Mode) {

  // The remote server
  //private val client = new Client("http://62.243.118.234:20005")
  private val client = new Client("http://"+host+":"+mode.id)
  private val remote = new RestEndpoint(client) //select(Node(host, mode.id), 'siigna// )

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
   * @param f  The callback function to execute when data is successfully retrieved
   * @throws UnknownException  If the data returned did not match expected type(s)
   */
  def apply(message : RemoteCommand, f : Any => Unit) {
    try {
      if (shouldExit) {
        Log.info("Server: Connection closing.")
      } else {

        remote.handle(message) match {
          case Some(cmd : RemoteCommand) => { // Call the callback function
            f(cmd)

            // We're now connected for sure
            if (_retries > 0) Log.debug("Server: Connection (re)established after " + retries + " attempts.")
            _retries = 0 // Reset retries
          }
          case e => { // Timeout or false values
            // Increment retries
            _retries += 1

            if (_retries % 10 == 0) {
              Log.warning(s"Server: Connection to '$host:${mode.id}' failed after ${retries + 1} attempt(s), retrying: $message.")
            }

            // Retry
            apply(message, f)
          }
        }
      }
    } catch {
      case e : StackOverflowError => {
        shouldExit = true
        Log.error("Server: " + retries + " means stack overflow :-( Shutting down!")
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
   * An integer describing how many times we have attempted to send the latest message. This can be quite large
   * since the server never gives up.
   * @return An integer > 0 describing the number of times the latest message has been sent without a reply.
   */
  def retries = math.max(_retries, 0)

}

/**
 * The server mode in which the server operates. There are three modes: Production, Testing and Cleaning.
 * Production is used on the live, public Siigna application.
 * Testing is used for testing purposes.
 * And cleaning is used in the Siigna backend.
 */
object Mode extends Enumeration {
  type Mode = Value
  val Production = Value(20004)
  val Testing    = Value(20005)
  val Cleaning   = Value(20006)
  val http       = Value(7788)
}