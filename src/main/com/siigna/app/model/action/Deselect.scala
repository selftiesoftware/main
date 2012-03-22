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
import com.siigna.app.model.shape.{DynamicShape, ImmutableShape, Shape}
import com.siigna.util.logging.Log

/**
 * An [[com.siigna.app.model.action.Action]] that can deselect objects. Apart from just deselecting
 * this action actually makes sure the [[com.siigna.app.model.shape.DynamicShape]]s that has been
 * selected are thrown away, and the changes applied to them are stored in the underlying
 * [[com.siigna.app.model.ImmutableModel]] where alle the shapes are hidden. Thus <i>the changes
 * applied to selected shapes are not saved in the model until they are deselected</i>. Until that
 * happens they are just floating around in dynamic space somewhere...
 */
object Deselect {
  /*
  def apply(shape : Shape) { shape match {
    case s : DynamicShape => {
      Model.deselect(s)
    }
    case _ =>
  } }

  def apply(shape1 : Shape, shape2 : Shape, shapes : Shape*) {
    apply(Traversable(shape1, shape2) ++ shapes)
  }

  def apply[T](shapes : Traversable[T])(implicit m : Manifest[T]) {
    m.erasure.getSimpleName match {
      case "String" => Model.deselect(shapes.asInstanceOf[Traversable[String]])
      case e => Log.info("Selection: Got type: "+e+", required String.") //shapes.foreach(apply)
    }
  }

  def apply(id : String) { Model.deselect(id) }

  def apply(id1 : String, id2 : String, ids : String*) { apply(Traversable(id1, id2) ++ ids) }
                */
}

