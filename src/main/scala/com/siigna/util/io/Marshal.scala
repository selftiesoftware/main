/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.util.io

import org.ubjson.io.ByteArrayOutputStream
import version.IOVersion
import com.siigna.util.Log

/**
 * Marshals known objects to arrays of bytes according to the [http://ubjson.org UBJSON] (Universal Binary JSON)
 * standard. See [[com.siigna.util.io.ObjectType]] for a reference on known types.
 *
 * <h2>Examples</h2>
 * {{{
 *   // Writing and reading a double
 *   val marshaledDouble = Marshal(42d)
 *   val double = Unmarshal[Double](marshaledDouble) // Some(42)
 *
 *   // Writing and reading a shape
 *   val marshalledLineShape = Marshal(LineShape(0, 0, 1, 1)
 *   val object = Unmarshal[LineShape](marshaledLineShape) // Some(LineShape(0, 0, 1, 1)
 *
 *   // Writing and reading a collection
 *   val marshalledSeq = Marshal(Seq(1, 1, 2, 3, 5, 7))
 *   val object = Unmarshal[Seq[Int]](marshaledArray) // Some(Seq(1, 1, 2, 3, 5, 7))
 *
 *   // Similar with maps
 *   val marshalledMap = Map(1 -> 1, 2 -> 3, 5 -> 7)
 *   val object = Unmarshal[Map[Int, Int]](marshaledMap) // Some(Map(1 -> 1, 2 -> 3, 5 -> 7))
 *
 * }}}
 *
 * @author csp <csp@siigna.com>
 * @author jegp <jegp@siigna.com>
 * @see [[https://github.com/thebuzzmedia/universal-binary-json-java/ UBJSON on GitHub]]
 */
object Marshal {

  /**
   * Marshals a given object. See [[com.siigna.util.io.SiignaOutputStream]] for compatible objects.
   * @param any  The object to marshal.
   * @return  A ByteBuffer containing the marshaled command, useful for being send over network or to a file..
   * @throws IllegalArgumentException  If the object could not be recognized.
   */
  def apply(any : Any) : Array[Byte] = marshal(_.writeObject(any))

  /**
   * Prepares the right output stream, so the given function f can be executed, and wraps the output stream up
   * and turn it into a ByteBuffer
   * @param f  The function to execute of the SiignaOutputStream
   * @return  A byte array containing the objects written to it.
   */
  protected def marshal(f : (SiignaOutputStream) => Unit) : Array[Byte] = {
    val bytes = new ByteArrayOutputStream(64)
    val out = new SiignaOutputStream(bytes, IOVersion(IOVersion.Current))
    val version = IOVersion.Current

    Log.debug(s"Unmarshal: Marshalling byte stream with version $version")

    // Write the version of the content
    out.writeString("version")
    out.writeByte(version)

    // Write the object itself
    f(out)

    Log.debug("Unmarshal: Successfully wrote object " + f)

    // Flush and close
    out.flush()
    out.close()

    // Return the buffer itself
    bytes.getArray
  }

}
