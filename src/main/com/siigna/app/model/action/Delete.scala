/*
 * Copyright (c) 2011. Siigna is released under the creative common license by-nc-sa. You are free
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
import com.siigna.app.model.shape.{DynamicShape, ImmutableShape, Shape}
import com.siigna.util.logging.Log

object Delete {
  def apply(shape : Shape) { shape match {
    case s : ImmutableShape => {
      val id = Model.findId(_ == s)
      if (id.isDefined)
        Model(DeleteShape(id.get, s))
    }
    case s : DynamicShape => {
      Model.deselect(s)
      Model(DeleteShape(s.id, s.immutableShape))
    }
    case _ =>
  } }

  def apply(shape1 : ImmutableShape, shape2 : ImmutableShape, shapes : ImmutableShape*) {
    apply(Iterable(shape1, shape2) ++ shapes)
  }

  def apply(shapes : Traversable[Shape]) {
    var shapesToDelete = Map[String, ImmutableShape]()
    shapes.foreach{ s => s match {
        case DynamicShape(id, shape) => {
          if (!shapesToDelete.contains(id)) {
            shapesToDelete = shapesToDelete + (id -> shape)
          }
        }
        case s : ImmutableShape => {
          val id = Model.findId(_ == s)
          if (id.isDefined && !shapesToDelete.contains(id.get)) {
            shapesToDelete = shapesToDelete + (id.get -> s)
          }
        }
        case e => Log.warning("Delete: Received unknown element: "+e)
      }
    }

    // Tries the length and returns Option[Action] accordingly
    shapesToDelete.size match {
      case 0 =>
      case 1 => Model(DeleteShape(shapesToDelete.head._1, shapesToDelete.head._2))
      case _ => Model(DeleteShapes(shapesToDelete))
    }
  }
}

/**
 * Deletes a shape.
 */
case class DeleteShape(id : String, shape : ImmutableShape) extends Action {

  def execute(model : Model) = model - id

  def merge(that : Action) = that match {
    case DeleteShape(i : String, s : ImmutableShape) =>
      if (i == id) this
      else DeleteShapes(Map((id -> shape), (i -> s)))
    case _ => SequenceAction(this, that)
  }

  def undo(model : Model) = model + (id -> shape)

}

/**
 * Deletes a number of shapes.
 */
case class DeleteShapes(shapes : Map[String, ImmutableShape]) extends Action {

  def execute(model : Model) = model -- shapes.keys

  def merge(that : Action) = that match {
    case CreateShape(i : String, s : ImmutableShape) => DeleteShapes(shapes + (i -> s))
    case CreateShapes(s : Map[String, ImmutableShape]) => DeleteShapes(shapes ++ s)
    case _ => SequenceAction(this, that)
  }

  def undo(model : Model) = model ++ shapes

}