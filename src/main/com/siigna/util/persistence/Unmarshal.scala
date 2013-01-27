package com.siigna.util.persistence

import com.siigna.app.model.action.RemoteAction
import java.io.{ByteArrayInputStream, ObjectInputStream}
import com.siigna.app.model.{Model, RemoteModel}
import com.siigna.util.collection.Attributes

/**
 * Unmarshals
 */
object Unmarshal {

  /**
   * Read an object from a byte array.
   * @param arr  The array to read
   * @return  An object
   */
  private def readFromBytes(arr: Array[Byte]): AnyRef = {
    val stream = new ObjectInputStream(new ByteArrayInputStream(arr))
    stream.readObject()
  }

  /**
   * Attempts to read an action from a byte array.
   * @param arr  The array with the action-bytes
   * @return An instance of a RemoteAction
   */
  def readAction(arr: Array[Byte]): RemoteAction = {
    readFromBytes(arr).asInstanceOf[RemoteAction]
  }

  /**
   * Attempts to read a [[com.siigna.app.model.RemoteModel]] from the given byte array
   * @param arr The array to read from
   * @return A remote model
   */
  def readDrawing(arr: Array[Byte]): RemoteModel = {
    val rmodel = new RemoteModel(new Model(Map(), Seq(), Seq()), Attributes())
    val stream = new ObjectInputStream(new ByteArrayInputStream(arr))
    rmodel.readExternal(stream)

    rmodel
  }

}
