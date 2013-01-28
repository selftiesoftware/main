package com.siigna.util.io

import java.nio.ByteBuffer
import org.ubjson.io.{UBJInputStream, ByteBufferInputStream}
import version.IOVersion

/**
 * Unmarshals objects from binary encoding according to the [http://ubjson.org UBJSON] (Universal Binary JSON) standard.
 * We use [[java.nio.ByteBuffer]]s internally since they in many ways are faster than output streams.
 */
object Unmarshal {

  /**
   * Attempts to read a well-known Siigna object from the given [[java.nio.ByteBuffer]]. Internally we wrap it
   * as a [[org.ubjson.io.UBJInputStream]].
   * @param buffer  The ByteBuffer containing the data to be read.
   * @tparam T  The type of the object to be read.
   * @return  Some[T] if the object could be read and successfully parsed, None otherwise
   */
  def apply[T](buffer : ByteBuffer) : Option[T] = {
    val in = getInputStream(buffer)

    // Read the "main" object
    val obj = in.readObject

    obj match {
      case s : Some[T] => s
      case _ => None
    }
  }

  /**
   * Attemps to read a well-known Siigna object from the given byte array.
   * @param array  The array to read the object from.
   * @tparam T  The type of the object to be retrieved.
   * @return  Some[T] if the object could be read and successfully parsed, None otherwise
   */
  def apply[T](array : Array[Byte]) : Option[T] = apply(ByteBuffer.wrap(array))

  /**
   * Creates the correct version of a [[com.siigna.util.io.SiignaInputStream]] capable of reading binary data in the
   * UBJSON format, for the given byte buffer.
   * @param buffer  The byte buffer containing the data to be read.
   * @return  A SiignaInputStream that can read data.
   */
  def getInputStream(buffer : ByteBuffer) = {
    val bufferInput = new ByteBufferInputStream(buffer)
    val UBJInput    = new UBJInputStream(bufferInput)

    // Read the version
    assert(UBJInput.readString() == "version", "Could not identify version number")
    val version = IOVersion(UBJInput.readByte)

    new SiignaInputStream(bufferInput, version)
  }

}
