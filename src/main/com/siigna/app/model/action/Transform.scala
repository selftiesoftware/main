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
import com.siigna.util.geom.TransformationMatrix

object Transform {

  def apply(id : Int, transformation : TransformationMatrix) {
    Model execute TransformShape(id, transformation)
  }

}

/**
 * Transforms a shape.
 */
case class TransformShape(id : Int, transformation : TransformationMatrix) extends Action {

  def execute(model : Model) = model add (id, model.shapes(id).transform(transformation))

  def merge(action : Action) = action match {
    case TransformShape(idx : Int, trf : TransformationMatrix) =>
      if (idx == id)
        TransformShape(id, transformation.concatenate(trf))
      else
        SequenceAction(this, action)
    case _ => SequenceAction(this, action)
  }

  def undo(model : Model) = model add (id, model.shapes(id).transform(transformation.inverse))

}

/**
 * Transforms a number of shapes.
 */
case class TransformShapes(ids : Traversable[Int], transformation : TransformationMatrix) extends Action {

  def execute(model : Model) = {
    val map = ids.map(id => (id -> model.shapes(id).transform(transformation))).toMap
    model add map
  }

  def merge(that : Action) = that match {
    case TransformShape(id, transformationOther) => {
      if (transformation == transformationOther)
        TransformShapes(ids.++(Traversable(id)), transformation)
      else
        SequenceAction(this, that)
    }
    case TransformShapes(idsOther, transformationOther) => {
      if (ids == idsOther)
        TransformShapes(ids, transformation.concatenate(transformationOther))
      else
        SequenceAction(this, that)
    }
    case _ => SequenceAction(this, that)
  }

  def undo(model : Model) = {
    val map = ids.map(id => (id -> model.shapes(id).transform(transformation.inverse))).toMap
    model add map
  }
}

