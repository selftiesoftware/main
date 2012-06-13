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

import com.siigna.app.model.shape.{ShapeSelector, Shape}
import com.siigna.app.model.{Selection, Model}

object Delete {
  
  def apply(id : Int) {
    Model execute DeleteShape(id, Model(id))
  }
  
  def apply(id : Int, part : ShapeSelector) {
    Model execute DeleteShapePart(id, Model(id), part)
  }
  
  def apply(ids : Traversable[Int]) {
    Model execute DeleteShapes(ids.map(i => i -> Model(i)).toMap)
  }
  
  def apply(selection : Selection) {
    Model deselect()
    Model execute DeleteShapeParts(selection.parts)
  }
  
}

/**
 * Deletes a shape.
 */
@SerialVersionUID(320024820)
sealed case class DeleteShape(id : Int, shape : Shape) extends Action {

  def execute(model : Model) = model remove id

  def undo(model : Model) = model.add(id, shape)

}

/**
 * Deletes a ShapeSelector.
 */
@SerialVersionUID(-1303124189)
sealed case class DeleteShapePart(id : Int, shape : Shape, part : ShapeSelector) extends Action {
  
  val parts = shape.delete(part); 
  var partIds = Seq[Int]()
  
  def execute(model : Model) = {
    if (parts.size == 0) {
      // Remove the shape if no parts result from the deletion
      model.remove(id)
    } else if (parts.size == 1) {
      // Replace the shape if the deletion result in one shape
      model.add(id, parts(0))
    } else {
      // Create the new shapes through a CreateAction
      // since we don't know if there are enough local id's
      Create(parts)

      // Remove the shape
      model.remove(id)
    }
  }
  
  def undo(model : Model) = {
    if (parts.size <= 1) {
      model.add(id, shape)
    } else {
      model.remove(id)
    }
  }
  
}

/**
 * Deletes a ShapeSelector.
 */
@SerialVersionUID(-1068568626)
case class DeleteShapeParts(shapes : Map[Int, ShapeSelector]) extends Action {

  private val oldShapes = shapes.map(t => t._1 -> Model(t._1))
  
  def execute(model : Model) = {/*
    // Create a map of shapes with deleted parts
    var xs = Map[Int, Shape]()
    // Create a seq of shapes that are completely removed
    var cs = Seq[Int]()
    // Iterate through shapes
    shapes.foreach(t => {
      val (id, part) = t
      val x = Model(id).delete(part)
      if (x.isDefined) {
        xs = xs + (id -> x.get)
      } else cs = cs :+ id
    })
    // Replace the shapes in the model if defined
    if (xs.isEmpty && cs.isEmpty) model
    else {
      model.remove(cs).add(xs)
    }*/ model
  }
  
  def undo(model : Model) = model //model add oldShapes
  
}

/**
 * Deletes a number of shapes.
 */
@SerialVersionUID(-1408705883)
case class DeleteShapes(shapes : Map[Int, Shape]) extends Action {

  def execute(model : Model) = model remove shapes.keys

  def undo(model : Model) = model add shapes
}