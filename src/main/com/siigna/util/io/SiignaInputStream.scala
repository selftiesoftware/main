package com.siigna.util.io

import java.io.InputStream
import org.ubjson.io.UBJInputStream
import org.ubjson.io.IUBJTypeMarker._
import version.IOVersion

/**
 * An input stream capable of reading objects familiar to the Siigna domain. Uses the [[org.ubjson.io.UBJInputStream]]
 * to use the UBJSON functionality.
 * @param in  The InputStream from which to read data.
 * @see [[http://ubjson.org]] Universal Binary JSON
 */
class SiignaInputStream(in : InputStream, version : IOVersion) extends UBJInputStream(in) {

  /**
   * Reads and parses a single object by first checking the type of the object and then attempting to create an
   * instance of that object.
   * @return  An object with the type found in [[com.siigna.util.io.Type]].
   */
  def readObject : Any = {
    // Find the next valid byte
    nextType() match {
      // UBJSON types
      case BYTE   => read
      case DOUBLE => java.lang.Double.longBitsToDouble(readInt64Impl())
      case FALSE  => false
      case FLOAT  => java.lang.Float.intBitsToFloat(readInt32Impl())
      case INT32  => readInt32Impl
      case INT64  => readInt64Impl
      case TRUE   => true

      // Iterable and map
      case ARRAY          => new Array[Any](readInt32).map(_ => readObject)
      case ARRAY_COMPACT  => new Array[Any](read).map(_ => readObject)

      // Siigna object
      case OBJECT         => version.readSiignaObject(this, readInt32)
      case OBJECT_COMPACT => version.readSiignaObject(this, read)
    }
  }

  /**
   * Reads an object and attempts to cast it to type T.
   * @tparam T  The type to return.
   * @return  An object of type T. Or an exception.
   */
  def readType[T] : T = readType.asInstanceOf[T]

}
