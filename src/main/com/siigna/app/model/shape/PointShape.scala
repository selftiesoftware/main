/*
 * Copyright (c) 2011. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.app.model.shape

import com.siigna._
import com.siigna.util.collection.Attributes
import com.siigna.util.dxf.{DXFSection, DXFValue}

/**
 * This class draws a simple 2-dimensional point.
 */
case class PointShape(point : Vector, attributes : Attributes) extends ImmutableShape
{

  val geometry = point

  val points = Iterable(point)

  def setAttributes(attributes : Attributes) = PointShape(point, attributes)

  def toDXF(value1 : Int = 10, value2 : Int = 20) = DXFSection(DXFValue(10, point.x), DXFValue(20, point.y))
  def toDXF : DXFSection = toDXF(10, 20)

  def transform(transformation : TransformationMatrix) : PointShape =
    PointShape(point transform(transformation), attributes)
}

object PointShape {
  def apply(point : Vector) = new PointShape(point, Attributes())
}