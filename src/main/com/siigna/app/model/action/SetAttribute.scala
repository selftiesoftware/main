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

package com.siigna.app.model.action

import com.siigna.app.model.Model

/**
 * Creates actions to set a single attribute on any number of shapes.
 */
object SetAttribute {
  
  def apply (id : Int, name : String, value : Any) {
    Model execute new SetSingleAttribute(Traversable(id), name, value)
  }
  
  def apply(ids : Traversable[Int], name : String, value : Any) { 
    Model execute new SetSingleAttribute(ids, name, value)
  }
  
}

/* TODO: Create a SetAttributes
object SetAttributes {
  
  def apply(id : Int, attributes : Attributes) {
    Model execute new SetAttributes(Traversable(id), attributes)
  }
  
  def apply(id : Int, attributes : Map[String, Any]) {
    Model execute new SetAttributes(Traversable(id), Attributes(attributes))
  }
  
  def apply(ids : Traversable[Int], attributes : Attributes) {
    Model execute new SetAttributes(ids, attributes)
  }

  def apply(ids : Traversable[Int], attributes : Map[String, Any]) {
    Model execute new SetAttributes(ids, Attributes(attributes))
  }
  
} */

/**
 * Sets a single attribute on a number of shapes to the given value.
 * @param ids  The ids of the shapes to set the attribute on.'
 * @param name  The name of the attribute
 * @param value  The value of the attribute
 */
@SerialVersionUID(-955468992)
case class SetSingleAttribute(ids : Traversable[Int], name : String, value : Any) extends Action {
  
  val oldValues : Map[Int, Option[Any]] = ids.map(i => i -> Model(i).attributes.get(name)).toMap
  
  def execute(model: Model) = model.add(ids.map(i => i -> Model(i).setAttribute(name, value)).toMap)

  //def merge(that: Action) = throw new UnsupportedOperationException("Not yet implemented.")

  def undo(model: Model) = model.add(oldValues.map(i => {
    i._1 -> (if (i._2.isDefined) {
      Model(i._1).setAttribute(name, i._2)
    } else {
      Model(i._1).removeAttribute(name)
    })
  }))
  
}
