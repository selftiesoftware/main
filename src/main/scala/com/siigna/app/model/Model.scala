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

import action.Action
import shape.Shape
import com.siigna.util.collection.{HasAttributes, Attributes}
import com.siigna.app.model.selection.Selection

/**
 * An immutable model containing shapes with uniquely (and globally for this specific drawing) identifiable keys.
 *
 * @param shapes  The shapes and their identifiers (keys) stored in the model.
 * @param executed  The actions that have been executed on this model.
 * @param undone  The actions that have been undone on this model.
 */
sealed case class Model(shapes : Map[Int, Shape], executed : Seq[Action], undone : Seq[Action], attributes : Attributes)
       extends ImmutableModel[Int, Shape]
          with SpatialModel[Int, Shape]
          with ModelBuilder[Int, Shape]
          with HasAttributes{

  type T = Model

  /**
   * Creates an empty model.
   * @return  A model with no shapes, actions or attributes.
   */
  def this() = this(Map(), Nil, Nil, Attributes())

  def build(coll : Map[Int, Shape]) = new Model(coll, executed, undone, attributes)

  def build(coll : Map[Int, Shape], executed : Seq[Action], undone : Seq[Action]) = new Model(coll, executed, undone, attributes)

  /**
   * The current selection, if any, represented by a [[com.siigna.app.model.selection.Selection]]. The Selection
   * contains the references to the shapes that have been selected and information about how they were selected. It
   * can also convert itself to one or more [[com.siigna.app.model.shape.Shape]]s for rendering.
   *
   * The selection is placed in the Model to ensure that the selection is removed whenever the model changes.
   */
  var selection : Selection = Selection.empty

  /**
   * Replace the current attributes with the given set of attributes.
   *
   * @param newAttributes  the new attributes to replace.
   * @return  a HasAttributes object with the updated attributes.
   */
  def setAttributes(newAttributes: Attributes) = Model(shapes,executed,undone,newAttributes)

}

