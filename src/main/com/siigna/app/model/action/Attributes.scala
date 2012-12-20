package com.siigna.app.model.action

import com.siigna.util.collection.Attributes
import com.siigna.util.SerializableProxy
import serialization.{AddAttributesProxy, SerializationProxy, SetAttributesProxy}
import com.siigna.app.model.{Drawing, Model}

/**
 * Adds the given attributes to the shapes, replacing the ones with the same key, but leaving the ones already
 * existing.
 * @param shapes  The shapes to give the attribute
 * @param attributes  The attributes to add
 */
@SerialVersionUID(1306561642)
case class AddAttributes(shapes : Map[Int, Attributes], attributes : Attributes)
  extends SerializableProxy(() => new AddAttributesProxy(shapes, attributes)) with Action {

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
case class SetAttributes(shapes : Map[Int, Attributes], attributes : Attributes)
  extends SerializableProxy(() => new SetAttributesProxy(shapes, attributes)) with Action {

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
