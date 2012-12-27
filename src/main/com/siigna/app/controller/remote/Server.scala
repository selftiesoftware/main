package com.siigna.app.controller.remote

import actors.remote.RemoteActor._
import actors.remote.Node
import com.siigna.util.logging.Log

/**
 * An instance of a specific server located at the given host and the given port that can
 * send messages synchronously.
 *
 * @param host  The URL of the host.
 * @param mode  The mode of the connection, can be in production or testing mode
 * @param timeout  The timeout in milliseconds before we re-send a request to the server.
 *                 Defaults to 10 seconds (10000 ms).
 */
class Server(host : String, mode : Mode.Mode, val timeout : Int = 10000) {

  // The remote server
  private val remote = select(Node(host, mode.id), 'siigna)

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
   * A method that sends a remote command synchronously with an associated callback function
   * with side effects. The method repeats the procedure until something is received.
   * @param message  The message to send
   * @param f  The callback function to execute when data is successfully retrieved
   * @throws UnknownException  If the data returned did not match expected type(s)
   */
  def apply(message : RemoteCommand, f : Any => Unit) {
    try {
      if (shouldExit) {
        Log.info("Server: Connection closing", message.session)
      } else {
        remote.!?(timeout, message) match {
          case Some(data) => { // Call the callback function
            f(data) // Parse the data

            // We're now connected for sure
            if (_retries > 0) Log.debug("Server: Connection (re)established after " + retries + " attempts.")
            _retries = 0 // Reset retries
          }
          case _ => { // Timeout
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
 * The server mode in which the server operates. There are two modes: Production and Testing.
 */
object Mode extends Enumeration {
  type Mode = Value
  val Production = Value(20004)
  val Testing    = Value(20005)
}
