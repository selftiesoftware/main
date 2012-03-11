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

package com.siigna.app.model

import shape.Shape
import collection.parallel.immutable.ParMap

/**
 * An immutable model containing shapes
 * @tparam Key  The keys in the Model.
 * @tparam Value  The shapes in the model.
 * @rparam Model  The return-type of the model.
 */
trait ImmutableModel[Key, Value <: Shape, Model <: ImmutableModel] {
  
  /**
   * Add a shape to the model.
   */
  def add(key : Key, shape : Value) : Model

  /**
   * Add several shapes to the model.
   */
  def add(shapes : Map[Key, Value]) : Model

  /**
   * Remove a shape from the model.
   */
  def remove(key : Key) : Model

  /**
   * Remove several shapes from the model.
   */
  def remove(keys : Traversable[Key]) : Model

  /**
   * Update a shape in the model.
   */
  def update(key : Key, shape : Value) : Model

  /**
   * Update several shapes in the model.
   */
  def update(shapes : Map[Key, Value]) : Model

}
