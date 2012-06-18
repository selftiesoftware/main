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

/**
 * An immutable model containing shapes
 * @tparam Key  The keys in the Model.
 * @tparam Value  The shapes in the model.
 */
trait ImmutableModel[Key, Value <: HasAttributes] extends ModelBuilder[Key, Value]
                                                 with GroupableModel[Key, Value] {

  /**
   * Add a shape to the model.
   */
  def add(key : Key, shape : Value) = build(shapes.+((key, shape)))

  /**
   * Add several shapes to the model.
   */
  def add(shapes : Map[Key, Value]) = build(this.shapes ++ shapes)

  /**
   * Remove a shape from the model.
   */
  def remove(key: Key) = build(shapes - key)

  /**
   * Remove several shapes from the model.
   */
  def remove(keys: Traversable[Key]) = build(shapes.filterNot(i => keys.exists(_ == i._1)))

}
