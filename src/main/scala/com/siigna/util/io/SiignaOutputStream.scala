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

import org.ubjson.io.UBJOutputStream
import java.io.OutputStream
import version.IOVersion

/**
 * An output stream capable of writing Siigna types to the underlying [[org.ubjson.io.UBJOutputStream]].
 * @param out  The OutputStream to write to.
 * @see [[http://ubjson.org]] Universal Binary JSON
 */
class SiignaOutputStream(out : OutputStream, version : IOVersion) extends UBJOutputStream(out) {

  /**
   * Writes a member in the UBJSON format by first writing the name and then the object.
   * @param name  The name of the member, fx "id"
   * @param obj  The name of the object, fx 12L
   */
  def writeMember(name : String, obj : Any) {
    writeString(name)
    writeObject(obj)
  }

  /**
   * Writes a given object to the underlying output stream.
   * @param obj  The object to write
   * @throws IllegalArgumentException  If the object could not be recognized.
   */
  def writeObject(obj : Any) {
    obj match {

      // Simple types
      case null => writeNull()
      case b : Boolean => writeBoolean(b)
      case b : Byte => writeByte(b)
      case c : Char => writeByte(c.asInstanceOf[Byte])
      case s : Short => writeInt16(s)
      case i : Int => writeInt32(i)
      case l : Long => writeInt64(l)
      case d : Double => writeDouble(d)
      case f : Float => writeFloat(f)
      case b : BigDecimal => writeHuge(b.bigDecimal)
      case b : BigInt => writeHuge(b.bigInteger)
      case s : String => writeString(s)

      // Not a primitive
      case _ => version.writeSiignaObject(this, obj)
    }
  }

}
