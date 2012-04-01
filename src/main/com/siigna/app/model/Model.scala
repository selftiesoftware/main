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


import com.siigna.app.Siigna
import com.siigna.util.geom.{Vector2D, Rectangle2D}
import collection.parallel.IterableSplitter
import shape.ImmutableShape
import collection.parallel.immutable.{ParMap}

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
sealed class Model(val shapes : ParMap[Int, ImmutableShape]) extends ImmutableModel[Int, ImmutableShape]
                                                                    with SpatialModel[Int, ImmutableShape]
                                                                    with ModelBuilder[Int, ImmutableShape] {

  def build(coll : ParMap[Int, ImmutableShape]) = new Model(coll)

}

/**
 * The model of Siigna.
 */
object Model extends ActionModel
                with DynamicModel[Int]
                with SpatialModel[Int, ImmutableShape]
                with ParMap[Int, ImmutableShape] {

  /**
   * The boundary from the current content of the Model.
   * The rectangle returned fits an A-paper format, but <b>without margin</b>.
   * This is done in order to make sure that the print viewed on page is the
   * actual print you get.
   *
   * @return A rectangle in an A-paper format (margin exclusive). The scale is given in <code>boundaryScale</code>.
   */
  def boundary = {
    val newBoundary  = model.mbr
    val size         = (newBoundary.bottomRight - newBoundary.topLeft).abs
    val center       = newBoundary.center
    //val proportion   = 1.41421356

    // Saves the format, as the format with the margin subtracted
    var aFormatMin = Siigna.printFormatMin
    var aFormatMax = Siigna.printFormatMax

    // If the format is too small for the least proportion, then up the size
    // one format.
    // TODO: Optimize!
    val list = List[Double](2, 2.5, 2)
    var take = 0 // which element to "take" from the above list
    while (aFormatMin < scala.math.min(size.x, size.y) || aFormatMax < scala.math.max(size.x, size.y)) {
      val factor = list(take)
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
   * The [[com.siigna.util.rtree.PRTree]] used by the model.
   */
  //def rtree = model.rtree

  def shapes = model.shapes

  //------------- Required by the ParMap trait -------------//
  def +[U >: ImmutableShape](kv : (Int, U)) = model.shapes.+[U](kv)
  def -(key : Int) = model.shapes.-(key)
  def get(key : Int) = model.shapes.get(key)
  def seq = model.shapes.seq
  def size = model.shapes.size
  def splitter = model.shapes.iterator.asInstanceOf[IterableSplitter[(Int, ImmutableShape)]]
  
}