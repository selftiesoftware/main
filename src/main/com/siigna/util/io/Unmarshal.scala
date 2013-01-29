package com.siigna.util.io

import java.nio.ByteBuffer
import org.ubjson.io.{UBJInputStream, ByteBufferInputStream}
import version.IOVersion
import com.siigna.util.Log

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
object Unmarshal  extends App {


  import com.siigna.app.model.shape.LineShape

  val obj = "Hej"
  val out = Marshal(obj)
  val in  = Unmarshal[Long](out)
  println("In: " + in)

  /**
   * <p>
   *   Attempts to read a well-known Siigna object from the given [[java.nio.ByteBuffer]] and cast it to the
   *   given type T.
   *   <br>
   *   <b>Important: Due to erasure we cannot ensure that the expected type will be the returned type.</b>
   * </p>
   * @param buffer  The ByteBuffer containing the data to be read.
   * @tparam T  The expected type of the object to be read.
   * @return  Some[T] if the object could be read and successfully parsed, None otherwise
   * @throws UBJFormatException  If the formatting could not be understood.
   * @throws IOException  If an I/O error occurred.
   */
  def apply[T](buffer : ByteBuffer) : Option[T] = {
    val in = getInputStream(buffer)

    // Read the "main" object
    try {
      Some(in.readObject.asInstanceOf[T])
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
  def apply[T](array : Array[Byte]) : Option[T] = apply[T](ByteBuffer.wrap(array))

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
