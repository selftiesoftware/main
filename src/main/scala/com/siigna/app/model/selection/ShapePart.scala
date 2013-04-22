/*
 *
 *  * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 *  * to Share — to copy, distribute and transmit the work,
 *  * to Remix — to adapt the work
 *  *
 *  * Under the following conditions:
 *  * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 *  * Noncommercial — You may not use this work for commercial purposes.
 *  * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 *
 */

package com.siigna.app.model.selection

import com.siigna.app.model.shape.Shape
import com.siigna.util.geom.Vector2D

/**
 * A trait used to describe a runtime selection of a given shape, i. e. a description of a given shape and how it has
 * been selected. The selection is different from the [[com.siigna.app.model.selection.ShapeSelector]] and the
 * [[com.siigna.app.model.shape.Shape]] because the ShapePart contains the responsibility
 * for both controlling the manipulation of the ShapePart.
 * @tparam T  The type of the shape that have been selected.
 * @see [[com.siigna.app.model.selection.ShapeSelector]], [[com.siigna.app.model.shape.Shape]]
 */
trait ShapePart[+T <: Shape] {

  /**
   * Adds a new part to the shape-part by including it in the implementation for a given shape.
   * @param part  The part of the shape to include.
   * @return  A new ShapePart with the selector included.
   */
  def add(part : ShapeSelector[T]) : ShapePart[T]

  /**
   * Retrieves the part of the shape that have selected, represented as a shape. If the selection is empty or the
   * combination of the given [[com.siigna.app.model.selection.ShapeSelector]] does not make sense, we return None.
   * @return  Some[Shape] if the selection can be represented graphically, None otherwise.
   */
  def part : Option[Shape]

  /**
   * The entire [[com.siigna.app.model.shape.Shape]] that have been selected.
   * @return  The 'parent' shape to this ShapePart.
   */
  def shape : T

  /**
   * The [[com.siigna.app.model.selection.ShapeSelector]] of the [[com.siigna.app.model.shape.Shape]], that is the
   * way the shape have been selected.
   * @return  A ShapeSelector capable of describing a subset of a shape with type T.
   */
  def selector : ShapeSelector[T]

  /**
   * Retrieves a sequence of points in the current ShapePart.
   * @return  A sequence of points. Can be empty.
   */
  def vertices : Seq[Vector2D]

}

/**
 * A full selection of a shape. Used to optimize the selection process by simply referring to the entire shape as
 * the selection. In other words all the selection-logic can be shaved away, so the part technically behaves as
 * the entire shape.
 * @param shape The shape to select a part of.
 * @tparam T  The type of the shape that have been selected.
 */
case class FullShapePart[+T <: Shape](shape : T) extends ShapePart[T] {

  /**
   * Adding a selector to a full selection simply results in the same selection.
   * @param selector  The selector to add.
   * @return  A new ShapePart with the selector included.
   */
  def add(selector : ShapeSelector[T]) = this

  /**
   * In a FullShapePart the shape-part is equal to the entire shape.
   * @return  The full shape described as Some(shape).
   */
  def part = Some(shape)

  /**
   * Returns a [[com.siigna.app.model.selection.FullShapeSelector]] that represents the entire shape-selection.
   * @return  A [[com.siigna.app.model.selection.FullShapeSelector]]
   */
  def selector = FullShapeSelector

  /**
   * Returns all the vertices of the shape.
   */
  lazy val vertices = shape.geometry.vertices

}