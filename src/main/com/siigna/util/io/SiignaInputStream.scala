package com.siigna.util.io

import java.io.InputStream
import org.ubjson.io.{UBJFormatException, UBJInputStream}
import org.ubjson.io.IUBJTypeMarker._
import version.IOVersion
import java.nio.CharBuffer
import reflect.runtime.universe.{typeOf, Type}

/**
 * An input stream capable of reading objects familiar to the Siigna domain. Uses the [[org.ubjson.io.UBJInputStream]]
 * to use the UBJSON functionality.
 * @param in  The InputStream from which to read data.
 * @see [[http://ubjson.org]] Universal Binary JSON
 */
class SiignaInputStream(in : InputStream, version : IOVersion) extends UBJInputStream(in) {

  /**
   * Affirms that the next member name equals the given string, by assertion.
   * @param name  The expected name of the next member.
   */
  def checkMemberName(name : String) {
    val actual = readString()
    if(!actual.equals(name)) throw new UBJFormatException(pos, s"SiignaInputStream: Expected '$name', got '$actual'")
  }

  /**
   * Reads a member of an object with the given expected name and type.
   * @param name  The name of the expected member
   * @tparam T  The type of the expected member
   * @return  An object of the given type.
   * @throws ClassCastException  If the object found could not be casted to the given type T
   */
  def readMember[T](name : String) = {
    checkMemberName(name)
    readType[T]
  }

  /**
   * Reads and parses a single object by first checking the type of the object and then attempting to create an
   * instance of that object.
   * @return  An object read from the input stream
   */
  def readObject : (Type, Any) = {
    // Match on the next valid byte
    nextType() match {
      // UBJSON types
      case BYTE   => typeOf[Byte]    -> read
      case DOUBLE => typeOf[Double]  -> java.lang.Double.longBitsToDouble(readInt64Impl())
      case FALSE  => typeOf[Boolean] -> false
      case FLOAT  => typeOf[Float]   -> java.lang.Float.intBitsToFloat(readInt32Impl())
      case INT32  => typeOf[Int]     -> readInt32Impl
      case INT64  => typeOf[Long]    -> readInt64Impl
      case TRUE   => typeOf[Boolean] -> true
      case STRING => typeOf[String]  -> {
        val buffer : CharBuffer = CharBuffer.allocate(readInt32)
        readStringBodyAsCharsImpl(buffer.capacity(), buffer)
        new String(buffer.array, buffer.position, buffer.remaining)
      }
      case STRING_COMPACT => typeOf[String] -> {
        val buffer : CharBuffer = CharBuffer.allocate(read)
        readStringBodyAsCharsImpl(buffer.capacity(), buffer)
        new String(buffer.array, buffer.position, buffer.remaining)
      }

      // Siigna object
      case OBJECT         => version.readSiignaObject(this, readObjectLength())
      case OBJECT_COMPACT => version.readSiignaObject(this, read)

      case e => throw new IllegalArgumentException("SiignaInputStream: Unknown type " + e)
    }
  }

  /**
   * Reads an object and attempts to cast it to type T.
   * @tparam T  The type to return.
   * @return  An object of type T. Or an exception.
   */
  protected def readType[T] : T = readObject._2.asInstanceOf[T]

}
