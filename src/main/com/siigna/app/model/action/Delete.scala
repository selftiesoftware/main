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
import com.siigna.app.model.shape.{ShapeSelector, Shape}

object Delete {
  
  def apply(id : Int) {
    Model execute DeleteShape(id, Model(id))
  }
  
  def apply(id : Int, part : ShapeSelector) {
    Model execute DeleteShapePart(id, Model(id), part)
  }
  
}

/**
 * Deletes a shape.
 */
case class DeleteShape(id : Int, shape : Shape) extends Action {

  def execute(model : Model) = model remove id

  def merge(that : Action) = that match {
    case DeleteShape(i : Int, s : Shape) =>
      if (i == id) this
      else DeleteShapes(Map((id -> shape), (i -> s)))
    case _ => SequenceAction(this, that)
  }

  def undo(model : Model) = model.add(id, shape)

}

/**
 * Deletes a ShapeSelector.
 */
case class DeleteShapePart(id : Int, shape : Shape, part : ShapeSelector) extends Action {
  
  def execute(model : Model) = {
    val x = shape.delete(part);
    if (x.isDefined) {
      model.add(id, x.get)
    } else model
  }
  
  def merge(that : Action) = SequenceAction(this, that)
  
  def undo(model : Model) = model add (id, shape)
  
}

/**
 * Deletes a number of shapes.
 */
case class DeleteShapes(shapes : Map[Int, Shape]) extends Action {

  def execute(model : Model) = model remove shapes.keys

  def merge(that : Action) = that match {
    case CreateShape(i : Int, s : Shape) => DeleteShapes(shapes + (i -> s))
    case CreateShapes(s : Map[Int, Shape]) => DeleteShapes(shapes ++ s)
    case _ => SequenceAction(this, that)
  }

  def undo(model : Model) = model add shapes
}