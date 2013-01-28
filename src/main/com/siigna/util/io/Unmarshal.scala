package com.siigna.util.io

import java.nio.ByteBuffer

/**
 * Unmarshals
 */
object Unmarshal {

  /**
   * Attempts to read a well-known Siigna object from the given [[java.nio.ByteBuffer]]. Internally we convert wrap it
   * as a [[org.ubjson.io.UBJInputStream]].
   * @param buffer  The ByteBuffer containing the data to be read.
   * @tparam T  The type of the object to be read.
   * @return  Some[T] if the object could be read and successfully parsed, None otherwise
   */
  def apply[T](buffer : ByteBuffer) : Option[T] = {
    None
  }

  /**
   * Attemps to read a well-known Siigna object from the given byte array.
   * @param array  The array to read the object from.
   * @tparam T  The type of the object to be retrieved.
   * @return  Some[T] if the object could be read and successfully parsed, None otherwise
   */
  def apply[T](array : Array[Byte]) : Option[T] = apply(ByteBuffer.wrap(array))

}
