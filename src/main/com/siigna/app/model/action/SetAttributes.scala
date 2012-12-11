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
import serialization.SetAttributesProxy
import com.siigna.util.SerializableProxy

/**
 * Sets a number of attributes on a number of shapes to the values specified in the parameter <code>attributes</code>.
 * @param shapes  The ids of the shapes to set the attribute on, mapped to the corresponding old attribute values.
 * @param attributes  The attributes to set.
 */
@SerialVersionUID(-1003577121)
case class SetAttributes(shapes : Map[Int, Attributes], attributes : Attributes)
  extends SerializableProxy(() => new SetAttributesProxy(shapes, attributes)) with Action {

  def execute(model: Model) = model.add(ids.map(i => i -> model.shapes(i).setAttributes(attributes)).toMap)

  def ids = shapes.keys

  //def merge(that: Action) = throw new UnsupportedOperationException("Not yet implemented.")

  def undo(model: Model) = model.add(shapes.map(i => {
    i._1 -> model.shapes(i._1).setAttributes(i._2)
  }))

  def update(map: Map[Int, Int]) = copy(shapes.map(t => map.getOrElse(t._1, t._1) -> t._2))

}

/**
 * Companion object to SetAttributes.
 */
object SetAttributes {

  /**
   * Creates an instance of SetAttributes which sets one single attribute to the given value.
   * This method finds the default values for the old values for the attributes, defined in the parameter
   * <code>shapes</code> from the current Drawing.
   * @param ids  The ids of the shapes to set the attribute on
   * @param name  The name of the attribute to set
   * @param value  The value of the attribute to set
   * @return  An instance of SetAttributes
   */
  def apply(ids : Traversable[Int], name : String, value : Any) : SetAttributes =
    SetAttributes(ids.map(i => i -> Drawing(i).attributes).toMap, Attributes(name -> value))

  /**
     * Creates an instance of SetAttributes which sets several attributes of the shapes with the given ids.
     * This method finds the default values for the old values for the attributes, defined in the parameter
     * <code>shapes</code> from the current Drawing.
     * @param ids  The ids of the shapes to set the attribute on.
     * @param attributes  The attributes to set.
     * @return  An instance of SetAttributes.
     */
  def apply(ids : Traversable[Int], attributes : Attributes) : SetAttributes =
    SetAttributes(ids.map(i => i -> Drawing(i).attributes).toMap, attributes)

}
