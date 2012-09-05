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
sealed protected[action] class TransformShapePartsProxy(private var shapes : Map[Int, ShapeSelector], private var transformation : TransformationMatrix) extends Externalizable {

  /**
   * A public, empty constructor required by the Externalizable trait.
   * @return  An empty TransformShapePartsProxy
   */
  def this() = this(Map(), TransformationMatrix())

  /**
   * Writes the TransformShapePartsProxy to the given ObjectOutput.
   * @param out  The ObjectOutput to write the content of the class to.
   */
  def writeExternal(out: ObjectOutput) {
    out.writeInt(shapes.size)
    shapes.foreach{ case (i : Int, s : ShapeSelector) => {
      out.writeInt(i)
      out.writeObject(s)
    }}
    out.writeObject(transformation)
  }

  /**
   * Reads the content of the ObjectInput and attempts to parse it as a TransformShapePartsProxy.
   * After the object has been read the <code>readResolve</code> method is called and an instance
   * of a TransformShapeParts is returned instead.
   * @see The description for [[com.siigna.app.model.action.serialization.TransformShapePartsProxy]]
   * @param in
   */
  def readExternal(in: ObjectInput) {
    val size = in.readInt()
    shapes = new Array[(Int, ShapeSelector)](size).map(_ => {
      in.readInt() -> in.readObject().asInstanceOf[ShapeSelector]
    }).toMap
    transformation = in.readObject().asInstanceOf[TransformationMatrix]
  }

  /**
   * When de-serializing we read the proxy as if it was a TransformShapeParts.
   * @return  A new instance of TransformShapeParts.
   */
  def readResolve() : Object = new TransformShapeParts(shapes, transformation)
}