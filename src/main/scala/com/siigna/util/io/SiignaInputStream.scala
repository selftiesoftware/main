/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */

package com.siigna.util.io

import java.io.InputStream
import org.ubjson.io.{UBJFormatException, UBJInputStream}
import org.ubjson.io.IUBJTypeMarker._
import version.IOVersion
import java.nio.CharBuffer
import reflect.runtime.universe._
import reflect.ClassTag

/**
 * An input stream capable of reading objects familiar to the Siigna domain. Uses the [[org.ubjson.io.UBJInputStream]]
 * to use the UBJSON functionality.
 * @param in  The InputStream from which to read data.
 * @see [[http://ubjson.org]] Universal Binary JSON
 */
class SiignaInputStream(in : InputStream, val version : IOVersion) extends UBJInputStream(in) {

  /**
   * Affirms that the next member name equals the given string, by assertion.
   * @param name  The expected name of the next member.
   */
  def checkMemberName(name : String) {
    val actual = readString()
    if(!actual.equals(name)) throw new UBJFormatException(pos, s"SiignaInputStream: Expected '$name', got '$actual'")
  }

  /**
   * Reads a member of an object with the expected type E and the expected name of the member.
   * @param name  The name of the expected member
   * @tparam E  The expected type of the member
   * @return  An object of the given type.
   * @throws ClassCastException  If the object found could not be casted to the given type T
   */
  def readMember[E : TypeTag : ClassTag](name : String) : E = {
    checkMemberName(name)
    readObject[E]
  }

  /**
   * Matches the expected type E with the actual type A. If A is a subtype of E (using the <:< notation) we
   * execute the function f and attempts to cast it to the expected type E. If not we throw a class cast exception.
   * This methods allows us to do recursive type-casting, so we can provide type-safety for nested types.
   * @param f  The function to execute if the types match.
   * @tparam E  The type we expect to return.
   * @tparam A  The type we actually found.
   * @return  An object of the expected type E. If the types did not match we throw a ClassCastException
   * @throws  ClassCastException  If the type A is not the same class as E or a sub-class to E
   */
  def readType[E : TypeTag, A : TypeTag](f : => Any) : E = {
    val actual = typeOf[A]
    val expected = typeOf[E]
    actual match {
      case _ if (actual <:< expected) => f.asInstanceOf[E]
      case _ => throw new ClassCastException(s"Could not cast the found type $actual to the expected type $expected.")
    }
  }

  /**
   * Reads and parses a single object by first checking the type of the object and then attempting to create an
   * instance of that object.
   * @return  An object read from the input stream.
   * @throws  ClassCastException  If the returned type differed from the expected.
   */
  def readObject[T : TypeTag] : T = {
    // Match on the next valid byte
    nextType() match {
      // UBJSON types
      case NULL   => readType[T, Null](null)
      case BYTE   => readType[T, Byte](read.toByte)
      case DOUBLE => readType[T, Double](java.lang.Double.longBitsToDouble(readInt64Impl()))
      case FALSE  => readType[T, Boolean](false)
      case FLOAT  => readType[T, Float](java.lang.Float.intBitsToFloat(readInt32Impl()))
      case INT32  => readType[T, Int](readInt32Impl)
      case INT64  => readType[T, Long](readInt64Impl)
      case TRUE   => readType[T, Boolean](true)
      case STRING => readType[T, String]({
        val buffer : CharBuffer = CharBuffer.allocate(readInt32())
        readStringBodyAsCharsImpl(buffer.capacity(), buffer)
        new String(buffer.array, buffer.position, buffer.remaining)
      })
      case STRING_COMPACT => readType[T, String]({
        val buffer : CharBuffer = CharBuffer.allocate(read())
        readStringBodyAsCharsImpl(buffer.capacity(), buffer)
        new String(buffer.array, buffer.position, buffer.remaining)
      })

      // Siigna object
      case OBJECT         => version.readSiignaObject[T](this, readObjectLength())
      case OBJECT_COMPACT => version.readSiignaObject[T](this, read)

      case e => throw new IllegalArgumentException("SiignaInputStream: Unknown type " + e)
    }
  }

}
