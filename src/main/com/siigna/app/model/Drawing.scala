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

import shape.Shape
import collection.immutable.MapProxy
import com.siigna.app.Siigna
import com.siigna.util.geom.{Vector2D, SimpleRectangle2D}

/**
 * <p>
 *   The model of Siigna where all the drawing data are kept.
 * </p>
 *
 * <p>
 *   This is the model part of the
 *   <a href="http://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller">Model-View-Controller</a> pattern.
 *   The Drawing contains all the [[com.siigna.app.model.shape.Shape]]s of the current active drawing.
 * </p>
 *
 * <h3>Manipulating the Drawing through actions</h3>
 * <p>
 *   The only way to
 *   manipulate the drawing is via [[com.siigna.app.model.action.Action]]s - they can be executed or undone.
 *   That is it. This control structure is in place because it allows the user to undo and redo; important features
 *   when you are creating a large and complex drawing.
 *   The Drawing is also subject to change from other parts, namely other clients attached to the same drawing. This
 *   is also done via [[com.siigna.app.model.action.Action]]s, which affects everyone - so be sure be careful when
 *   erasing the entire model for instance...
 * </p>
 *
 * <h3>Selection</h3>
 * <p>
 *  Drawing also has a [[com.siigna.app.model.Selection]]. This represents the way any number or
 *  [[com.siigna.app.model.shape.Shape]]s have been selected. So besides <i>which</i>
 *  [[com.siigna.app.model.shape.Shape]] has been selected, we also need to know <i>how</i> they were selected.
 *  If no selection is present, the selection is None.
 * </p>
 *
 * <h3>Boundaries</h3>
 * <p>
 *  A standard Drawing is designed to fit the <a href="http://en.wikipedia.org/wiki/Paper_size">international
 *  paper-size standard</a> with an aspect ratio of sqrt(2) (square root of 2). We chose this format to make
 *  it simpler to view, print and send drawings in ways that as many people as possible understands. The
 *  absolute paper-size is subject to change, or course, and the dimensions of the paper as a
 *  [[com.siigna.util.geom.Rectangle]] can be found via the <code>boundary()</code> method.
 *  <br />
 *  It is possible to set a print margin. See more in the [[com.siigna.app.Siigna]] object.
 * </p>
 */
object Drawing extends ActionModel
                  with SpatialModel[Int, Shape]
                  with MapProxy[Int, Shape] {

  /**
   * The boundary from the current content of the Model.
   * The rectangle returned fits an A-paper format, but <b>a margin is added</b>.
   * This is done in order to make sure that the print viewed on page is the
   * actual print you get.
   *
   * @return A rectangle in an A-paper format (margin included). The scale is given in <code>boundaryScale</code>.
   */
  def boundary = {
    val newBoundary  = model.mbr
    val size         = (newBoundary.bottomRight - newBoundary.topLeft).abs
    val center       = newBoundary.center
    //val proportion   = 1.41421356

    // Saves the format, as the format with the margin subtracted
    val printMargin = Siigna.double("printMargin").getOrElse(13.0)
    var aFormatMin = Siigna.double("printFormatMin").getOrElse(210.0) - printMargin
    var aFormatMax = Siigna.double("printFormatMax").getOrElse(297.0) - printMargin

    // If the format is too small for the least proportion, then up the size
    // one format.
    // TODO: Optimize!
    //TODO: prevent margin from multuttiplying as well. It should be fixed regardless of paper scale.
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
      SimpleRectangle2D(center.x - aFormatMax * 0.5, center.y - aFormatMin * 0.5,
                center.x + aFormatMax * 0.5, center.y + aFormatMin * 0.5)
    } else {
      SimpleRectangle2D(center.x - aFormatMin * 0.5, center.y - aFormatMax * 0.5,
                center.x + aFormatMin * 0.5, center.y + aFormatMax * 0.5)
    }
  }

  //the boundary without print margin
  def boundaryWithoutMargin = {
    val newBoundary  = model.mbr
    val size         = (newBoundary.bottomRight - newBoundary.topLeft).abs
    val center       = newBoundary.center
    //val proportion   = 1.41421356

    // Saves the format
    var aFormatMin = Siigna.double("printFormatMin").getOrElse(210.0)
    var aFormatMax = Siigna.double("printFormatMax").getOrElse(297.0)

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
      SimpleRectangle2D(center.x - aFormatMax * 0.5, center.y - aFormatMin * 0.5,
                center.x + aFormatMax * 0.5, center.y + aFormatMin * 0.5)
    } else {
      SimpleRectangle2D(center.x - aFormatMin * 0.5, center.y - aFormatMax * 0.5,
                center.x + aFormatMin * 0.5, center.y + aFormatMax * 0.5)
    }
  }

  /**
   * The scale of the height and width boundary of the model, or in other words, the relation between the height
   * and width of the paper and the maximum print scale.
   *
   * Uses toInt since it always rounds down to an integer.
   */
  def boundaryScale : Int =
    math.ceil((scala.math.max(boundaryWithoutMargin.width, boundaryWithoutMargin.height) / (Siigna.double("printFormatMax").getOrElse(297.0)).toInt)).toInt

  /**
   * The [[com.siigna.util.rtree.PRTree]] used by the model.
   */
  //def rtree = model.rtree

  /**
   * The current selection represented by a an Option of [[com.siigna.app.model.Selection]].
   * @return  Some[Selection] if a selection is active or None if nothing has been selected
   */
  def selection : Option[Selection] = model.selection

  //------------- Required by the MapProxy trait -------------//
  def self = model.shapes

}
