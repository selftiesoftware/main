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

import java.io.{ObjectInput, ObjectOutput}
import com.siigna.app.model.shape.Shape
import com.siigna.app.model.action.CreateShapes

/**
 * A proxy class used to serialize and de-serialize instances of CreateShapes.
 *
 * @see Effective Java 2nd Edition, item 78.
 * @param shapes  The ids and the shapes that are associated with the ids.
 */
@SerialVersionUID(572887823)
sealed protected[action] class CreateShapesProxy(shapes : Map[Int, Shape])
  extends SerializationProxy(() => new CreateShapes(CSPValues.shapes)) {

  /**
   * A public, empty constructor required by the Externalizable trait.
   * @return  An empty CreateShapesProxy
   */
  def this() = this(Map())

  def writeExternal(out: ObjectOutput) {
    out.writeInt(shapes.size)
    shapes.foreach{ case (i : Int, s : Shape) => {
      out.writeInt(i)
      out.writeObject(s)
    }}
  }

  def readExternal(in: ObjectInput) {
    val size = in.readInt()
    CSPValues.shapes = new Array[(Int, Shape)](size).map(_ => {
      in.readInt() -> in.readObject().asInstanceOf[Shape]
    }).toMap
  }
}

// An object to store persistant values
private[serialization] object CSPValues {
  var shapes = Map[Int, Shape]()
}