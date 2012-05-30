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

import collection.immutable.Queue

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
  protected var idBank : Queue[Int] = Queue()
  
  /**
   * A queue of all the shapes with local ids that needs new ids.
   */
  protected var localIds : Queue[Int] = Queue()

  /**
   * Retrieves a unique id for a shape in the model. The ids are retrieved from the server,
   * but if no connection could be made or the cached ids does not contain enough ids, a
   * local id is given temporarily.
   * @return  A positive integer if the id comes from the server, otherwise negative.
   */
  def getId = {
    if (!idBank.isEmpty) {
      val (id, queue) = idBank.dequeue
      idBank = queue
      id
    } else {
      localCounter -= 1 // Decrement by one (avoid collision with server ids)
      localIds = localIds.enqueue(localCounter)
      localCounter
    }
  }
  
}
