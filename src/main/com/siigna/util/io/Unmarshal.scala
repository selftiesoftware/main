package com.siigna.util.io

import java.nio.ByteBuffer
import org.ubjson.io.{UBJInputStream, ByteBufferInputStream}
import version.IOVersion
import com.siigna.util.Log
import reflect.runtime.universe._

/**
 * <p>
 *   Unmarshals objects from binary encoding according to the [http://ubjson.org UBJSON] (Universal Binary JSON) standard.
 *   We use [[java.nio.ByteBuffer]]s internally since they in many ways are faster than output streams.
 * </p>
 * <p>
 *   <b>Important: If not type tag is given we can not parse the type, so a type-less attempt would result in None!</b>
 * </p>
 *
 * Examples:
 * {{{
 *   // Read a double
 *   val marshalledDouble = ...
 *   val double = Unmarshal[Double](marshalledDouble)
 *
 *   // Read a line shape
 *   val marshalledLineShape = ...
 *   val object = Unmarshal[LineShape](marshalledLineShape)
 *
 *   // .. and so forth
 * }}}
 */
object Unmarshal {

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
  def apply[T : TypeTag](buffer : ByteBuffer) : Option[T] = {
    val expectedType = typeOf[T]
    if (expectedType == typeOf[Nothing]) throw new IllegalArgumentException("Unmarshal: Please specify return type")

    val in = getInputStream(buffer)

    // Read the "main" object
    try {
      val (actualType, obj) = in.readObject

      if (actualType <:< expectedType) {
        Log.success(s"Unmarshal: Successfully read type $actualType as an instance of $expectedType.")
        Some(obj.asInstanceOf[T])
      } else {
        Log.warning(s"Unmarshal: Could not cast type $actualType to $expectedType. Returning None.")
        None
      }
    } catch {
      case e : Throwable => Log.warning("Unmarshal: Error while unmarshalling: " + e); None
    }
  }

  /**
   * Attemps to read a well-known Siigna object from the given byte array.
   * @param array  The array to read the object from.
   * @tparam T  The type of the object to be retrieved.
   * @return  Some[T] if the object could be read and successfully parsed, None otherwise
   */
  def apply[T : TypeTag](array : Array[Byte]) : Option[T] = apply[T](ByteBuffer.wrap(array))

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
    val version = UBJInput.readByte
    val inputVersion = IOVersion(version)

    Log.debug(s"Unmarshal: Created input stream of version $version")

    new SiignaInputStream(bufferInput, inputVersion)
  }

}
