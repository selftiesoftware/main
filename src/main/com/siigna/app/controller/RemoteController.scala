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
import com.siigna.app.controller.AppletParameters._
import com.siigna.app.model.Model
import actors.remote.RemoteActor._
import actors.remote.{RemoteActor, Node}

/**
 * An object whose sole responsibility is to handle incoming requests and .
 */
object RemoteController extends PartialFunction[RemoteCommand, Unit] {

  // Set remote class loader
  RemoteActor.classLoader = getClass.getClassLoader

  var client : Option[Client] = None

  // Define the sink
  protected val sink = select(Node("siigna.com", 20004), 'siigna)

  // Register the client
  // Remember: When remote commands are created, they are sent to the controller immediately
  Register(AppletParameters.contributorName, AppletParameters.readDrawingIdAsOption)

  /**
   * Examines the input command and handles it appropriately.
   * @param command  The RemoteCommand to process.
   */
  def apply(command : RemoteCommand) {

    Log.debug("Controller: Received remote command: " + command)
    command match {
        // Catch successes - we know these are from the server
        case success : Success => {
          // Examine what was successful
          success.command match {

            // Successful registration of the client
            case r : Register => {
              client = Some(r.client)
              val id = client.get.id
              Log.info("Controller registered client with id " + id)

              AppletParameters.setClient(client)
              //Hvis der er kommet en aktiv tegning fra hjemmesiden hentes den, ellers laves der en ny:
              if (AppletParameters.readDrawingIdAsOption.isDefined) {
                println("Sending get drawing command to server")
                //sink ! GetDrawingTitle(AppletParameters.readDrawingIdAsOption.get, client.get)
                //sink ! GetDrawing(AppletParameters.readDrawingIdAsOption.get, client.get)
                //GetDrawingOwnerName(readDrawingIdAsOption.get,client.get)
              } else if (client.isDefined) {
                GetNewDrawingId(client.get)
              }
              //get a specified number of new shapeIds from the server, ready to use for new shapes
              if (client.isDefined) {
                GetNewShapeIds(20,client.get)
              }
            }
            case _ =>
          }
        }
        case failure : Failure => {
          Log.warning("Remote command " + failure.command + " failed with message: " + failure.message)
        }

        // Forward everything else to the server. If it is not a Success type we can be
        // sure the remote command are meant to be forwarded to the server
        case _ => sink ! command
    }

  }

  /**
   * Examines if the function is defined for the given input. Defaults to true for all RemoteCommands.
   * @param x  The input to examine.
   * @return  True, since the incoming type is always type RemoteCommand.
   */
  def isDefinedAt(x : RemoteCommand) = true

}
