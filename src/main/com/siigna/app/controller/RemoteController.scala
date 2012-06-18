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

import actors.AbstractActor
import com.siigna.util.logging.Log
import com.siigna.app.model.Drawing
import remote._

/**
 * An object whose sole responsibility is to handle incoming and outgoing
 * [[com.siigna.app.controller.remote.RemoteCommand]]s.
 */
protected[app] object RemoteController {

  /**
   * The unique identifier for this client.
   */
  var client : Option[Client] = None

  /**
   * Examines the input command and handles it appropriately.
   * @param command  The RemoteCommand to process.
   */
  def apply(command : RemoteCommand, sink : AbstractActor) {
    try {
      command match {
        // Failure
        case failure : Failure => {
          Log.warning("Remote command " + failure.command + " failed with message: " + failure.message)
        }
        // Catch successes - we know these are from the server
        case success : Success => {
          // Examine what was successful
          success.command match {

            // Successful get shape identifiers command
            case Get(ShapeIdentifier, value, _) => {
              try {
                Drawing.addRemoteIds(value.get.asInstanceOf[Seq[Int]])
              } catch {
                case e => Log.warning("Unknown input for shape identifiers: " + value)
              }
            }

            // Successful registration of the client
            case r : Register => {
              // Log the received client
              Log.info("RemoteController: Registered client: " + r.client)

              // Store the client
              client = Some(r.client)

              // Get shape-ids for the id-bank
              Get(ShapeIdentifier, Some(4), r.client)
              
              Log.debug("RemoteController: Sucessfully registered client: " + client)
            }

            // Remote actions
            case RemoteAction(_, action, undo) => {
              if (undo) {
                Drawing undo Some(action)
              } else {
                Drawing execute(action, false)
              }
            }
            case _ => Log.warning("RemoteController: Received unknown success: " + success)
          }
        }

        // Forward every register command to the sink (without checking online-status).
        case c : Register => sink ! c

        // Forward everything else to the server. If it is not a Success type we can be
        // sure the remote command are meant to be forwarded to the server
        case _ => if (Controller.isOnline) sink ! command
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
