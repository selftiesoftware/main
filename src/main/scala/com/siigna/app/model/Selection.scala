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
package com.siigna.app.model

import action.{Delete, Action}
import com.siigna.util.collection.{HasAttributes, Attributes}
import com.siigna.util.geom.{TransformationMatrix, Vector2D}
import collection.immutable.{Map, MapProxy}
import shape._
import collection.mutable.BitSet

/**
 * <p>A Selection is a mutable wrapper for parts of a regular Shape(s).</p>
 *
 * <p>
 * A selection does not store entire shapes (that would be annoyingly expensive!) but instead remembers
 * subsets of shapes (the [[com.siigna.app.model.shape.ShapePart]]s). These subsets can be given to
 * the shapes stored in the model and through the [[com.siigna.app.model.shape.Shape.apply]] method
 * the shapes will provide [[com.siigna.app.model.shape.PartialShape]]s which can be used to manipulate
 * these subsets.</p>
 *
 * <p>
 * When altered, the selection saves the action required to alter the shape(s) in the static layer, so the changes
 * can be made to the static version later on - when the shape(s) are "demoted" back into the static layer.</p>
 *
 * @param parts  The ids of the wrapped shape(s).
 * @see [[com.siigna.app.model.MutableModel]]
 */
@SerialVersionUID(2104259901)
case class Selection(var parts: Map[Int, ShapePart]) extends HasAttributes with MapProxy[Int, ShapePart] {

  type T = Selection

  /**
   * Stores a private transformation matrix that indicates the translation applied to the
   * Selection since creation.
   */
  var transformation : TransformationMatrix = TransformationMatrix()

  /**
   * Stores the attributes variable with values that have been applied to the Selection since
   * creation.
   */
  var attributes : Attributes = Attributes()

  /**
   * Applies a transformation matrix to the selected parts of the contained shapes only, and not
   * touching the rest of the shape.
   * @param t  The transformation matrix to be applied.
   * @return The shapes with the partial transform applied.
   */
  def apply(t : TransformationMatrix) : Iterable[Shape] = {
    parts.map(p => {
      Drawing(p._1)(p._2)
    }).collect{ case Some(p : PartialShape) => p.apply(t) }
  }

  /**
   * The boundary of the underlying ImmutableShapes.
   * @return A SimpleRectangle2D.
   */
  def boundary = parts.map(s => Drawing(s._1)).foldLeft(Drawing(parts.head._1).boundary)((a, b) => a.expand(b.boundary))

  /**
   * Calculates the distance from the vector and to the underlying Shape.
   * @param point  The point to calculate the distance to.
   * @param scale  The scale in which we are calculating.
   * @return  The length from the closest point of this shape to the point.
   */
  def distanceTo(point: Vector2D, scale: Double) = parts.map(s => Drawing(s._1).distanceTo(point)).reduceLeft((a, b) => if (a < b) a else b) * scale

  /**
   * Retrieves the selected parts of the shapes in the current selection.
   * @return A number of selected sub-shapes.
   */
  def selectedShapes : Iterable[Shape] = {
    parts.map((t : (Int, ShapePart)) => {
      Drawing(t._1).getShape(t._2)
    }).collect { case Some(s : Shape) => s.setAttributes(attributes) }
  }

  /**
   * Retrieves the current shapes of the selection.
   * @return A map containing the ids and the shapes used in the current selection.
   */
  def shapes : Map[Int, Shape] = {
    parts.map((t : (Int, ShapePart)) => {
      (t._1 -> Drawing(t._1).addAttributes(attributes))
    })
  }
  
  def self = parts

  def setAttributes(attributes: Attributes) = {
    this.attributes = attributes
    this
  }

  /**
   * Switches a selection on or off depending on its current state.
   * @param id  The id of the shape whose part we wish to toggle
   * @param selector  The selector describing which part(s) of the shape to select
   */
  def toggle(id : Int, selector : ShapePart) {
    if (parts contains id) {
      parts(id) match {
        case PolylineShape.Part(xs) => parts = parts + (id -> PolylineShape.Part({
          val set = BitSet()
          for (i <- 0 to xs.max) {
            if (!xs(i)) set + i
          }
          set
        }))
        case EmptyShapePart          => parts = parts + (id -> selector)
        case FullShapePart           => parts = parts - id
        case LineShape.Part(x)  => parts = parts + (id -> LineShape.Part(!x))
        case _ =>
      }
    } else parts + (id -> selector)
  }

  override def toString() = "Selection[" + parts + "]"

  /**
   * Transforms the underlying Shape by adding a TransformShape action to the list of actions
   * applied to this Selection
   * @param transformation  The TransformationMatrix to apply to the shape.
   */
  def transform(transformation: TransformationMatrix) = {
    // Store the transformation
    this.transformation = transformation
    // Return this
    this
  }

}

/**
 * Companion-object for the Selection class.
 */
object Selection {

  /**
   * A method to create a Selection with only one id.
   */
  def apply(id: Int, part : ShapePart) = {
    new Selection(Map(id -> part))
  }

}