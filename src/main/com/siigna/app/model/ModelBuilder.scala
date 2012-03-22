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

import collection.GenMap


/**
 * Trait that provides necessary information to build a model with a
 * [[scala.collection.parallel.immutable.ParHashMap]] containing the given types.
 */
trait ModelBuilder[Key, Value] {
  
  /**
   * Builds a new Model from the given Map of shapes.
   * @param coll  The map of keys and shapes.
   * @return A new (immutable) Model.
   */
  protected def build(coll : GenMap[Key, Value]) : Model

  /**
   * The shapes used to perform actions upon.
   * @return A ParHashMap containing the shapes.
   */
  def shapes : GenMap[Key, Value]
  
}