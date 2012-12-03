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

import com.siigna.app.model.shape.Shape
import com.siigna.app.model.action.DeleteShapeParts
import java.io.{ObjectInput, ObjectOutput}
import com.siigna.util.SerializationProxy

/**
 * A proxy class used to serialize and de-serialize instances of DeleteShapes.
 *
 * @see Effective Java 2nd Edition, item 78.
 * @param oldShapes  The old shapes that existed before parts of them were removed
 * @param newShapes  The shapes that emerges after the parts have been removed from the old shapes
 */
@SerialVersionUID(1130567644)
sealed protected[action] class DeleteShapePartsProxy(oldShapes : Map[Int, Shape], newShapes : Map[Int, Shape])
  extends SerializationProxy(() => DeleteShapeParts(DSPP.oldShapes, DSPP.newShapes)) {

  /**
     * A public, empty constructor required by the Externalizable trait.
     * @return  An empty DeleteShapePartsProxy
     */
    def this() = this(Map(), Map())
  
    def writeExternal(out: ObjectOutput) {
      def writeMap(map : Map[Int, Shape]) {
        out.writeInt(map.size)
        map.foreach{ case (i : Int, s : Shape) => {
          out.writeInt(i)
          out.writeObject(s)
        }}
      }
      writeMap(oldShapes)
      writeMap(newShapes)
    }
  
    def readExternal(in: ObjectInput) {
      def readMap = {
        val size = in.readInt()
        new Array[(Int, Shape)](size).map(_ => {
          in.readInt() -> in.readObject().asInstanceOf[Shape]
        }).toMap
      }
      DSPP.oldShapes = readMap
      DSPP.newShapes = readMap
    }
  
}

// An object for persistant storage
private[serialization] object DSPP {
  var oldShapes : Map[Int, Shape] = Map()
  var newShapes : Map[Int, Shape] = Map()
}
