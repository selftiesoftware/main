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
 * A quick-reference for deselecting objects.
 */
object Deselect {

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

}

/**
 * A quick-reference for selecting objects.
 */
object Select {

  def apply(shape : Shape) { shape match {
    case s : ImmutableShape => {
      val id = Model.findId(_ == s)
      if (id.isDefined)
        Model.select(id.get)
    }
    case _ =>
  } }

  def apply(shape1 : Shape, shape2 : Shape, shapes : Shape*) { apply(Traversable(shape1, shape2) ++ shapes) }

  def apply[T](shapes : Traversable[T])(implicit m : Manifest[T]) {
    m.erasure.getSimpleName match {
      case "String" => Model.select(shapes.asInstanceOf[Traversable[String]])
      case e => Log.info("Selection: Got type: "+e+", required String.") //shapes.foreach(apply)
    }
  }

  def apply(id : String) { Model.select(id) }

  def apply(id1 : String, id2 : String, ids : String*) { apply(Traversable(id1, id2) ++ ids) }

}
