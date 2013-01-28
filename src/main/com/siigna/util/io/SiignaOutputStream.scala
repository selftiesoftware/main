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
