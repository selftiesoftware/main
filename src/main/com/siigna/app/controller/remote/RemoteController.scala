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

  /**
   * A queue of commands waiting to be sent to the server.
   */
  private var queue = Seq[Client => RemoteCommand]()

  // The remote server
  private val remote = actor {

    // Register the client IF the user is logged on
    // Remember: When remote commands are created, they are sent to the controller immediately
    if (Siigna.user.isDefined) {
      val Drawing = com.siigna.app.model.Drawing // Use the right namespace
      Log.debug("Controller: Registering with user " + Siigna.user + " and drawing " + Drawing.attributes.int("id"))
      Register(Siigna.user.get, Drawing.attributes.int("id"), Client(0))
    }

    loop {
      react {
        case msg => Controller ! msg
      }
    }
  }

  // Define the remote
  // TODO: What to do if we're not online??
  private val sink = select(Node("siigna.com", 20004), 'siigna)

  // TEST!!!!
  //isConnected = true
  //Register(User("Jens"), None, Client(0))

  /**
   * Enqueues a message to the remote server. If the client is not connected the messages is sent as soon
   * as a connection is established. Messages are sent in the order they are received.
   * @param message The message to forward to the remote server.
   */
  def ! (message : RemoteCommand) {
    // TODO: Check for connection
    if (isOnline)
      sink.send(message, sink)
    else {

    }

  }

  /**
   * Enqueues a message to the remote server while providing a client to the function. If no client can be found
   * (failure to registrate) or no connection can be made, then the message is enqueued and sent as soon
   * a connection is established. Messages are sent in the order they are received.
   */
  def ! (f : Client => RemoteCommand) {
    Siigna.client match {
      case Some(c) => this ! f(c)
      case None => queue = queue :+ f
    }
  }

}
