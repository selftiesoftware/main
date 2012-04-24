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

import com.siigna.app.model.{DynamicShape, Model}
import com.siigna.app.model.shape.{DynamicShape, Shape, ShapeLike}
import com.siigna.util.logging.Log

object Delete {
  /*
  def apply(shape : ShapeLike) { shape match {
    case s : Shape => {
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

  def apply(shape1 : Shape, shape2 : Shape, shapes : Shape*) {
    apply(Iterable(shape1, shape2) ++ shapes)
  }

  def apply(shapes : Traversable[ShapeLike]) {
    var shapesToDelete = Map[String, Shape]()
    shapes.foreach{ s => s match {
        case DynamicShape(id, shape) => {
          if (!shapesToDelete.contains(id)) {
            shapesToDelete = shapesToDelete + (id -> shape)
          }
        }
        case s : Shape => {
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
case class DeleteShape(id : String, shape : Shape) extends Action {

  def execute(model : Model) = model - id

  def merge(that : Action) = that match {
    case DeleteShape(i : String, s : Shape) =>
      if (i == id) this
      else DeleteShapes(Map((id -> shape), (i -> s)))
    case _ => SequenceAction(this, that)
  }

  def undo(model : Model) = model + (id -> shape)

}

/**
 * Deletes a number of shapes.
 */
case class DeleteShapes(shapes : Map[String, Shape]) extends Action {

  def execute(model : Model) = model -- shapes.keys

  def merge(that : Action) = that match {
    case CreateShape(i : String, s : Shape) => DeleteShapes(shapes + (i -> s))
    case CreateShapes(s : Map[String, Shape]) => DeleteShapes(shapes ++ s)
    case _ => SequenceAction(this, that)
  }

  def undo(model : Model) = model ++ shapes
         */
}