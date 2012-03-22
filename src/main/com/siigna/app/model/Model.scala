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
import com.siigna.util.rtree.PRTree
import com.siigna.app.Siigna
import com.siigna.util.geom.{Vector2D, Rectangle2D}
import collection.parallel.IterableSplitter
import collection.parallel.immutable.{ParHashMap, ParMap}
import shape.ImmutableShape

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
sealed class Model(val shapes : ParHashMap[Int, ImmutableShape]) extends ImmutableModel[Int, ImmutableShape]
                                                                    with DynamicModel[Int]
                                                                    with ModelBuilder[Int, ImmutableShape] {

  def build(coll : ParHashMap[Int, ImmutableShape]) = new Model(coll)

}

/**
 * The model of Siigna.
 */
object Model extends SpatialModel[Int, ImmutableShape] with ParMap[Int, ImmutableShape] {

  /**
   * The [[com.siigna.app.model.action.Action]]s that have been executed on this model.
   */
  private var executed = Seq[Action]()

  /**
   * The underlying immutable model of Siigna.
   */
  private var model = new Model(ParHashMap[Int, ImmutableShape]())

  /**
   * The [[com.siigna.app.model.action.Action]]s that have been undone on this model. 
   */
  private var undone = Seq[Action]()

  /**
   * The boundary from the current content of the Model.
   * The rectangle returned fits an A-paper format, but <b>without margin</b>.
   * This is done in order to make sure that the print viewed on page is the
   * actual print you get.
   *
   * @return A rectangle in an A-paper format (margin exclusive). The scale is given in <code>boundaryScale</code>.
   */
  def boundary = {
    val newBoundary  = model.rtree.mbr
    val size         = (newBoundary.bottomRight - newBoundary.topLeft).abs
    val center       = (newBoundary.bottomRight + newBoundary.topLeft) / 2
    //val proportion   = 1.41421356

    // Saves the format, as the format with the margin subtracted
    var aFormatMin = Siigna.printFormatMin
    var aFormatMax = Siigna.printFormatMax

    // If the format is too small for the least proportion, then up the size
    // one format.
    // TODO: Optimize!
    val list = List[Double](2, 2.5, 2)
    var take = 0
    while (aFormatMin < scala.math.min(size.x, size.y) || aFormatMax < scala.math.max(size.x, size.y)) {
      val factor = list.apply(take)
      aFormatMin *= factor
      aFormatMax *= factor
      take = if (take < 2) take + 1 else 0
    }

    // Set the boundary-rectangle.
    if (size.x >= size.y) {
      Rectangle2D(Vector2D(center.x - aFormatMax * 0.5, center.y - aFormatMin * 0.5),
                Vector2D(center.x + aFormatMax * 0.5, center.y + aFormatMin * 0.5))
    } else {
      Rectangle2D(Vector2D(center.x - aFormatMin * 0.5, center.y - aFormatMax * 0.5),
                Vector2D(center.x + aFormatMin * 0.5, center.y + aFormatMax * 0.5))
    }
  }

  /**
   * Uses toInt since it always rounds down to an integer.
   */
  def boundaryScale = (scala.math.max(boundary.width, boundary.height) / Siigna.printFormatMax).toInt

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

  //------------- Required by the ParMap trait -------------//
  def +[U >: ImmutableShape](kv : (Int, U)) = model.shapes.+[U](kv)
  def -(key : Int) = model.shapes.-(key)
  def get(key : Int) = model.shapes.get(key)
  def seq = model.shapes.seq
  def size = model.shapes.size
  def splitter = model.shapes.iterator.asInstanceOf[IterableSplitter[(Int, ImmutableShape)]]
  
}