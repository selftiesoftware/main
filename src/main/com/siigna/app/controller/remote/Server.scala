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
 */
class Server(host : String, mode : Mode.Mode, timeout : Int = 4000) {

  // The remote server
  private val remote = select(Node(host, mode.id), 'siigna)

  // A boolean flag to indicate if we have a connection with this server
  protected var _isConnected = false

  /**
   * A boolean value to indicate whether a connection has been successfully made to this server.
   * @return  True if a connection is available, false otherwise
   */
  def isConnected = _isConnected

  /**
   * A method that sends a remote command synchronously with an associated callback function
   * with side effects. The method repeats the procedure until something is received.
   * @param message  The message to send
   * @param f  The callback function to execute when data is successfully retrieved
   * @tparam R  The return type of the callback function
   * @return  Right[R] if things go well, Left[Error] if not
   * @throws UnknownException  If the data returned did not match expected type(s)
   */
  def apply[R](message : RemoteCommand, f : Any => R) : Either[Error, R] = {
    Log.info("Remote: Sending: " + message)
    val res = remote.!?(timeout, message) match {
      case Some(data) => { // Call the callback function
        try {
          val r = f(data)     // Parse the data
          _isConnected = true // We're not connected for sure
          Right(r)            // Return
        } catch {
          case e : Error => Left(e)
          case e => throw new UnknownError("Remote: Unknown data received from the server: " + e)
        }
      }
      case None      => { // Timeout
        _isConnected = false // We're no longer connected
        apply(message, f) // Retry
      }
    }
    res
  }
}

/**
 * The server mode in which the server operates. There are two modes: Production and Testing.
 */
object Mode extends Enumeration {
  type Mode = Value
  val Production = Value(20004)
  val Testing    = Value(20005)
}
