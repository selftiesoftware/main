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

import com.siigna.util.collection.Attributes
import com.siigna.app.model.{Drawing, Model}

/**
 * Sets a single attribute on a number of shapes to the given value.
 * @param ids  The ids of the shapes to set the attribute on.
 * @param name  The name of the attribute
 * @param value  The value of the attribute
 */
@SerialVersionUID(-955468992)
case class SetAttribute(ids : Traversable[Int], name : String, value : Any) extends Action {
  
  val oldValues : Map[Int, Option[Any]] = ids.map(i => i -> Drawing(i).attributes.get(name)).toMap
  
  def execute(model : Model) = model.add(ids.map(i => i -> Drawing(i).setAttribute(name, value)).toMap)

  //def merge(that: Action) = throw new UnsupportedOperationException("Not yet implemented.")

  def undo(model: Model) = model.add(oldValues.map(i => {
    i._1 -> (if (i._2.isDefined) {
      Drawing(i._1).setAttribute(name, i._2)
    } else {
      Drawing(i._1).removeAttribute(name)
    })
  }))

  def update(map: Map[Int, Int]) = copy(ids.map(i => map.getOrElse(i, i)))
}

/**
 * Sets a number of attributes on a number of shapes to the given value.
 * @param ids  The ids of the shapes to set the attribute on.
 * @param attributes  The attributes to set.
 */
@SerialVersionUID(-1003577121)
case class SetAttributes(ids : Traversable[Int], attributes : Attributes) extends Action {

  val oldValues : Map[Int, Attributes] = ids.map(i => i -> Drawing(i).attributes).toMap

  def execute(model: Model) = model.add(ids.map(i => i -> Drawing(i).setAttributes(attributes)).toMap)

  //def merge(that: Action) = throw new UnsupportedOperationException("Not yet implemented.")

  def undo(model: Model) = model.add(oldValues.map(i => {
    i._1 -> Drawing(i._1).setAttributes(i._2)
  }))

  def update(map: Map[Int, Int]) = copy(ids.map(i => map.getOrElse(i, i)))

}
