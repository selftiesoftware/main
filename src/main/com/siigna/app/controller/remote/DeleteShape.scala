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

package com.siigna.app.controller.remote

/**
 * Deletes a shape. The old shape is required, since if it is a polyline-shape, the subshapes need to be deleted too.
 */
import com.siigna.app.model.shape.Shape
import com.siigna.app.controller.Client

case class DeleteShape (drawingId : Int, shapeId : Int, shape : Shape, client : Client) extends RemoteCommand