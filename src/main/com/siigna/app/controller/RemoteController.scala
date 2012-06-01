/*
 * Copyright (c) 2012. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.controller

import com.siigna.util.logging.Log
import remote._
import com.siigna.app.model.Model
import actors.remote.RemoteActor._
import actors.remote.{RemoteActor, Node}
import com.siigna.app.Siigna
import com.siigna.app.model.server.Drawing

/**
 * An object whose sole responsibility is to handle incoming and outgoing
 * [[com.siigna.app.controller.remote.RemoteCommand]]s.
 */
protected[app] object RemoteController extends PartialFunction[RemoteCommand, Unit] {

  // Set remote class loader
  RemoteActor.classLoader = getClass.getClassLoader

  /**
   * The connection to the server (sink).
   */
  val sink = select(Node("siigna.com", 20004), 'siigna)

  /**
   * The unique identifier for this client.
   */
  var client : Option[Client] = None

  /**
   * Examines the input command and handles it appropriately.
   * @param command  The RemoteCommand to process.
   */
  def apply(command : RemoteCommand) { 
    try {
      command match {
          // Catch successes - we know these are from the server
          case success : Success => {
            // Examine what was successful
            success.command match {
  
              // Successful registration of the client
              case r : Register => {
                // Log the received client
                Log.info("RemoteController: Registered client: " + r.client)
  
                // Store the client
                client = Some(r.client)

                // Store the drawing id
                if (r.drawingId.isDefined) Siigna.drawing = Drawing(r.drawingId.get)
                
                Log.debug("RemoteController: Sucessfully registered client: " + client)
              }
              case _ => Log.warning("RemoteController: Received unknown success: " + success)
            }
          }
          case failure : Failure => {
            Log.warning("Remote command " + failure.command + " failed with message: " + failure.message)
          }
  
          // Forward everything else to the server. If it is not a Success type we can be
          // sure the remote command are meant to be forwarded to the server
          case _ => sink ! command
      }
  
    } catch {
      case e => Log.warning("RemoteController: Warning while processing remote command.", e)
    } 
  }

  /**
   * Examines if the function is defined for the given input. Defaults to true for all RemoteCommands.
   * @param x  The input to examine.
   * @return  True, since the incoming type is always type RemoteCommand.
   */
  def isDefinedAt(x : RemoteCommand) = true

}
