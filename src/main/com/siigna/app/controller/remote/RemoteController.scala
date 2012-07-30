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
import com.siigna.app.model.action.Action
import actors.{Scheduler, Future, Actor}
import java.util.concurrent.Executors

/**
 * Controls any remote connection(s).
 * If the client is not online or no connection could be made we simply wait until a connection can be
 * re-established before pushing all the received events/requests in the given order.
 */
protected[controller] object RemoteController {

  // Set remote class loader
  RemoteActor.classLoader = getClass.getClassLoader

  /**
   * The unique identifier for this client.
   */
  var client : Option[Client] = None

  /**
   * A boolean flag to indicate if this controller has been successfully registered with the server.
   */
  protected var isConnected = false

  // A timestamp for last ping attempt
  protected var lastPingAttempt : Long = 0L

  // A queue of commands waiting to be sent to the server.
  protected var queue : Seq[Client => RemoteCommand] = Seq()

  // The local sink, receiving actions from the remote sink
  protected val local : Actor = actor {
    // Register the client IF the user is logged on
    // Remember: When remote commands are created, they are sent to the controller immediately
    if (Siigna.user.isDefined) {
      val Drawing = com.siigna.app.model.Drawing // Use the right namespace
      Log.debug("Controller: Registering with user " + Siigna.user + " and drawing " + Drawing.attributes.int("id"))
      Register(Siigna.user.get, Drawing.attributes.int("id"), Client())
    }

    // TEST!!!!
    import com.siigna.app.model.server._
    //isConnected = true
    //println("Sending Register")
    //remote.send(Register(User("Jens"), Some(31), Client(31)), local)

    loop {
      react {
        // Successful registration of the client
        case client : Client => {
          // Log the received client
          Log.info("Remote: Registered client: " + client)

          // Store the client
          this.client = Some(client)

          // Empty the queue
          dequeue(client)

          //remote.send(Get(Drawing, Some(31), client), local)

          // Get shape-ids for the id-bank
          remote.send(Get(ShapeIdentifier, Some(4), client), local)

          Log.debug("Remote: Sucessfully registered client: " + client)
        }
        case msg => {
          Log.info("Remote: Received: " + msg)
          Controller ! msg
        }
      }
    }
  }

  // The remote server
  protected val remote = select(Node("localhost", 20004), 'siigna)

  /**
   * Sends an action remotely.
   * @param action The action to dispatch.
   * @param undo Should the action be undone?
   */
  def ! (action : Action, undo : Boolean) {
    queue :+= ((c : Client) => RemoteAction(c, action, undo))
  }
  
  /**
   * Enqueues a message to the remote server while providing a client to the function. If no client can be found
   * (failure to registrate) or no connection can be made, then the message is enqueued and sent as soon
   * a connection is established. Messages are sent in the order they are received.
   */
  def ! (f : Client => RemoteCommand) {
    client match {
      // Send the message to the client and provide a return channel
      case Some(c) if (isOnline) => { remote.send(f(c), local) }
      // Enqueue it and wait for a connection
      case _ => queue :+= f
    }
  }

  /**
   * Dequeues the enqueued commands in the queue.
   * @param client  The client to authorize the commands.
   */
  protected def dequeue(client : Client) {
    // Send and dequeue the enqueued messages
    queue = queue.foldLeft(queue)((q : Seq[Client => RemoteCommand], f : (Client => RemoteCommand)) => {
      remote.send(q.head(client), local)
      q.tail
    })

    Log.success("Remote: Queue sent: " + q.head(client))
  }

  /**
   * Defines whether the client is connected to a remote server or not.
   * @return true if connected, false if not.
   */
  def isOnline = isConnected   

  // Pings the server
  protected val pingThread = new Thread("Remote ping loop") {
    override def run() {
      try {
        while (true) {
          // Ping the server
          if (System.currentTimeMillis() - lastPingAttempt > 10000L && client.isDefined) {
            // Request an answer
            remote !? (5000, client.get) match {
              case Some(b : Boolean) => isConnected = b
              case None => {
                isConnected = false
                Log.warning("Remote: Server timeout")
              }
            }
            // Set the last ping attempt
            lastPingAttempt = System.currentTimeMillis()
    
            // Empty the queue
            if (isConnected && !queue.isEmpty) {
              dequeue(client.get)
            }
          }
          Thread.sleep(10000)
        }
      } catch {
        case _ => 
      }
    }
  }.start()

}
