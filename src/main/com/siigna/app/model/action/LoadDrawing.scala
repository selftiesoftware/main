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

package com.siigna.app.model.action

import com.siigna.app.model.shape.Shape
import com.siigna.app.model.{Drawing, Model}
import com.siigna.util.collection.Attributes


/**
 * Loads a drawing from a given input.
 *
 * @param shapes  The shapes in the drawing.
 * @param actions  The actions that have been executed on the model.
 */
case class LoadDrawing(shapes : Map[Int, Shape], actions : Seq[Action], attributes : Attributes) extends VolatileAction {

  def execute(model : Model) = {
    // Store the attributes
    Drawing.setAttributes(attributes)

    // Store the shapes and the executed actions.
    new Model(shapes, actions, Seq())
  }
  
}
