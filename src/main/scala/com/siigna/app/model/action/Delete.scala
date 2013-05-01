/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */
package com.siigna.app.model.action

import com.siigna.app.model.shape.Shape
import com.siigna.app.model.{Drawing, Model}
import com.siigna.app.model.selection.{ShapeSelector, Selection}

/**
 * An object that contains methods for deleting shapes from the [[com.siigna.app.model.Drawing]].
 */
object Delete {

  /**
   * Removes the [[com.siigna.app.model.shape.Shape]] with the given id from the [[com.siigna.app.model.Drawing]].
   * @param id  The unique identifier (id) of the shape to delete.
   */
  def apply(id : Int) {
    Drawing execute DeleteShape(id, Drawing(id))
  }

  /**
   * Removes a part (represented by the [[com.siigna.app.model.selection.ShapeSelector]]) of the
   * [[com.siigna.app.model.shape.Shape]] with the given id in the [[com.siigna.app.model.Drawing]].
   * @param id  The unique identifier (id) of the shape in the model.
   * @param selector  The part of the shape to delete.
   */
  def apply(id : Int, selector : ShapeSelector) {
    apply(Map(id -> selector))
  }
  
  def apply(shapes : Map[Int, ShapeSelector]) {
    val oldShapes = shapes.map(t => t._1 -> Drawing(t._1))
    val newShapes = shapes.map(t => Drawing(t._1).delete(t._2)).flatten
    // Does the deletion result in new shapes?
    if (newShapes.isEmpty) { // No - that's easy!
      Drawing execute DeleteShapes(oldShapes)
    } else { // Yes - now we need ids
      Drawing.execute(DeleteShapeParts(oldShapes, Drawing.getIds(newShapes)))
    }
  }
  
  def apply(ids : Traversable[Int]) {
    Drawing execute DeleteShapes(ids.map(i => i -> Drawing(i)).toMap)
  }

  def apply(selection : Selection) {
    if (!selection.isEmpty) {
      Drawing execute DeleteShapeParts(selection.shapes, Drawing.getIds(selection.values.map(t => t._1.delete(t._2)).flatten))
    }
  }
  
}

/**
 * Deletes a shape with the given id.
 * @param id  The unique identifier (id) of the shape to delete.
 * @param shape  The shape that have been deleted, used to re-create it in case the user regrets the deletion.
 */
sealed case class DeleteShape(id : Int, shape : Shape) extends Action {

  def execute(model : Model) = model remove id
  
  def ids = Traversable(id)

  def undo(model : Model) = model.add(id, shape)
  
  def update(map : Map[Int, Int]) = copy(map.getOrElse(id, id))

}

/**
 * Deletes a part of a shape with a givnen unique identifier (id).
 * @param id  The id of the shape to delete.
 * @param shape  The shape to delete a part of, used to re-create it in case the user regrets.
 * @param selector  The part of the shape to delete.
 */
sealed case class DeleteShapePart(id : Int, shape : Shape, selector : ShapeSelector) extends Action {
  
  val parts = shape.delete(selector)
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
  
  def ids = Traversable(id)
  
  def undo(model : Model) = {
    if (parts.size <= 1) {
      model.add(id, shape)
    } else {
      model.remove(id)
    }
  }
  
  def update(map : Map[Int, Int]) = copy(map.getOrElse(id, id))
}

/**
 * Deletes a part of a shape represented as a shape selector.
 */
case class DeleteShapeParts(oldShapes : Map[Int, Shape], newShapes : Map[Int, Shape]) extends Action {
  
  def execute(model : Model) = 
    model.remove(oldShapes.keys).add(newShapes)
  
  def ids = oldShapes.keys ++ newShapes.keys
  
  def undo(model : Model) = 
    model.remove(newShapes.keys).add(oldShapes)
  
  def update(map : Map[Int, Int]) = copy(
    oldShapes.map(t => map.getOrElse(t._1, t._1) -> t._2),
    newShapes.map(t => map.getOrElse(t._1, t._1) -> t._2))
  
}

/**
 * Deletes a number of shapes.
 */
case class DeleteShapes(shapes : Map[Int, Shape]) extends Action {

  def execute(model : Model) = model remove shapes.keys

  def ids = shapes.keys

  def undo(model : Model) = model add shapes

  def update(map : Map[Int, Int]) = copy(shapes.map(t => map.getOrElse(t._1, t._1) -> t._2))
}