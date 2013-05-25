/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */

package com.siigna.app.model

import action.Action
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
  protected def build(coll : Map[Key, Value]) : Model
  
  /**
   * Builds a new Model from the given Map of shapes along with the actions this model has executed and undone.
   * @param coll  The map of keys and shapes.
   * @param executed  The actions that has been executed on this model
   * @param undone  The actions that has been undone on this model.
   * @return A new (immutable) Model.
   */
  protected def build(coll : Map[Key, Value], executed : Seq[Action], undone : Seq[Action]) : Model

  /**
   * The shapes used to perform actions upon.
   * @return A Map containing the shapes.
   */
  def shapes : Map[Key, Value]
  
}