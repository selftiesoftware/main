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

import com.siigna.app.model.shape.{ImmutableShape, Shape}
import com.siigna.app.model.Model

/**
* An object that allows you to create one or multiple shapes.
*/
object Create {

  def apply(shape : Shape) { shape match {
    case s : ImmutableShape => Model(CreateShape(s))
    case _ => Model
  } }

  def apply(shape1 : Shape, shape2 : Shape, shapes : Shape*) {
    apply(Iterable(shape1, shape2) ++ shapes)
  }

  def apply(shapes : Traversable[Shape]) {
    val immutableShapes = shapes.filter(_.isInstanceOf[ImmutableShape]).asInstanceOf[Traversable[ImmutableShape]]
    
    if (immutableShapes.size == 1)
      Model(CreateShape(uuid, immutableShapes.head))
    else
      Model(CreateShapes(immutableShapes.map(s => (uuid, s)).toMap))
  }

}

/**
 * Creates a shape with an associated ID.
 */
case class CreateShape(shape : ImmutableShape) extends Action {

  def execute(model : Model) = {
    new Model(model.shapes :+ shape, model.dynamics)
  }

  def merge(that : Action) = that match {
    case CreateShape(s : ImmutableShape) =>
      if (s == shape) this
      else CreateShapes(Seq(shape, s))
    case _ => SequenceAction(this, that)
  }

  def undo(model : Model) = new Model(model.shapes.filterNot(_ == shape), model.dynamics)

}

/**
 * Creates several shapes.
 * This action cannot be instantiated directly, but needs to be called through the <code>Create</code> object.
 */
case class CreateShapes(shapes : Map[String, ImmutableShape]) extends Action {

  def execute(model : Model) = model ++ shapes

  def merge(that : Action) = that match {
    case CreateShape(i : String, s : ImmutableShape) => CreateShapes(shapes + (i -> s))
    case CreateShapes(s : Map[String, ImmutableShape]) => CreateShapes(shapes ++ s)
    case _ => SequenceAction(this, that)
  }

  def undo(model : Model) = model -- shapes.keys

}