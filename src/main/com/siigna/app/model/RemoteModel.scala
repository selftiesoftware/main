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

package com.siigna.app.model

import action.{CreateShape, DeleteShape}
import com.siigna.app.controller.Controller
import com.siigna.app.controller.remote._
import com.siigna.app.Siigna

/**
 * A RemoteModel with the responsibilities to keep track of information relevant in the
 * communications between the client and the server, for example the unique shape ids
 * necessary for each shape.
 */
trait RemoteModel {

  /**
   * An integer to keep track of the local ids.
   */
  protected var localCounter = 0

  /**
   * A queue of unique ids received from the server.
   */
  protected var idBank : Seq[Int] = Seq()
  
  /**
   * A queue of all the shapes with local ids that needs new ids.
   */
  protected var localIds : Seq[Int] = Seq()

  /**
   * Retrieves a unique id for a shape in the model. The ids are retrieved from the server,
   * but if no connection could be made or the cached ids does not contain enough ids, a
   * local id is given temporarily.
   * @return  A positive integer if the id comes from the server, otherwise negative.
   */
  def getId = {
    if (!idBank.isEmpty) {
      val id = idBank.head
      idBank = idBank.tail
      id
    } else {
      localCounter -= 1 // Decrement by one (avoid collision with server ids)
      localIds = localIds :+ localCounter

      // Send a request for more ID's
      if (Siigna.client.isDefined) {
        Get(ShapeIdentifier, Some(10), Siigna.client.get)
      }

      localCounter
    }
  }
  
  def setIdBank(xs : Seq[Int]) {
    // If there are no local ids, then just save the ids
    if (localIds.isEmpty) {
      idBank ++= xs

    // Otherwise we can use them to replace local ids
    } else {
      xs.foreach(id => {
        // Store the local id if we can't use it
        if (localIds.isEmpty) idBank :+ id
        // Otherwise store it for the local id
        else {
          // Get the id and the shape
          val localId = localIds.head
          val shape = Model(localId)

          // Remove the used id
          localIds = localIds.tail
          
          // Delete the shape (locally)
          Model execute(DeleteShape(localId, shape), false)
          
          // Add the shape with the new id (remotely)
          Model execute(CreateShape(id, shape), true)
        }
      })
    }
  }
  
}
