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
import actors.Actor
import com.siigna.app.model.RemoteModel
import java.io.{ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream, ObjectInputStream}
import RemoteConstants._

/**
 * Controls any remote connection(s).
 * If the client is not online or no connection could be made we simply wait until a connection can be
 * re-established before pushing all the received events/requests in the given order.
 */
protected[controller] object RemoteController {

  // Set remote class loader
  RemoteActor.classLoader = getClass.getClassLoader

  // The unique identifier for this client.
  protected var client : Option[Client] = None

  // A boolean flag to indicate if this controller has been successfully registered with the server.
  protected var isConnected = false

  // A timestamp for last ping attempt
  protected var lastPingAttempt : Long = 0L

  // A map of local ids mapped to their remote counterparts
  protected var localIdMap : Map[Int, Int] = Map()

  // A queue of commands waiting to be sent to the server.
  protected var queue : Seq[Client => RemoteCommand] = Seq()

  // The remote server
  protected val remote = select(Node("62.243.118.234", 20004), 'siigna)
  //protected val remote = select(Node("localhost", 20004), 'siigna)

  com.siigna.app.model.Drawing.setAttribute("title", "operahuset")
  com.siigna.app.model.Drawing.setAttribute("id", 1)

  val SiignaDrawing = com.siigna.app.model.Drawing // Use the right namespace

  // The local sink, receiving actions from the remote sink
  protected val local : Actor = actor {
    /*if (Siigna.user.isDefined && SiignaDrawing.attributes.int("id").isDefined) {
      remote.send(Register(Siigna.user, SiignaDrawing.attributes.int("id"), client.getOrElse(com.siigna.app.controller.Client())), local)
      Log.info("Remote: Registering with user " + Siigna.user + " and drawing " + SiignaDrawing.attributes.int("id"))
    } */

    // TEST!!!!
    // import com.siigna.app.model.server._
    //isConnected = true
    //println("Sending Register")
    //remote.send(Register(User("Jens"), Some(31), Client(31)), local)

    loop {
      react {
        // Successful user registration
        case cl: Client => {
          // Log the received client
          println("Remote: Registered client: " +cl)
          // Store the client
          this.client = Some(cl)

          remote.send(Get(Drawing, Some(SiignaDrawing.attributes.int("id").get), cl), local)

          /*
          if (!SiignaDrawing.attributes.int("id").isDefined)
            remote.send(Get(DrawingId, None, cl), local)
            */

          // Empty the queue
          dequeue(cl)
        }


        case Set(typ, value, client) => {
          typ match {

            case Drawing => {
              //SiignaDrawing.shapes = value.getAsInstanceOf[RemoteModel].shapes
              value.get match {
                case rem: RemoteModel => {

                  val baos = new ByteArrayOutputStream()
                  val oos = new ObjectOutputStream(baos)

                  rem.writeExternal(oos)
                  val modelData = baos.toByteArray

                  val drawData = new ObjectInputStream(new ByteArrayInputStream(modelData))

                  SiignaDrawing.readExternal(drawData)
                }
                case e => println("Got a weird drawing "+e)
              }
            }

            case _ =>
          }
        }

        case false => {
          isConnected = false;
          println("Authentication has failed you. Please log in again")
        }

        case msg => {
          println("Remote: Received: " + msg)
          Controller ! msg
        }
      }
    }
  }

  /**
   * Sends an action remotely.
   * @param action The action to dispatch.
   * @param undo Should the action be undone?
   */
  def ! (action : Action, undo : Boolean) {
    queue :+= ((c : Client) => RemoteAction(c, action, undo))
    Log.success("RemoteController: Successfully handled action: " + action)
  }
  
  /**
   * Enqueues a message to the remote server while providing a client to the function. If no client can be found
   * (failure to registrate) or no connection can be made, then the message is enqueued and sent as soon
   * a connection is established. Messages are sent in the order they are received.
   */
  def ! (f : Client => RemoteCommand) {
    client match {
      // Send the message to the client and provide a return channel
      case Some(c) if (isOnline) => { remote.send(f(c), local); }
      // Enqueue it and wait for a connection
      case _ => queue :+= f
    }
  }

  /**
   * Dequeues the enqueued commands in the queue.  
   * If the action is local (see [[com.siigna.app.model.action.Action.isLocal]])
   * we query the server for ids so we can be certain everything is synchronized with the server.
   * @param client  The client to authorize the commands.
   */
  protected def dequeue(client : Client) { if (!queue.isEmpty ) {
    Log.info("Remote: Sending queue of size: " + queue.size)

    // Send and dequeue the enqueued messages
    queue = queue.foldLeft(queue)((q : Seq[Client => RemoteCommand], f : (Client => RemoteCommand)) => {

      // Retrieve the command
      val command : RemoteCommand = f(client) match {
        case remoteAction : RemoteAction => {
          val action = remoteAction.action

          // Query for remote ids if the action is local
          if (action.isLocal) {
            val localIds = action.ids.filter(_ < 0).toIterator
            
            // Map the ids with existing key-pairs
            val ids = localIds.map(i => localIdMap.getOrElse(i, i))
            
            // Do we still have local ids? 
            val updatedAction = if (ids.exists(_ < 0)) {
              // Find the local ids
              val localIds = ids.filter(_ < 0)

              // .. Then we need to query the server for ids
              remote !? Get(ShapeIdentifier, Some(localIds.size), client) match {
                case Set(ShapeIdentifier, Some(i : Range), _) => {
                  
                  // Find out how the ids map to the action
                  val map = for (n <- 0 until localIds.size) yield localIds.next() -> i(n)
                  
                  // Update the map in the remote controller
                  localIdMap ++= map

                  // Update the model
                  SiignaDrawing.execute(UpdateLocalActions(localIdMap))

                  // Return the updated action
                  action.update(localIdMap)
                }
                case e => throw new UnknownError("Remote: Expected Set(ShapeIdentifier, _, _), got " + e)
              }
            } else { // Else give the action the new ids
              action.update(localIds.zip(ids).toMap)
            }

            // Dispatch the remote command
            RemoteAction(client, updatedAction, remoteAction.undo)
          } else { // Else simply just dispatch
            remoteAction
          }
        }
        case cmd => cmd // Just return
      }
      
      // Send it to the server
      remote.send(command, local)
      q.tail
    })
  } }

  /**
   * Defines whether the client is connected to a remote server or not.
   * @return true if connected, false if not.
   */
  def isOnline = isConnected

  // TODO: Can we incorporate this in a nicer way?
  protected val pingThread = new Thread("Remote ping loop") {
    override def run() {
      try {
        while (true) {
          // Ping the server
          if (client.isDefined) {
            // Request an answer com.siigna.app.model.Drawing.attributes.string("title")
            remote !? (5000, client.get) match {
              case Some(b : Boolean) => { isConnected = b }
              case None => {
                isConnected = false
                Log.warning("Remote: Server timeout")
              }
            }

            // Empty the queue
            if (isConnected) {
              dequeue(client.get)
            }
            
            // Register if the client is empty, but the title AND user has been set
          } else if (SiignaDrawing.attributes.string("title").isDefined && Siigna.user.isDefined) {
            println("No client")
            remote.send(Register(Siigna.user, SiignaDrawing.attributes.int("id"), com.siigna.app.controller.Client()), local)
          }


          // Ping every 5 seconds (dev)
          Thread.sleep(5000)
        }
      } catch {
        case e => println("Ping got "+e)
      }
    }
  }.start()

}
