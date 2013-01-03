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

package com.siigna.app.model.action.serialization

import com.siigna.app.model.shape.ShapeSelector
import com.siigna.util.geom.TransformationMatrix
import java.io.{ObjectInput, ObjectOutput, Externalizable}
import com.siigna.app.model.action.TransformShapeParts

/**
 * A proxy class used to serialize and de-serialize instances of TransformShapeParts. This class is used
 * to instantiate instances of the TransformShapeParts because it is immutable - we cannot (and should not)
 * change its values during run-time. Instead we create and return new instances.
 * This is also called a <i>serialization proxy pattern</i>.
 * @see Effective Java 2nd Edition, item 78.
 * @param shapes  The shapes and their parts to transform.
 * @param transformation  The transformation to apply.
 */
@SerialVersionUID(-540105375)
sealed protected[action] class TransformShapePartsProxy(shapes : Map[Int, ShapeSelector],
                                                        transformation : TransformationMatrix)
  extends SerializationProxy(() => new TransformShapeParts(TSPPValues.shapes, TSPPValues.transformation)) {

  /**
   * A public, empty constructor required by the Externalizable trait.
   * @return  An empty TransformShapePartsProxy
   */
  def this() = this(Map(), TransformationMatrix())

  def writeExternal(out: ObjectOutput) {
    out.writeInt(shapes.size)
    shapes.foreach{ case (i : Int, s : ShapeSelector) => {
      out.writeInt(i)
      out.writeObject(s)
    }}
    out.writeObject(transformation)
  }

  def readExternal(in: ObjectInput) {
    val size = in.readInt()
    TSPPValues.shapes = new Array[(Int, ShapeSelector)](size).map(_ => {
      in.readInt() -> in.readObject().asInstanceOf[ShapeSelector]
    }).toMap
    TSPPValues.transformation = in.readObject().asInstanceOf[TransformationMatrix]
  }
}

// An object to store persistance values
private[serialization] object TSPPValues {

  var shapes : Map[Int, ShapeSelector] = Map()
  var transformation : TransformationMatrix = TransformationMatrix()

}