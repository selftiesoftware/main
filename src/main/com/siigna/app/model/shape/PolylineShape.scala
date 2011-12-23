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
import com.siigna.util.dxf.DXFSection
import com.siigna.util.geom.{Geometry, Vector}
import collection.generic.{Subtractable, Addable}

/**
 * A PolylineShape is a shape that can consist of segments or arcs.
 * <b>Note:</b> PolylineShapes needs to consist of elements that's connected! If not, the PolylineShape is flagged as
 * malformed and the log outputs an error.
 * TODO: Do an apply(shapes : BasicShape*)..
 */
case class PolylineShape(shapes : Seq[BasicShape], attributes : Attributes) extends ImmutableShape with Subtractable[BasicShape, PolylineShape] {

  /**
   * Whether the PolylineShape is malformed (the shapes aren't connected).
   * TODO: What to do here? Just a flag? Or a MalformedPolylineShape object? Or...?
   * TODO: Just connect...?
   */
  assert(PolylineShape.isConnected(shapes), Log.error("The following PolylineShape is malformed: "+this))

  /**
   * Add a single shape to the polyline.
   */
  def +: (shape : BasicShape) = copy(shapes.+:(shape))

  /**
   * Add several shapes to the polyline.
   */
  def +: (shapes : BasicShape*) = copy(shapes.++:(shapes))

  /**
   * Remove a shape from the polyline.
   */
  def - (shape : BasicShape) = copy(shapes.filterNot(_ == shape))

  def geometry = Polyline(shapes.map(_.geometry))

  def repr = this

  def setAttributes(attr : Attributes) = copy(attributes = attr)

  // TODO: export polylines.
  def toDXF = DXFSection(List())

  def transform(transformation : TransformationMatrix) = copy(shapes.map(_.transform(transformation)))

}

object PolylineShape {


  def empty = new PolylineShape(Seq(), Attributes())

  def fromPoints(points : Vector*) : PolylineShape = fromPoints(points.toIterable)

  def fromPoints(points : Iterable[Vector]) : PolylineShape = {
    var lines = Seq[LineShape]()
    points.reduceLeft((a, b) => {
      lines :+= LineShape(a, b)
      b
    })
    PolylineShape(lines, Attributes())
  }

  def fromRectangle(rect : Rectangle) = fromPoints(rect.points :+ rect.topLeft)

  def isConnected(shapes : Seq[BasicShape]) : Boolean = if (!shapes.isEmpty) {
    var isConnected = true
    shapes.reduceLeft((a, b) => {
      if (a.end != b.start) {
        println(a.end, b.start)
        isConnected = false
      }
      b
    })
    isConnected
  } else true

}