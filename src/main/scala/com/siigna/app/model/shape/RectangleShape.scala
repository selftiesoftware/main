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

package com.siigna.app.model.shape

import com.siigna.util.geom.{SimpleRectangle2D, Vector2D, TransformationMatrix, ComplexRectangle2D}
import com.siigna.util.collection.Attributes
import com.siigna.app.model.selection.ShapeSelector

case class RectangleShape(center : Vector2D, width : Double, height : Double, rotation : Double, val attributes : Attributes) extends ClosedShape {

  type T = RectangleShape

  val geometry = ComplexRectangle2D(center,width,height,rotation)

  def delete(selector: _root_.com.siigna.app.model.selection.ShapeSelector): scala.Seq[_root_.com.siigna.app.model.shape.Shape] = throw new UnsupportedOperationException("Not yet implemented")

  def getPart(selector: ShapeSelector): Option[PartialShape] = throw new UnsupportedOperationException("Not yet implemented")

  def getSelector(point: Vector2D): ShapeSelector = throw new UnsupportedOperationException("Not yet implemented")

  def getSelector(rect: SimpleRectangle2D): ShapeSelector = throw new UnsupportedOperationException("Not yet implemented")

  def getShape(selector: ShapeSelector): Option[Shape] = throw new UnsupportedOperationException("Not yet implemented")

  def getVertices(selector: ShapeSelector): Seq[Vector2D] = throw new UnsupportedOperationException("Not yet implemented")

  def setAttributes(attributes: Attributes): RectangleShape#T = throw new UnsupportedOperationException("Not yet implemented")

  def transform(transformation: TransformationMatrix): RectangleShape#T = this

}
