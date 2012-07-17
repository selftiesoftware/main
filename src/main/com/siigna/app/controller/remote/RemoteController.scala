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

package com.siigna.app.controller.remote

import actors.Actor._
import actors.remote.RemoteActor._
import actors.remote.{Node, RemoteActor}
import com.siigna.app.controller.{Client, Controller}
import com.siigna.app.Siigna
import com.siigna.util.logging.Log
import com.siigna.app.model.server.User

/**
 * Controls any remote connection(s).
 * If the client is not online or no connection could be made we simply wait until a connection can be
 * re-established before pushing all the received events/requests in the given order.
 */
protected[controller] object RemoteController {

  // Set remote class loader
  RemoteActor.classLoader = getClass.getClassLoader

  /**
   * A boolean flag to indicate if this controller has been successfully registered with the server.
   */
  protected var isConnected = false

  /**
   * Defines whether the client is connected to a remote server or not.
   * @return true if connected, false if not.
   */
  def isOnline = isConnected

  // The local sink, receiving actions from the remote sink
  private val local = actor {
    // Register the client IF the user is logged on
    // Remember: When remote commands are created, they are sent to the controller immediately
    if (Siigna.user.isDefined) {
      val Drawing = com.siigna.app.model.Drawing // Use the right namespace
      Log.debug("Controller: Registering with user " + Siigna.user + " and drawing " + Drawing.attributes.int("id"))
      Register(Siigna.user.get, Drawing.attributes.int("id"), Client())
    }

    // TEST!!!!
    //isConnected = true
    //Register(User("Jens"), None, Client(0))

    loop {
      react {
        case msg => {
          Controller ! msg
          isConnected = true
        }
      }
    }
  }

  /**
   * A queue of commands waiting to be sent to the server.
   */
  private var queue : Seq[Client => RemoteCommand] = Seq()

  // The remote server
  //private val remote = select(Node("siigna.com", 20004), 'siigna)
  private val remote = select(Node("localhost", 20004), 'siigna)

  // TODO: Fix this.
  def ! (command : RemoteCommand) {
    command match {
      case c : Register if (isOnline) => remote.send(command, local)
      case c : Unregister if (isOnline) => remote.send(command, local)
      case _ => {
        Controller.client match {
          case Some(c) if (isOnline) => remote.send(command, local)
          case _ =>
        }
      }
    }
  }
  
  /**
   * Enqueues a message to the remote server while providing a client to the function. If no client can be found
   * (failure to registrate) or no connection can be made, then the message is enqueued and sent as soon
   * a connection is established. Messages are sent in the order they are received.
   */
  def ! (f : Client => RemoteCommand) {
    Controller.client match {
      // Send the message to the client and provide a return channel
      case Some(c) if (isOnline) => remote.send(f(c), local)
      // Enqueue it and wait for a connection
      case _ => queue = queue :+ f
    }
  }

}
