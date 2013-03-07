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

package com.siigna.app.model.action

import com.siigna.util.collection.Attributes
import com.siigna.app.model.{Drawing, Model}

/**
 * Adds the given attributes to the shapes, replacing the ones with the same key, but leaving the ones already
 * existing.
 * @param shapes  The shapes to give the attribute
 * @param attributes  The attributes to add
 */
case class AddAttributes(shapes : Map[Int, Attributes], attributes : Attributes) extends Action {

  def execute(model: Model) = model.add(ids.map(i => i -> model.shapes(i).addAttributes(attributes)).toMap)

  def ids = shapes.keys

  def undo(model: Model) = model.add(shapes.map(i => {
    i._1 -> model.shapes(i._1).addAttributes(shapes(i._1))
  }))

  def update(map: Map[Int, Int]) = copy(shapes.map(t => map.getOrElse(t._1, t._1) -> t._2))

}

/**
 * A companion object to the AddAttributes class.
 */
object AddAttributes {

  /**
   * Creates an instance of AddAttributes which adds one single attribute to the given value.
   * This method finds the default values for the old values for the attributes, defined in the parameter
   * <code>shapes</code> from the current Drawing.
   * @param ids  The ids of the shapes to set the attribute on
   * @param name  The name of the attribute to set
   * @param value  The value of the attribute to set
   * @return  An instance of AddAttributes
   */
  def apply(ids : Traversable[Int], name : String, value : Any) : AddAttributes =
    new AddAttributes(ids.map(i => i -> Drawing(i).attributes).toMap, Attributes(name -> value))

  /**
   * Creates an instance of AddAttributes which adds several attributes of the shapes with the given ids.
   * This method finds the default values for the old values for the attributes, defined in the parameter
   * <code>shapes</code> from the current Drawing.
   * @param ids  The ids of the shapes to set the attribute on.
   * @param attributes  The attributes to set.
   * @return  An instance of AddAttributes.
   */
  def apply(ids : Traversable[Int], attributes : Attributes) : AddAttributes =
    new AddAttributes(ids.map(i => i -> Drawing(i).attributes).toMap, attributes)

}

/**
 * Sets a the attributes on a number of shapes to attributes given in the parameter <code>attributes</code>. This
 * also erases any other attributes set on the shape(s).
 * @param shapes  The ids of the shapes to set the attribute on, mapped to the corresponding old attribute values.
 * @param attributes  The attributes to set.
 */
@SerialVersionUID(-1003577121)
case class SetAttributes(shapes : Map[Int, Attributes], attributes : Attributes) extends Action {

  def execute(model: Model) = model.add(ids.map(i => i -> model.shapes(i).setAttributes(attributes)).toMap)

  def ids = shapes.keys

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
    new SetAttributes(ids.map(i => i -> Drawing(i).attributes).toMap, Attributes(name -> value))

  /**
   * Creates an instance of SetAttributes which sets several attributes of the shapes with the given ids.
   * This method finds the default values for the old values for the attributes, defined in the parameter
   * <code>shapes</code> from the current Drawing.
   * @param ids  The ids of the shapes to set the attribute on.
   * @param attributes  The attributes to set.
   * @return  An instance of SetAttributes.
   */
  def apply(ids : Traversable[Int], attributes : Attributes) : SetAttributes =
    new SetAttributes(ids.map(i => i -> Drawing(i).attributes).toMap, attributes)

}
