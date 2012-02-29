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

import collection.parallel.immutable.ParHashMap
import shape.DynamicShape

/**
 * An immutable model containing [[com.siigna.app.model.shape.DynamicShape]]s.
 *
 * @tparam Model  The model to return whenever operations are performed on the DynamicModel.
 * @see [[com.siigna.app.model.Model]]
 */
trait DynamicModel[Model <: DynamicModel] {

  def deselect(id : Int) : Model

  def select(id : Int) : Model

}
