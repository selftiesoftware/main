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

import collection.immutable.Vector

import com.siigna.util.dxf.DXFSection
import com.siigna.util.geom.{CollectionGeometry, TransformationMatrix, Rectangle2D, Vector2D}
import com.siigna.util.collection.{Preferences, Attributes}

/**
 * A PolylineShape is a shape that can consist of segments or arcs. <b>Use the companion object
 * [[com.siigna.app.model.shape.PolylineShape]] to construct a Polylineshape!</b> The default
 * constructor uses a [[com.siigna.app.model.shape.PolylineShape#InnerPolylineShape]] to ensure
 * that data is not being duplicated in the long list of lines and/or arcs.
 *
 * Available attributes:
 * <pre>
 *  - Color        Color   The color of the lines in the Polyline.
 *  - StrokeWidth  Double  The width of the linestroke used to draw.
 *  - Raster       Color   A color that fills out the PolylineShape. The fill is defined as the polygon given by the points
 *                    in the PolylineShape.
 * </pre>
 *
 * @param startPoint  The starting point of the PolylineShape.
 * @param innerShapes  The inner shapes of the PolylineShape, basically a seq of [[com.siigna.app.model.shape.PolylineShape#InnerPolylineShape]].
 * TODO: Do an apply(shapes : BasicShape*)..
 * TODO: Implement additions and subtractions
 */
case class PolylineShape(startPoint : Vector2D, private val innerShapes : Seq[PolylineShape.InnerPolylineShape], attributes : Attributes) extends ImmutableShape {

  type T = PolylineShape

  /**
   * The shapes inside the PolylineShape
   */
  def shapes : Seq[BasicShape] = if (!innerShapes.isEmpty) {
    val tmp = new Array[BasicShape](innerShapes.size)
    tmp(0) = innerShapes.head.apply(startPoint)
    for (i <- 1 until innerShapes.size) {
      tmp(i) = innerShapes(i).apply(innerShapes(i - 1).point)
    }
    tmp
  } else Seq[BasicShape]()

  def geometry = if (shapes.isEmpty) Rectangle2D.empty else CollectionGeometry(shapes.map(_.geometry))

  def select(rect: Rectangle2D) =
    if (rect.contains(geometry.boundary)) {
      Some(select())
    } else if (rect.intersects(geometry.boundary)) {
      val touched = innerShapes.filter(s => rect.contains(s.point))
      Some((t : TransformationMatrix) => {
        val start = if (rect.contains(startPoint)) startPoint.transform(t) else startPoint
        val innerShapes = touched.map(_.transform(t))
        new PolylineShape(start, innerShapes, attributes)
      })
    } else None


  def select(point: Vector2D) = {
    val dSelect = Preferences.double("selectionDistance")
    if (geometry.distanceTo(point) < dSelect) {
      // Find the id of the closest shape
      var closestId = 0
      var dClosest = innerShapes(0).point.distanceTo(point)
      for (x <- 1 until innerShapes.size) {
        val d = innerShapes(x).point.distanceTo(point)
        if (dClosest < d) {
          dClosest = d
          closestId = x
        }
      }
      
      // Create the dynamic shape
      val dStart = startPoint.distanceTo(point)
      if (dClosest < dSelect || dStart < dSelect) {
        Some((t : TransformationMatrix) => {
          if (dClosest < dStart) {
            val updated = innerShapes(closestId).transform(t)
            PolylineShape(startPoint, innerShapes.updated(closestId, updated), attributes)
          } else {
            PolylineShape(startPoint.transform(t), innerShapes, attributes)
          }
        })
        // Return the option... phew...
      } else None
    } else None
  }

  def setAttributes(attr : Attributes) = copy(attributes = attr)

  // TODO: export polylines.
  def toDXF = DXFSection(List())

  def transform(t : TransformationMatrix) = new PolylineShape(t.transform(startPoint), innerShapes.map(_.transform(t)), attributes)
}

/**
 * A companion object to PolylineShape. Provides shortcuts to creations of PolylineShapes.
 */
object PolylineShape {

  /**
   * A shape type used in the PolylineShape. This shape is instantiated by a given point,
   * so we (1) ensure that the shapes are connected and (2) avoids any duplicated points.
   */
  sealed trait InnerPolylineShape extends Function[Vector2D, BasicShape] {

    /**
     * Creates a BasicShape to use inside the PolylineShape.
     * @param v  The vector with which the BasicShape is instantiated.
     */
    def apply(v : Vector2D) : BasicShape

    /**
     * The only point the InnerPolylineShape knows for certain.
     */
    def point : Vector2D

    /**
     * Transforms the InnerPolylineShape with the given [[com.siigna.util.geom.TransformationMatrix]].
     */
    def transform(t : TransformationMatrix) : InnerPolylineShape

  }

  /**
   * A LineShape representation used inside a PolylineShape.
   * @param point  The point given to create a LineShape.
   */
  sealed class PolylineLineShape(val point : Vector2D) extends InnerPolylineShape {
    def apply(v : Vector2D) = LineShape(v, point)
    def transform(t : TransformationMatrix) = new PolylineLineShape(point.transform(t))
  }

  /**
   * An ArcShape representation used inside a PolylineShape.
   * @param middle  The center point of the arc
   * @param point The point given to create a LineShape.
   */
  sealed class PolylineArcShape(val middle : Vector2D, val point : Vector2D) extends InnerPolylineShape {
    def apply(v : Vector2D) = ArcShape(v, middle, point)
    def transform(t : TransformationMatrix) = new PolylineArcShape(middle.transform(t), point.transform(t))
  }

  /**
   * Creates an empty PolylineShape.
   */
  def empty = new PolylineShape(Vector2D.empty, Vector[InnerPolylineShape](), Attributes())

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
    val startPoint = points.head
    var lines = points.tail.toSeq.map(p => new PolylineLineShape(p))

    // Close the shape, if requested
    if (closed) lines = lines :+ new PolylineLineShape(startPoint) else lines

    PolylineShape(startPoint, lines, Attributes())
  }

  /**
   * Returns a PolylineShape with four lines, representing the given Rectangle.
   */
  def fromRectangle(rect : Rectangle2D) = fromPoints(rect.vertices :+ rect.vertices.head)

}