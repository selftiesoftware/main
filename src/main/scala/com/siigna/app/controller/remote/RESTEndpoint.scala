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
 */
class RESTEndpoint {

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
   * A method that sends a synchronous GET command over HTTP. The endpoint repeats the
   * call until something is received, being an error or the actual data.
   * @param url  The url of the GET request
   */
  def get(url : String) : Either[Array[Byte], String] = {
    try {
      dispatch(new Connection(url), _.get)
    } catch {
      case e : StackOverflowError => {
        shouldExit = true
        Right(s"Remote: Too many retries $retries means stack overflow :-( Shutting down!")
      }
    }
  }

  /**
   * A method that sends a synchronous POST command over HTTP with some data attached. The
   * endpoint repeats the call until something is received, being an error or the actual data.
   * @param url  The URL of the POST request
   * @param message  The message to send as a remote command
   */
  def post(url : String, message : Array[Byte]) : Either[Array[Byte], String] = {
    try {
      dispatch(new Connection(url), _.post(message))
    } catch {
      case e : StackOverflowError => {
        shouldExit = true
        Right(s"Remote: Too many retries $retries means stack overflow :-( Shutting down!")
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
   * @param connection  The connection to open
   * @param handler  A function to retrieve the data from the connection
   * @return  The obj if it was successfully returned, otherwise an error message.
   */
  @tailrec protected final def dispatch(connection : Connection, handler : Connection => Either[Array[Byte], String]) : Either[Array[Byte], String] = {
    if (shouldExit) {
      Right("Server: Cannot dispatch message; closing.")
    } else {

      handler(connection) match {
        case Left(arr) => { // Call the callback function
          Log.debug(s"REST received: $arr")

          // We're now connected for sure
          if (_retries > 0) Log.debug("Server: Connection (re)established after " + retries + " attempts.")

          Log.debug(s"REST URL: ${connection.url}")
          _retries = 0 // Reset retries

          Left(arr)
        }
        case Right(message) => { // Connection issue or unexpected reply
          // Increment retries
          _retries += 1

          if (_retries % 10 == 0) {
            Log.warning(s"Server: $message.")
          }

          // Retry
          dispatch(connection, handler)
        }
      }
    }
  }

  /**
   * An integer describing how many times we have attempted to send the latest message. This can be quite large
   * since the server never gives up.
   * @return An integer > 0 describing the number of times the latest message has been sent without a reply.
   */
  def retries = math.max(_retries, 0)

}