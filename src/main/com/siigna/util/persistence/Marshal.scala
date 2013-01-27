/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.util.persistence

import java.io._
import com.siigna.app.model.{Model, RemoteModel}
import com.siigna.util.collection.Attributes
import com.siigna.app.model.action.RemoteAction
import com.siigna.app.controller.remote.RemoteCommand

/**
 * Marshals objects according to the constants defined in the [[com.siigna.util.persistence]] package.
 *
 * @author csp <csp@siigna.com>
 * @author jegp <jegp@siigna.com>
 */
object Marshal {

  def apply(attributes : Attributes) = {
    map.size
    for (e <- map) {
      out.writeUTF(e._1)
      out.writeObject(e._2)
    }
  }

  def apply(command : RemoteCommand) = {

  }

  /**
   * Writes a serializable object to a byte array.
   * @tparam T  The type of the object to serialize.
   * @param obj  The object to serialize.
   * @return  An array of bytes containing the serialized object.
   */
  private def writeToBytesSer[T <: Serializable](obj : T) : Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(baos)
    oos.writeObject(obj)
    baos.toByteArray
  }

  /**
   * Write a remote action to a byte array
   * @param ra The RemoteAction to write
   * @return An array of bytes containing the RemoteAction
   */
  def writeAction(ra: RemoteAction): Array[Byte] = {
    if (ra == null) throw new NullPointerException("Cannot serialize null")
    writeToBytesSer(ra)
  }

  /**
   * Writes a drawing to a byte array
   * @param rm  The remote model to write
   * @return A byte array containing the RemoteModel.
   */
  def writeDrawing(rm: RemoteModel): Array[Byte] = {
    if (rm == null) throw new NullPointerException("Cannot serialize null")
    val bytes = new ByteArrayOutputStream
    val out = new ObjectOutputStream(bytes)
    rm.writeExternal(out)

    bytes.toByteArray
  }
}
