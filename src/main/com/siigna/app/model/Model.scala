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


import action.{VolatileAction, Action}
import com.siigna.util.logging.Log
import shape.{ImmutableShape, Shape}
import collection.parallel.immutable.{ParMap}
import com.siigna.util.rtree.PRTree

/**
 * An immutable model with two layers: an static and dynamic.
 * <br />
 * The static part is basically a long list of all the
 * [[com.siigna.app.model.shape.ImmutableShape]]s and their keys in the Model.
 * <br />
 * The dynamic part allows selecting parts of the global immutable layer. These shapes can be altered
 * without changes in the static layer which allows for significant performance benefits. When the
 * changes have been made (and the shapes are deselected), the shapes are removed from the dynamic
 * layer, and the actions which have been applied on the dynamic layer is applied on the static layer.
 *
 * @param shapes  The shapes and their identifiers (keys) stored in the model.
 *
 * TODO: Examine possibility to implement an actor. Thread-less please.
 */
sealed class Model(val shapes : ParMap[Int, ImmutableShape]) extends ImmutableModel[Int, ImmutableShape, Model]
                      with DynamicModel[Int, Model]
                      with GroupableModel[Int, Model]
                      with SpatialModelInterface {

  val rtree = PRTree;

  def add(key : Int, shape : ImmutableShape) = { this }
  def add(shapes : Map[Int, ImmutableShape]) = { this }

  def remove(key: Int) = null

  def remove(keys: Traversable[Int]) = null

  def update(key: Int, shape: ImmutableShape) = null

  def update(shapes: Map[Int, ImmutableShape]) = null

  def group(key: Int, group: Int) = null

  def group(keys: Traversable[Int]) = null

  def group(keys: Traversable[Int], group: Int) = null

  def ungroup(group: Int) = null

  def ungroup(shape: Int, group: Int) = null
}

/**
 * The model of Siigna.
 */
object Model extends SpatialModelInterface with ParMap[Int, ImmutableShape] {

  /**
   * The [[com.siigna.app.model.action.Action]]s that have been executed on this model.
   */
  private var executed = Seq[Action]()

  /**
   * The underlying immutable model of Siigna.
   */
  private var model = new Model(ParMap[Int, ImmutableShape]())

  /**
   * The [[com.siigna.app.model.action.Action]]s that have been undone on this model. 
   */
  private var undone = Seq[Action]()

  def boundary = rtree.mbr

  /**
   * Execute an action, list it as executed and clear the undone stack to make way for a new actions
   * (if it is not a [[com.siigna.app.model.action.VolatileAction]]).
   */
  def execute(action : Action) {
    model = action.execute(model)

    // Only store the action if it is not volatile
    if (!action.isInstanceOf[VolatileAction]) {
      executed +:= action
      undone = Seq()
    }
  }

  /**
   * Redo an action, by executing the last function that's been undone.
   */
  def redo() {
    if (undone.size > 0) {
      // Retrieve the event
      val action = undone.head
      undone = undone.tail

      // Execute the event and add it to the executed list
      model = action.execute(model)
      executed +:= action
    } else {
      Log.warning("Model: No more actions to redo.")
    }
  }

  /**
   * The [[com.siigna.util.rtree.PRTree]] used by the model.
   */
  def rtree = model.rtree

  /**
   * Undo an action and put it in the list of undone actions.
   */
  def undo() {
    if (executed.size > 0) {
      // Retrieve the action
      val action = executed.head
      executed = executed.tail

      // Undo it and add it to the undone list
      model = action.undo(model)
      undone +:= action
    } else {
      Log.warning("Model: No more actions to undo.")
    }
  }

  //------------- Required by the ParIterable trait -------------//
  def +[U >: Int](kv : (Int, ImmutableShape)) = model.shapes.+[Int, U](kv)
  def -(key : Int) = model.shapes.-(key)
  def get(key : Int) = model.shapes.get(key)
  def seq : Iterable[Shape] = model.shapes.seq.values ++ model.dynamics.seq.values
  def size = model.shapes.size
  def splitter = model.shapes.splitter.appendParIterable(model.dynamics.splitter)

}