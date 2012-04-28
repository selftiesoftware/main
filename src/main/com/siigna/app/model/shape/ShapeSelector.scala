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

package com.siigna.app.model.shape

/**
 * A ShapeSelector is a part of a shape represented in various ways. This class exists so we can access small parts of
 * one single shape instead of creating new sub-instances of the shape.
 * <br />
 * A SmallShapeSelector indicates that a part of the shape has been selected and that the part is small enough to be
 * represented by one Int.
 * <br />
 * A LargeShapeSelector indicates that a part of the shape has been selected but the part (or the shape) is too big
 * to be identified via an Int.
 * <br />
 * An EmptyShapeSelector indicates that the selection is empty.
 * <br >
 * An
 */
trait ShapeSelector extends Serializable

/**
 * An EmptyShapeSelector is a ShapeSelector with no information and thus represents an empty Shape subset.
 */
case object EmptyShapeSelector extends ShapeSelector

/**
 * A FullShapeSelector signals that the ShapeSelector contains the entire shape. No sub-selection magic is needed.
 */
case object FullShapeSelector extends ShapeSelector

/**
 * A LargeShapeSelector is a set of Ints where each position in each integer represents one selectable
 * part of a shape.
 * @param x
 */
case class LargeShapeSelector(x : Array[Int]) extends ShapeSelector

/**
 * A SmallShapeSelector is basically an Int where each part of the shape represents one position
 * in the binary system. It can thus be identified which parts of the shape are selected and which
 * are not. <b>The specific implementation varies for each shape</b>, but the standard is to use numbers
 * <i>1 to length</i> to indicate which part has been selected.
 *
 * Note: For shapes with selectable parts > 30 a ComplexPartialShape is needed since Int only supports 30
 * positions of positive numbers.
 * TODO: Use negative bits as well.
 *
 * @param x The Int signalling which parts of the shape has been selected.
 */
case class SmallShapeSelector(x : Int) extends ShapeSelector {

  assert(x > 0, "The small shape part must be larger than zero")
  assert(x < 30, "The small shape part cannot be larger than 30")

}