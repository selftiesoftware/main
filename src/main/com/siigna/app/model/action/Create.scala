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
import com.siigna.util.logging.Log
import com.siigna.app.model.shape.{CollectionShape, ImmutableShape, Shape}

/**
* An object that allows you to create one or multiple shapes.
*/
object Create {
  
  private var id = 0;
  
  private def getId = { id += 1; id; }
  
  def apply(shape : ImmutableShape) { 
    val id = shape.attributes.int("id").getOrElse(getId)
    apply(id, shape)
  }
  
  def apply(id : Int, shape : ImmutableShape) {
    Model execute CreateShape(id, shape)
  }

  def apply[T <: ImmutableShape](collection : CollectionShape[T]) {
    val id = collection.attributes.int("id").getOrElse(getId)
    apply(id, collection)
  }

  def apply(shape1 : ImmutableShape, shape2 : ImmutableShape, shapes : ImmutableShape*) {
    apply(Traversable(shape1, shape2) ++ shapes)
  }

  def apply(shapes : Traversable[ImmutableShape]) {
    if (shapes.size > 1) {
      val map = shapes.map(s => {
        val id = s.attributes.int("id").getOrElse(getId)
        (id -> s)
      }).toMap
      Model execute CreateShapes(map)
    } else if (shapes.size == 1) {
      apply(shapes.head)
    } else {
      Log.info("Create: Create was called with an empty Traversable.")
    }
  }

  //DENNE DEF ER LAVET AF NIELS - VIRKER MED FLERE SHAPES, MEN VED IKKE HVAD DER SKER MED TOM MAP ELLER MAP MED EN SHAPE
  def apply(shapes : Map[Int,ImmutableShape]) {
    Model execute CreateShapes(shapes)
  }

}

/**
 * Creates a shape with an associated ID.
 */
case class CreateShape(id : Int, shape : ImmutableShape) extends Action {

  def execute(model : Model) = model add (id, shape)

  def merge(that : Action) = that match {
    case CreateShape(i : Int, s : ImmutableShape) =>
      if (s == shape) this
      else CreateShapes(Map(id -> shape, i -> s))
    case _ => SequenceAction(this, that)
  }

  def undo(model : Model) = model remove (id)

}

/**
 * Creates several shapes.
 * This action cannot be instantiated directly, but needs to be called through the <code>Create</code> object.
 */
case class CreateShapes(shapes : Map[Int, ImmutableShape]) extends Action {

  require(shapes.size > 1, "Cannot initialize CreateShapes with zero shapes.")

  def execute(model : Model) = model add shapes

  def merge(that : Action) = that match {
    case CreateShape(i : Int, s : ImmutableShape) => CreateShapes(shapes + (i -> s))
    case CreateShapes(s : Map[Int, ImmutableShape]) => CreateShapes(shapes ++ s)
    case _ => SequenceAction(this, that)
  }

  def undo(model : Model) = model remove shapes.keys
}