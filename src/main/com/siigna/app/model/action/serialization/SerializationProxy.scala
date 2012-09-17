/*
 * Copyright (c) 2012. Siigna is released under the creative common license by-nc-sa. You are free 
 * to Share — to copy, distribute and transmit the work, 
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */
package com.siigna.app.model.action.serialization

import java.io.{ObjectInput, ObjectOutput, Externalizable}
import com.siigna.app.model.action.Action

/**
 * A proxy class used to serialize and de-serialize instances of Actions that contains
 * parts that cannot be serialized or deserialized. This class is implemented as a mutable proxy to
 * an immutable action. Actions cannot (and should not) change its values during run-time, so
 * instead we create and return new instances of the action.
 * This is also called a <i>serialization proxy pattern</i>.
 * @see Effective Java 2nd Edition, item 78.
 * @param f  A function that creates a new Action
 */
protected[action] abstract class SerializationProxy(f : () => Action) extends Externalizable {

  /**
   * Writes the TransformShapePartsProxy to the given ObjectOutput.
   * @param out  The ObjectOutput to write the content of the class to.
   */
  def writeExternal(out: ObjectOutput)

  /**
   * Reads the content of the ObjectInput and attempts to parse it as a SerializationProxy.
   * After the object has been read the <code>readResolve</code> method is called and an instance
   * of a TransformShapeParts is returned instead.
   * @see The description for [[com.siigna.app.model.action.serialization.SerializationProxy]]
   * @param in  The ObjectInput to read from
   */
  def readExternal(in: ObjectInput) 
  
  /**
   * When de-serializing we read the proxy as if it was an instance of the action wrapped by the proxy.
   * @return  A new instance of an Action.
   */
  def readResolve() : Object = f()

}
