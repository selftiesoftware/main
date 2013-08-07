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

import java.nio.ByteBuffer
import org.ubjson.io.{ByteArrayInputStream, UBJInputStream, ByteBufferInputStream}
import version.IOVersion
import com.siigna.util.Log
import reflect.runtime.universe._
import java.io.InputStream

/**
 * <p>
 *   Unmarshals known objects from binary encoding according to the [http://ubjson.org UBJSON]
 *   (Universal Binary JSON) standard.
 *   We use [[java.nio.ByteBuffer]]s internally since they in many ways are faster than output streams.
 *   See [[com.siigna.util.io.ObjectType]] for a reference on known types.
 * </p>
 * <p>
 *   <b>Important: If not type tag is given we can not parse the type, and an exception is thrown.</b> This is done
 *   to avoid bugs where the return-type was None due to a missing type-parameter.
 * </p>
 * <p>
 *   Also, due to type erasure Unmarshal currently cannot retrieve native arrays. It has something to do with type erasure
 *   and casting of native java arrays. If you need arrays we recommend that you retrieve a scala-collection
 *   (Traversable for instance) and run <code>.toArray</code> on it. That should do the trick.
 * </p>
 *
 * <h2>Examples</h2>
 * {{{
 *   // Writing and reading a double
 *   val marshalledDouble = Marshal(42d)
 *   val double = Unmarshal[Double](marshalledDouble) // Some(42)
 *
 *   // Writing and reading a shape
 *   val marshalledLineShape = Marshal(LineShape(0, 0, 1, 1)
 *   val object = Unmarshal[LineShape](marshalledLineShape) // Some(LineShape(0, 0, 1, 1)
 *
 *   // Writing and reading a collection
 *   val marshalledSeq = Marshal(Seq(1, 1, 2, 3, 5, 7))
 *   val object = Unmarshal[Seq[Int]](marshalledArray) // Some(Seq(1, 1, 2, 3, 5, 7))
 *
 *   // Similar with maps
 *   val marshalledMap = Map(1 -> 1, 2 -> 3, 5 -> 7)
 *   val object = Unmarshal[Map[Int, Int]](marshalledMap) // Some(Map(1 -> 1, 2 -> 3, 5 -> 7))
 *
 * }}}
 */
object Unmarshal {

  /**
   * <p>
   *   Attempts to read a well-known Siigna object from the given array of bytes and cast it to the
   *   given type T. If no type is given we cannot cast or return the type, so an exception will be cast.
   * </p>
   * @param array  The array of bytes containing the data to be read.
   * @tparam T  The expected type of the object to be read.
   * @return  Some[T] if the object could be read and successfully parsed, None otherwise
   * @throws UBJFormatException  If the formatting could not be understood.
   * @throws IOException  If an I/O error occurred.
   * @throws IllegalArgumentException  If no type was specified.
   */
  def apply[T : TypeTag](array : Array[Byte]) : Option[T] = apply[T](new ByteArrayInputStream(array))

  /**
   * <p>
   *   Attempts to read a well-known Siigna object from the given [[java.nio.ByteBuffer]] and cast it to the
   *   given type T. If no type is given we cannot cast or return the type, so an exception will be cast.
   * </p>
   * @param buffer  The ByteBuffer containing the data to be read.
   * @tparam T  The expected type of the object to be read.
   * @return  Some[T] if the object could be read and successfully parsed, None otherwise
   * @throws UBJFormatException  If the formatting could not be understood.
   * @throws IOException  If an I/O error occurred.
   * @throws IllegalArgumentException  If no type was specified.
   */
  def apply[T : TypeTag](buffer : ByteBuffer) : Option[T] = apply[T](new ByteBufferInputStream(buffer))

  /**
   * <p>
   *   Attempts to read a well-known Siigna object from the given InputStream and cast it to the
   *   given type T. If no type is given we cannot cast or return the type, so an exception will be cast.
   * </p>
   * @param input  The InputStream containing the data to be read.
   * @tparam T  The expected type of the object to be read.
   * @return  Some[T] if the object could be read and successfully parsed, None otherwise
   * @throws UBJFormatException  If the formatting could not be understood.
   * @throws IOException  If an I/O error occurred.
   * @throws IllegalArgumentException  If no type was specified.
   */
  def apply[T : TypeTag](input : InputStream) : Option[T] = {
    val expectedType = typeOf[T]
    if (expectedType =:= typeOf[Nothing]) throw new IllegalArgumentException("Unmarshal: Please specify return type")

    val in = getInputStream(input)

    // Read the "main" object
    try {
      val x = in.readObject[T]
      Log.debug("Unmarshal: Successfully unmarshalled object " + x)
      Some(x)
    } catch {
      case e : ClassCastException => Log.warning("Unmarshal: " + e.getMessage); None
      case e : Throwable => Log.warning("Unmarshal: Error while unmarshalling: " + e); None
    }
  }

  /**
   * Creates the correct version of a [[com.siigna.util.io.SiignaInputStream]] capable of reading binary data in the
   * UBJSON format, for the given input stream.
   * @param input  The input stream containing the data to be read.
   * @return  A SiignaInputStream that can read data.
   */
  def getInputStream(input : InputStream) = {
    val UBJInput    = new UBJInputStream(input)

    // Read the version
    assert(UBJInput.readString() == "version", "Unmarshal: Could not identify version number")
    val version = UBJInput.readByte
    val inputVersion = IOVersion(version)

    // Return
    new SiignaInputStream(input, inputVersion)
  }

}
