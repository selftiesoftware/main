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

package com.siigna.app.model

import action.Action
import java.io._

import com.siigna.util.logging.Log
import com.siigna.util.collection.Attributes
import reflect.Type
import scala.reflect
import shape.Shape

/**
 * <p>A RemoteModel with the responsibilities of marshalling and unmarshalling a model.</p>
 */
@SerialVersionUID(2113124332)
class RemoteModel(var model : Model, var attributes : Attributes) extends HasAttributes
                     with Externalizable {

  type T = RemoteModel

  def readExternal(in : ObjectInput) {
    var fail = false
    try {
      model = in.readObject().asInstanceOf[Model]
    } catch {
      case e => Log.error("Model: Failed to read shapes from data.", e); fail = true
    }
    
    try {
      attributes = in.readObject().asInstanceOf[Attributes]
    } catch {
      case e => Log.error("Model: Failed to read attribtues from data.", e); fail = true
    }

    if (!fail) Log.success("Model: Sucessfully read data.")
  }

  def setAttributes(attributes : Attributes) = {
    this.attributes = attributes
    this
  }

  override def toString: String = {
    "shapes: "+model.shapes+"\n attributes:"+attributes
  }

  def writeExternal(out : ObjectOutput) {
    out.writeInt(model.shapes.size)
    for (t <- model.shapes) {
      out.writeInt(t._1)
      out.writeObject(t._2)
    }
    out.writeObject(attributes)
  }
  
}
