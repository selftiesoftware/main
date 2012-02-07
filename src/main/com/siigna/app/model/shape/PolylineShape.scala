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

import com.siigna._
import com.siigna.util.collection.Attributes
import com.siigna.util.dxf.DXFSection
import util.geom.PolylineGeometry

import polyline._

/**
 * A PolylineShape is a shape that can consist of segments or arcs.
 *
 * Available attributes:
 * <pre>
 *  - Color        Color   The color of the lines in the Polyline.
 *  - StrokeWidth  Double  The width of the linestroke used to draw.
 *  - Raster       Color   A color that fills out the PolylineShape. The fill is defined as the polygon given by the points
 *                    in the PolylineShape.
 * </pre>
 *
 * @param shapes  The inner shapes of the PolylineShape, basically a List implementation.
 * TODO: Do an apply(shapes : BasicShape*)..
 * TODO: Implement additions and subtractions
 */
case class PolylineShape(shapes : InnerPolylineShape, attributes : Attributes) extends ImmutableShape {

  type T = PolylineShape

  // TODO: Fix this
  def geometry = Rectangle2D.empty//if (shapes.isEmpty) Rectangle2D.empty else PolylineGeometry(shapes.map(_.geometry))

  def repr = this

  def setAttributes(attr : Attributes) = copy(attributes = attr)

  // TODO: export polylines.
  def toDXF = DXFSection(List())

  def transform(transformation : TransformationMatrix) = this //copy(shapes.map(_.transform(transformation)))

}

/**
 * A companion object to PolylineShape. Provides shortcuts to creations of PolylineShapes.
 */
object PolylineShape {

  /**
   * Creates an empty PolylineShape.
   */
  def empty = new PolylineShape(PolylineNil, Attributes())

  /**
   * Creates a PolylineShape from a number of points.
   *
   * @param points  The points to use.
   */
  def fromPoints(points : Vector2D*) : PolylineShape = fromPoints(points.toIterable)

  /**
   * Creates a PolylineShape from a collection of points.
   *
   * @param points  The collection of points to use
   * @param closed  A flag signalling whether to close the PolylineShape by adding the first point at the end. Defaults to false.
   */
  def fromPoints(points : Traversable[Vector2D], closed : Boolean = false) : PolylineShape = {
    var lines : InnerPolylineShape = PolylineNil
    points.foreach(p => lines = PolylineLineShape(p, lines))

    // Close the shape, if requested
    if (closed) lines = PolylineLineShape(points.last, lines)
      
    PolylineShape(lines, Attributes())
  }

  /**
   * Returns a PolylineShape with four lines, representing the given Rectangle.
   */
  def fromRectangle(rect : Rectangle2D) = fromPoints(rect.vertices :+ rect.vertices.head)

}