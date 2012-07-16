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

import action.{VolatileAction, Action}
import com.siigna.app.controller.remote._
import com.siigna.app.Siigna
import shape.Shape
import com.siigna.util.logging.Log
import com.siigna.app.view.View
import java.io._
import com.siigna.util.collection.Attributes

/**
 * <p>A RemoteActionModel with the responsibilities to keep track of actions (and information regarding
 * the actions) relevant in the communications between the client and the server.</p>
 * <p>An example is the unique shape id necessary for each shape, which can not be served locally before the
 * server has approved the id. To solve this the action is only applied locally, but not sent remotely.</p>
 */
@SerialVersionUID(2113944332)
class RemoteModel extends ActionModel
                     with HasAttributes 
                     with Externalizable {

  def writeExternal(out : ObjectOutput) {
    out.writeObject(model.shapes)
    out.writeObject(attributes)
  }

  def readExternal(in : ObjectInput) {
    var fail = false
    try {
      val shapes = in.readObject()
      model = new Model(shapes.asInstanceOf[Map[Int, Shape]])
    } catch {
      case e => Log.error("Model: Failed to read shapes from data.", e); fail = true
    }
    
    try {
      val attr = in.readObject()
      attributes = attr.asInstanceOf[Attributes]
    } catch {
      case e => Log.error("Model: Failed to read attribtues from data.", e); fail = true
    }

    if (!fail) Log.success("Model: Sucessfully read data.")
  }
  
}
