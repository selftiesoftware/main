package com.siigna.util.io.version

import com.siigna.util.io.{SiignaOutputStream, SiignaInputStream}

/**
 * A specific version of the IO used to write and read bytes as it was done in one particular version.
 * Please refer to the companion object [[com.siigna.util.io.version.IOVersion]] for retrieving the IOVersion
 * implementation for a given version.
 */
trait IOVersion {

  /**
   * Attempts to read a siigna object from the given input stream. We return the object coupled with its type
   * information to ensure type-safe casting.
   * @param in  The input stream to read from.
   * @param members  The amount of members in the expected object.
   * @return  The type of the object that has been read and the object itself, from the underlying input stream.
   * @throws  UBJFormatException  If the format could not be matched.
   */
  def readSiignaObject(in : SiignaInputStream, members : Int) : (reflect.runtime.universe.Type, Any)

  /**
   * Write an object from the Siigna domain to the output stream.
   * @param out The output stream to write to.
   * @param obj  The object to write.
   * @throws  IllegalArgumentException  If the object could not be recognized
   */
  def writeSiignaObject(out : SiignaOutputStream, obj : Any)

}

/**
 * Used to retrieve an instance of a [[com.siigna.util.io.version.IOVersion]] given a version number like so:
 * {{{
 *   val versionNumber = 1
 *   val versionIO     = IOVersion(1)
 * }}}
 *
 * Version numbers can be retrieved like so:
 * {{{
 *   IOVersion.One     // Version One
 *   IOVersion.Current //
 * }}}
 */
object IOVersion {

  // Version one
  val One   = 1.asInstanceOf[Byte]
//val Two   = 2.asInstanceOf[Byte]
//val Three = 3.asInstanceOf[Byte]
//And so on...

  // The current working version
  val Current = One

  /**
   * Attempts to retrieve the I/O implementation for the given version number.
   * @param version  The version to retrieve.
   */
  def apply(version : Int) : IOVersion = version match {
    case 1 => IOVersion1
    case _ => throw new IllegalArgumentException("IOVersion: Could not find an implementation for version: " + version)
  }

}
