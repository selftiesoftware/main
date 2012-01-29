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
import com.siigna.app.model.shape.{DynamicShape, ImmutableShape, Shape}

object Transform {

  // TODO: Optimize
  def apply(shape : Shape, transformation : TransformationMatrix) { shape match {
    case s : ImmutableShape => {
      val id = Model.findId(_ == shape)
      if (id.isDefined)
        Model(TransformShape(id.get, transformation))
    }
    case s : DynamicShape => {
      s.transformDynamic(transformation)
    }
  } }

  // TODO: Optimize.
  def apply(shapes : Iterable[Shape], transformation : TransformationMatrix) {
    var ids = Seq[String]()
    while (shapes.iterator.hasNext) {
      shapes.iterator.next() match {
        case s : ImmutableShape => {
          val id = Model.findId(_ == shapes.iterator.next)
          if (id.isDefined)
            ids = ids.:+(id.get)
        }
        case s : DynamicShape => {
          s.transformDynamic(transformation)
        }
        case _ =>
      }
    }

    if (ids.size == 1)
      Model(TransformShape(ids(0), transformation))
    else (ids.size > 1)
      Model(TransformShapes(ids, transformation))
  }

}

/**
 * Transforms a shape.
 */
case class TransformShape(id : String, transformation : TransformationMatrix) extends Action {

  def execute(model : Model) = model.update(id, (s : ImmutableShape) => s.transform(transformation))

  def merge(action : Action) = action match {
    case TransformShape(idx : String, trf : TransformationMatrix) =>
      if (idx == id)
        TransformShape(id, transformation.concatenate(trf))
      else
        SequenceAction(this, action)
    case _ => SequenceAction(this, action)
  }

  def undo(model : Model) = model.update(id, (s : ImmutableShape) => s.transform(transformation.inverse))

}

/**
 * Transforms a number of shapes.
 */
case class TransformShapes(ids : Seq[String], transformation : TransformationMatrix) extends Action {

  def execute(model : Model) = model.update(ids, (s : ImmutableShape) => s.transform(transformation))

  def merge(that : Action) = that match {
    case TransformShape(id, transformationOther) => {
      if (transformation == transformationOther)
        TransformShapes(ids.:+(id), transformation)
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

  def undo(model : Model) = model.update(ids, (s : ImmutableShape) => s.transform(transformation.inverse))

}

