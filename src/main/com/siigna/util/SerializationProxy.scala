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
package com.siigna.util

import java.io._

/**
 * A proxy class used to serialize and de-serialize instances of a given type that contains
 * parts that cannot be serialized or de-serialized (typically immutable classes). This class is
 * implemented as a mutable proxy to an immutable classes which cannot (and should not) change
 * their values during run-time, so instead we create and return new instances of the class.
 * This is also called a <i>serialization proxy pattern</i>.
 * @see Effective Java 2nd Edition, item 78.
 * @param f  A function that creates a new instance of the proxy object
 */
abstract class SerializationProxy(f : () => Object) extends Externalizable {

  /**
   * Writes the TransformShapePartsProxy to the given ObjectOutput.
   * @param out  The ObjectOutput to write the content of the class to.
   */
  def writeExternal(out: ObjectOutput)

  /**
   * Reads the content of the ObjectInput and attempts to parse it as a SerializationProxy.
   * After the object has been read the <code>readResolve</code> method is called and an instance
   * of a type T is returned instead.
   * @see The description for [[com.siigna.util.SerializationProxy]]
   * @param in  The ObjectInput to read from
   */
  def readExternal(in: ObjectInput)
  
  /**
   * When de-serializing we read the proxy as if it was an instance of the action wrapped by the proxy.
   * @return  A new instance of type T, cast to an Object to follow the Java types.
   */
  final def readResolve() : Object = f()

}

/**
 * A Serializable instance of a immutable class implemented by proxy. The point is to avoid actual
 * serialization of the class since it is immutable. Instead we implement this class that serializes into
 * another object (hidden for the user) and de-serialized into the original object, so no one notices the
 * transition. This strategy is also called a <i>serialization proxy pattern</i>.
 *
 * @see Effective Java 2nd Edition, item 78.
 * @param f  A function that returns the proxy object.
 */
abstract class SerializableProxy(f : () => Object) extends Serializable {

  /**
   * Writes the SerializableProxy out as a SerializationProxy. This is the <i>serialization proxy pattern</i>
   * in effect.
   * @see Effective Java 2nd Edition, item 78.
   * @return  An object that can be used to marshal to (for instance) a stream.
   */
  final def writeReplace() : Object = f()

  /**
   * Avoids the possibility to fake a SerializableProxy de-serialization (like that's ever gonna happen).
   * @see Effective Java 2nd Edition, item 74 and 78.
   * @param in  The ObjectInputStream to not read the object from.
   */
  final def readObject(in: ObjectInputStream) {
    throw new InvalidObjectException("Serializable Proxy: Requires proxy object to read class.")
  }

}
