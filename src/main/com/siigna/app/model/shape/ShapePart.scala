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
 * A ShapePart is a part of a shape represented in various ways. This class exists so we can access small parts of
 * one single shape instead of creating new sub-instances of the shape.
 * <br />
 * A SmallShapePart indicates that a part of the shape has been selected and that the part is small enough to be
 * represented by one Int.
 * <br />
 * A LargeShapePart indicates that a part of the shape has been selected but the part (or the shape) is too big
 * to be identified via an Int.
 * <br />
 * An EmptyShapePart indicates that the selection is empty.
 * <br >
 * An
 */
trait ShapePart extends Serializable

/**
 * An EmptyShapePart is a ShapePart with no information and thus represents an empty Shape subset.
 */
case object EmptyShapePart extends ShapePart

/**
 * A FullShapePart signals that the ShapePart contains the entire shape. No sub-selection magic is needed.
 */
case object FullShapePart extends ShapePart

/**
 * A LargePartialShape is a set of Ints where each position in each integer represents one selectable
 * part of a shape.
 * @param x
 */
case class LargeShapePart(x : Array[Int]) extends ShapePart

/**
 * A SmallShapePart is basically an Int where each part of the shape represents one position
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
case class SmallShapePart(x : Int) extends ShapePart {

  assert(x > 0, "The small shape part must be larger than zero")
  assert(x < 30, "The small shape part cannot be larger than 30")

}