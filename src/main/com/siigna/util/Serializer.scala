package com.siigna.util

import java.io._
import com.siigna.app.model.{Model, RemoteModel}
import com.siigna.util.collection.Attributes
import com.siigna.app.model.action.RemoteAction

/**
 * A Serializer to marshal and unmarshal objects, in particular [[com.siigna.app.model.Drawing]] and
 * [[com.siigna.app.model.action.RemoteAction]].
 *
 * Author: csp
 */
object Serializer {

  // Writes a serializable object to a byte array
  private def writeToBytesSer[T<:Serializable] (ser: T): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(baos)
    oos.writeObject(ser)
    baos.toByteArray
  }

  /**
   * Read an object from a byte array.
   * @param arr  The array to read
   * @return  An object
   */
  def readFromBytes(arr: Array[Byte]): AnyRef = {
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
   * Write a remote action to a byte array
   * @param ra The RemoteAction to write
   * @return An array of bytes containing the RemoteAction
   */
  def writeAction(ra: RemoteAction): Array[Byte] = {
    writeToBytesSer(ra)
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

  /**
   * Writes a drawing to a byte array
   * @param rm  The remote model to write
   * @return A byte array containing the RemoteModel.
   */
  def writeDrawing(rm: RemoteModel): Array[Byte] = {
    val bytes = new ByteArrayOutputStream
    val out = new ObjectOutputStream(bytes)
    rm.writeExternal(out)

    bytes.toByteArray
  }
}
