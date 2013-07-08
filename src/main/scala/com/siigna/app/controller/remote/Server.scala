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
import com.siigna.util.io.{Unmarshal, Marshal}
import actors.Actor
import com.siigna.app.controller.remote.RemoteConstants._
import com.siigna.communication.http.Client
import com.siigna.app.model.action.RemoteAction
import com.siigna.app.model.Model


class RestEndpoint(client:Client) extends Actor{

  def respond(r: RemoteCommand) {
    sender ! com.siigna.util.io.Marshal(r)
  }

  def handle(r: RemoteCommand) {
    r match {


      case Get(DrawingId,v,s) => {

        client.getDrawingId(s) match {
          case id: Long => respond(Set(DrawingId,id,s))
          case e => {
            println("Fail")
            println(e)
          }
        }

      }

      case Get(Drawing,v:Long,s) => {
        // Specific drawing is what we are after
        val res = client.getDrawing(v,s)
        res match {
          case Some(m: Model) => respond(Set(Drawing,m,s))
          case e => {
            println("Fail")
            println(e)
          }
          case _ => //Fail
        }
      }

      case Get(Drawing,v,s) => {
        // A new drawing is what we are after
        client.getDrawing(s) match {
          case Some(m: Model) => respond(Set(Drawing,m,s))
          case e => {
            println("Fail")
            println(e)
          }
          case _ => //Fail
        }
      }

      case Get(ShapeId,v:Int,s) => {

        client.getShapeIds(v,s) match {
          case Some(range: Range) => respond(Set(ShapeId,range,s))
          case e => {
            println("Fail")
            println(e)
          }
          case _ => // Fail
        }
      }

      case Set(Action,v:RemoteAction,s) => {

        client.setAction(v,s) match {
          case aid: Int => respond(Set(ActionId,aid,s))
          case e => {
            println("Fail")
            println(e)
          }
          case _ => // Fail
        }

      }

      case Get(Action,v:Int,s) => {
        client.getAction(v,s) match {
          case Some(r: RemoteAction) => respond(Set(Action,r,s))
          case e => {
            println("Fail")
            println(e)
          }
          case _ => //Fail
        }
      }

      case Get(ActionId,v,s) => {

        client.getActionId(s) match {
          case id: Int => respond(Set(ActionId,id,s))
          case e => {
            println("Fail")
            println(e)
          }
          case _ => // Fail
        }
      }
      case e => {
        println("Out")
        println(e)
      }
      case _ => // No comprehendo

    }

  }

  def act {
    loop {
      react{
        case in: Array[Byte] => {
          com.siigna.util.io.Unmarshal[RemoteCommand](in) match {

            case Some(r) => handle(r)

            case None =>  //Fail

          }
        }
      }
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
 * @param timeout  The timeout in milliseconds before we re-send a request to the server.
 *                 Defaults to 10 seconds (10000 ms).
 */
class Server(host : String, mode : Mode.Mode, val timeout : Int = 10000) {

  // The remote server
  //private val client = new Client("http://62.243.118.234:20005")
  private val client = new Client("http://"+host+":"+mode.id)
  private val remote = new RestEndpoint(client) //select(Node(host, mode.id), 'siigna// )
  remote.start()

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
        // Marshal and send the message
        val output = Marshal(message).array

        remote.!?(timeout, output) match {
          case Some(data : Array[Byte]) => { // Call the callback function
            // Parse the data
            Unmarshal[RemoteCommand](data) match {
              case Some(x) => f(x)
              case x       => f(Error(401, "Failed to parse data to expected type. See log for details.", message.session))
            }

            // We're now connected for sure
            if (_retries > 0) Log.debug("Server: Connection (re)established after " + retries + " attempts.")
            _retries = 0 // Reset retries
          }
          case e => { // Timeout
            // Increment retries
            if (_retries < 0) {
              _retries = 0
            } else {
              _retries += 1
            }

            if (_retries % 10 == 0) {
              Log.warning("Server: Connection failed after " + retries + " attempts, retrying: " + message + ".")
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