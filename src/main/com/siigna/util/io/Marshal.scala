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

import java.nio.ByteBuffer
import org.ubjson.io.ByteArrayOutputStream
import version.IOVersion

/**
 * Marshals objects to binary according to the [http://ubjson.org UBJSON] (Universal Binary JSON) standard.
 * This object uses [[java.nio.ByteBuffer]]s since they in many ways are faster than output streams.
 *
 * @author csp <csp@siigna.com>
 * @author jegp <jegp@siigna.com>
 * @see [[http://www3.ntu.edu.sg/home/ehchua/programming/java/J5b_IO_advanced.html Chua Hock Chuans notes on advanced I/O]]
 * @see [[https://github.com/thebuzzmedia/universal-binary-json-java/ UBJSON on GitHub]]
 */
object Marshal {

  /**
   * Marshals a given object. See [[com.siigna.util.io.SiignaOutputStream]] for compatible objects.
   * @param any  The object to marshal.
   * @return  A ByteBuffer containing the marshalled command, useful for being send over network or to a file..
   * @throws IllegalArgumentException  If the object could not be recognized.
   */
  def apply(any : Any) = marshal(_.writeObject(any))

  /**
   * Prepares the right output stream, so the given function f can be executed, and wraps the output stream up
   * and turn it into a ByteBuffer
   * @param f  The function to execute of the SiignaOutputStream
   * @return  A ByteBuffer with the contents written by the function f.
   */
  protected def marshal(f : (SiignaOutputStream) => Unit) : ByteBuffer = {
    //todo Optimize using ByteBuffer
    val bytes = new ByteArrayOutputStream()
    val out = new SiignaOutputStream(bytes, IOVersion(IOVersion.Current))

    // Write the version of the content
    out.writeString("version")
    out.writeByte(IOVersion.Current)

    // Write the object itself
    f(out)

    // Flush and close
    out.flush()
    out.close()

    // Return the buffer itself
    val arr = bytes.getArray
    ByteBuffer.wrap(arr)
  }

}
