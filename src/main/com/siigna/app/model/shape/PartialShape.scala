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
 * A PartialShape is an accessor for a shape through a given combination of the shape.
 */
trait PartialShape

/**
 * A SimplePartialShape is basically an Int where each part of the shape represents one position
 * in the binary system. It can thus be identified which parts of the shape are selected and which
 * are not. <b>The specific implementation varies for each shape</b>, but the standard is to use numbers
 * <i>0 to length - 1</i> to indicate which part has been selected and -1 for nothing at all.
 *
 * Note: For shapes with selectable parts > 30 a ComplexPartialShape is needed since Int only supports 30
 * positions of positive numbers.
 * TODO: Use negative bits as well.
 *
 * @param x The Int signalling which parts of the shape has been selected.
 */
case class SimplePartialShape(x : Int) extends PartialShape

/**
 * A ComplexPartialShape is a set of Ints where each position in each integer represents one selectable
 * part of a shape.
 * @param x
 */
case class ComplexPartialShape(x : Array[Int]) extends PartialShape