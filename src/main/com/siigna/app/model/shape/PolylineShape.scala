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
import collection.generic.{Subtractable, Addable}
import util.geom.{PolylineGeometry, Geometry, Vector}

/**
 * A PolylineShape is a shape that can consist of segments or arcs.
 *
 * Available attributes:
 * <pre>
 *  - Color   Color   The color of the lines in the Polyline.
 *  - Raster  Color   A color that fills out the PolylineShape. The fill is defined as the polygon given by the points
 *                    in the PolylineShape.
 * </pre>
 * TODO: Do an apply(shapes : BasicShape*)..
 * TODO: Rewrite into a seq of points instead and optimize...
 */
case class PolylineShape(shapes : Seq[BasicShape], attributes : Attributes) extends ImmutableShape with Subtractable[BasicShape, PolylineShape] {

  type T = PolylineShape

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

  // TODO: Fix this
  def geometry = if (shapes.isEmpty) Rectangle2D.empty else PolylineGeometry(shapes.map(_.geometry))

  def repr = this

  def setAttributes(attr : Attributes) = copy(attributes = attr)

  // TODO: export polylines.
  def toDXF = DXFSection(List())

  def transform(transformation : TransformationMatrix) = copy(shapes.map(_.transform(transformation)))

}

/**
 * A companion object to PolylineShape. Provides shortcuts to creations of PolylineShapes.
 */
object PolylineShape {

  /**
   * Creates an empty PolylineShape.
   */
  def empty = new PolylineShape(Seq(), Attributes())

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
    var lines = Seq[LineShape]()
    points.reduceLeft((a, b) => {
      lines :+= LineShape(a, b)
      b
    })

    // Close the shape, if requested
    if (closed) lines :+= LineShape(points.last, points.head)
      
    PolylineShape(lines, Attributes())
  }

  /**
   * Returns a PolylineShape with four lines, representing the given Rectangle.
   */
  def fromRectangle(rect : Rectangle2D) = fromPoints(rect.vertices :+ rect.vertices.head)

}