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
import com.siigna.app.model.shape.{CollectionShape, Shape}

/**
* An object that allows you to create one or multiple shapes.
*/
object Create {

  def apply(shape : Shape) {
    val id = getId
    apply(id, shape)
  }
  
  def apply(id : Int, shape : Shape) {
    Model execute(CreateShape(id, shape), id > 0)
  }

  def apply[T <: Shape](collection : CollectionShape[T]) {
    val id = getId
    apply(id, collection)
  }

  def apply(shape1 : Shape, shape2 : Shape, shapes : Shape*) {
    apply(Traversable(shape1, shape2) ++ shapes)
  }

  def apply(shapes : Traversable[Shape]) {
    if (shapes.size > 1) {
      var remote = Map[Int, Shape]()
      var local  = Map[Int, Shape]()
      shapes.foreach(s => {
        val id = getId
        if (id > 0) remote = remote + (id -> s)
        else        local  = local + (id -> s)
      })
      if (!remote.isEmpty) {
        Model execute CreateShapes(remote)
      }
      if (!local.isEmpty) {
        Model execute(CreateShapes(local), true)
      }
    } else if (shapes.size == 1) {
      apply(shapes.head)
    } else {
      Log.info("Create: Create was called with an empty Traversable.")
    }
  }

  /**
   * Creates several shapes
   * @param shapes  A map containing a number of ids and shapes.
   */
  def apply(shapes : Map[Int,Shape]) {
    if (!shapes.isEmpty) Model execute CreateShapes(shapes)
  }

}

/**
 * <p>Creates a shape with an associated id.</p>
 *
 * @define actionFactory [[com.siigna.app.model.action.Create]]
 * $actionDescription
 */
@SerialVersionUID(-1099507104)
case class CreateShape(id : Int, shape : Shape) extends Action {
  
  def execute(model : Model) = model add (id, shape)

  def undo(model : Model) = model remove id

}

/**
 * Creates several shapes.
 * @define actionFactory [[com.siigna.app.model.action.Create]]
 * $actionDescription
 */
@SerialVersionUID(1827663026)
case class CreateShapes(shapes : Map[Int, Shape]) extends Action {

  require(shapes.size > 0, "Cannot initialize CreateShapes with zero shapes.")
  
  def execute(model : Model) = model add shapes

  def undo(model : Model) = model remove shapes.keys
}