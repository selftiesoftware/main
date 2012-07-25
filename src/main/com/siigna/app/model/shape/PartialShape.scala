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

import com.siigna.util.geom.{TransformationMatrix}

/**
 * A PartialShape allows for a shape to be partially manipulated.
 *
 * <br/>
 * The PartialShape is actually a function receiving a [[com.siigna.util.geom.TransformationMatrix]]
 * so it can apply the matrix on the selected parts of the shape.
 *
 * @see MutableModel
 * @see Select
 * @see Selection
 *
 * @param part  The part representing the sub-part of a shape.
 * @param f  The function that can transform the sub-part of the shape and leave the rest unchanged.
 */
class PartialShape(val part : Shape, f : (TransformationMatrix => Shape)) extends (TransformationMatrix => Shape) {

  /**
   * Transforms the PartialShape with a given matrix.
   *
   * @param t  A [[com.siigna.util.geom.TransformationMatrix]] to apply to the sub-selection of the containing shape.
   * @return  A partially transformed shape.
   */
  def apply(t : TransformationMatrix) = f(t)
  
}