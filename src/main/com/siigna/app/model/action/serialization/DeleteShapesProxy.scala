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

import com.siigna.app.model.shape.Shape
import com.siigna.app.model.action.DeleteShapes
import java.io.{ObjectInput, ObjectOutput}

/**
 * A proxy class used to serialize and de-serialize instances of DeleteShapes.
 *
 * @see Effective Java 2nd Edition, item 78.
 * @param shapes  The ids and the shapes that are associated with the ids.
 */
@SerialVersionUID(-1262265471)
sealed protected[action] class DeleteShapesProxy(shapes : Map[Int, Shape])
  extends SerializationProxy(() => DeleteShapes(DSP.shapes)) {

  /**
     * A public, empty constructor required by the Externalizable trait.
     * @return  An empty DeleteShapesProxy
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
      DSP.shapes = new Array[(Int, Shape)](size).map(_ => {
        in.readInt() -> in.readObject().asInstanceOf[Shape]
      }).toMap
    }

}

// An object for persistant storage
private[serialization] object DSP {
  var shapes : Map[Int, Shape] = Map()
}
