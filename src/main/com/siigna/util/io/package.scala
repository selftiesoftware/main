package com.siigna.util

/**
 * The persistence package is capable of converting objects into byte arrays (marshalling), reading objects from
 * byte arrays (unmarshalling) and storing and reading content from disc.
 *
 * <h2>Marshalling and unmarshalling</h2>
 * <p>
 *   - is very simple to achieve. We have designed the library to be as independent as possible. No classes needs to
 *   inherit any interface or implement methods. This also gives us the power to version the (un)marshalling, which
 *   is done beneath the [[com.siigna.util.io.version]] package.
 * </p>
 * <p>
 *   It is currently only possible to marshal and unmarshal primitives and selected Java and Scala classes. Both
 *   Map and Traversable are one of these examples, so if you lack any implementation it is always possible to throw the
 *   object data into a collection. A complete overview of which types are supported can be found in the
 *   [[com.siigna.util.io.Type]] object, which reference the currently used data constants for object identification.
 * </p>
 * <h3>Examples on (un)marshalling</h3>
 * <p>
 *   To marshal objects you simply call the [[com.siigna.util.io.Marshal]] object with the data you would like to
 *   marshal like so:
 *   {{{
 *     import com.siigna.util.io.Marshal
 *     Marshal(123456789L)         // Array[Byte]
 *     Marshal("Hej Verden!")      // Array[Byte]
 *     Marshal(Seq(13, 142, 1392)) // Array[Byte]
 *   }}}
 *   The above mentioned examples will produce an array of bytes which can be used to send over network, store to
 *   a file etc.
 * </p>
 * <p>
 *   To unmarshal objects you simply call the [[com.siigna.util.io.Unmarshal]] object with the type of the
 *   object you expect to get back and the byte-array/byte buffer containing the marshalled data:
 *   {{{
 *     import com.siigna.util.io.Unmarshal
 *     Unmarshal[Long](byteArray)     // Some(123456789L)
 *     Unmarshal[String](byteArray)   // Some("Hej Verden!")
 *     Unmarshal[Seq[Int]](byteArray) // Some(Seq(13, 142, 1392))
 *   }}}
 *   The above mentioned examples will produce an Option[T] where T is the requested type. If any errors occurs the
 *   returned data will be None and a description of the error will be written to the [[com.siigna.util.Log]].
 * </p>
 * <p>
 *   It is important to note that for the time being it is not possible to retrieve native Arrays from the
 *   [[com.siigna.util.io.Unmarshal]] object. Instead, retrieve the data as a type like Traversable and use
 *   <code>.toArray</code> to cast it. The reason is that native java arrays cannot be casted at runtime, which
 *   makes is slighty difficult to cast the final objects. We will get around to doing this later, but if you have
 *   a specific need you are welcome to contact us at [[http://siigna.com/development]].
 * </p>
 *
 * <h2>Storing contents to disc</h2>
 *
 */
package object io extends Enumeration {

  /**
   * A Type object whose values we use to write to the byte-stream so we can identify the type of the marshalled
   * object when we unmarshal it.
   */
  object Type {
    // Remote package
    val Error   = 0.toByte
    val Get     = 1.toByte
    val Set     = 2.toByte
    val Session = 3.toByte
    val User    = 4.toByte

    // Util
    val Attributes = 50.toByte
    val TransformationMatrix = 51.toByte
    val Vector2D   = 52.toByte
    val Model      = 53.toByte

    // Scala
    val Traversable = 80.toByte
    val Map         = 81.toByte
    val Range       = 82.toByte

    // Java
    val Color    = 90.toByte

    // Actions
    val AddAttributes       = 100.toByte
    val SetAttributes       = 101.toByte
    val CreateShape         = 102.toByte
    val CreateShapes        = 103.toByte
    val DeleteShape         = 104.toByte
    val DeleteShapes        = 105.toByte
    val DeleteShapePart     = 106.toByte
    val DeleteShapeParts    = 107.toByte
    val RemoteAction        = 108.toByte
    val SequenceAction      = 109.toByte
    val TransformShape      = 110.toByte
    val TransformShapeParts = 111.toByte
    val TransformShapes     = 112.toByte

    // Shapes
    val ArcShape      = 200.toByte
    val CircleShape   = 201.toByte
    val GroupShape    = 202.toByte
    val ImageShape    = 203.toByte
    val LineShape     = 204.toByte
    val PolylineShapeClosed   = 205.toByte
    val PolylineShapeOpen     = 206.toByte
    val RectangleShapeSimple  = 207.toByte
    val RectangleShapeComplex = 208.toByte
    val TextShape     = 209.toByte

    // Inner polyline shapes
    val PolylineLineShape = 220.toByte
    val PolylineArcShape  = 221.toByte

    // Shape parts
    val ArcShapePart       = 230.toByte
    val CircleShapePart    = 231.toByte
    val GroupShapePart     = 232.toByte
    val ImageShapePart     = 233.toByte
    val LineShapePart      = 234.toByte
    val PolylineShapePart  = 235.toByte
    val RectangleShapePart = 236.toByte
    val TextShapePart      = 237.toByte
  }

}
