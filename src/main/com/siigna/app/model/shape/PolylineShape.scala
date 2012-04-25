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
import com.siigna.util.collection.{Preferences, Attributes}
import com.siigna.util.geom._
import com.siigna.app.Siigna

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
case class PolylineShape(startPoint : Vector2D, private val innerShapes : Seq[PolylineShape.InnerPolylineShape], attributes : Attributes) extends Shape {

  type T = PolylineShape
  
  def apply(part : ShapePart) = part match {
    case FullShapePart => Some(new PartialShape(transform))
    case SmallShapePart(x) => {
      val includeStart = (x & 1) == 1
      var ids = Seq[Int]()
      for (i <- 0 until innerShapes.size) { // Check all the binary positions
        if ((2 << i & x) == (2 << i)) { // Remember to start from 2 since the startPoint occupies the first position
          ids :+= i
        }
      }

      val inner = innerShapes.filter(ids.contains(_))

      Some(new PartialShape((t : TransformationMatrix) => if (includeStart) {
        PolylineShape(startPoint.transform(t), inner.map(_.transform(t)), attributes)
      } else {
        PolylineShape(startPoint, inner.map(_.transform(t)), attributes)
      }))
    }
    case _ => None
  }
  
  def delete(part : ShapePart) = part match {
    case FullShapePart => None
    case SmallShapePart(x) => {
      val includeStart = (x & 1) == 1
      var ids = Seq[Int]()
      for (i <- 0 until innerShapes.size) { // Check all the binary positions
        if ((2 << i & x) == (2 << i)) { // Remember to start from 2 since the startPoint occupies the first position
          ids :+= i
        }
      }

      if (includeStart && ids.size == innerShapes.size) { // Everything is selected!
        Some(this)
      } else if (ids.size == 0) { // Oh dear, no points left!
        None
      } else { // Otherwise we're somewhere in between
        val inner = innerShapes.filter(ids.contains(_))
        if (includeStart) { // Is the start point included?
          Some(PolylineShape(startPoint, inner, attributes))
        } else if (ids.size == 1) { // No points left to use as start!
          None
        } else { // Phew!
          Some(PolylineShape(inner(0).point, inner.slice(1, inner.size), attributes))
        }
      }
    }
    case LargeShapePart(xs) => {
      // TODO: Write this
      None
    }
    case EmptyShapePart => Some(this)
  }

  def geometry = if (shapes.isEmpty) Rectangle2D.empty else CollectionGeometry(shapes.map(_.geometry))

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

  def select(rect: Rectangle2D) =
    if (rect.contains(geometry.boundary)) {
      FullShapePart
    } else if (rect.intersects(geometry.boundary)) {
      if (innerShapes.size < 30) {
        var x = if (rect.contains(startPoint)) 1 else 0
        for (i <- 0 until innerShapes.size) {
          if (rect.contains(innerShapes(i).point)) {
            x = x | (1 << (i + 2)) // Add two since we already included the startPoint (1) and innerShapes begins with index 1
          }
        }
        SmallShapePart(x)
      } else {
        // TODO: Write this
        EmptyShapePart
      }
    } else EmptyShapePart


  def select(point: Vector2D) = {
    /*val selectionDistance = Siigna.selectionDistance
    // Find the distance to the start point
    val startDistance = startPoint.distanceTo(point)

    // Find the id of the closest point
    var closestId = 0
    var closestDistance = innerShapes(0).point.distanceTo(point)
    for (x <- 1 until innerShapes.size) {
      val d = innerShapes(x).point.distanceTo(point)
      if (d < closestDistance) {
        closestDistance = d
        closestId = x
      }
    }

    println(selectionDistance, startDistance, closestDistance)
    // If the startDistance for the start point is below the selectionDistance, return
    if (startDistance <= selectionDistance) {
      Some((t : TransformationMatrix) => PolylineShape(startPoint.transform(t), innerShapes, attributes))
      
    // If the closestDistance for the closest point is below the selectionDistance, return
    } else if (closestDistance <= selectionDistance) {
      Some((t : TransformationMatrix) => PolylineShape(startPoint, innerShapes.updated(closestId, innerShapes(closestId).transform(t)), attributes))
    
    // Otherwise test the distances to previous and following line segments
    } else {
      def segmentDistance(p : Vector2D) = Segment(innerShapes(closestId) point, p).distanceTo(point)
      val dPrevious  = if (closestId > 0) segmentDistance(innerShapes(closestId - 1).point)
                       else segmentDistance(startPoint)
      val dFollowing = if (closestId < innerShapes.size - 1) segmentDistance(innerShapes(closestId + 1).point)
                       else java.lang.Double.POSITIVE_INFINITY
      
      // Find the closest point
      var closestToStart = false
      val closestShapeId = if (dPrevious <= dFollowing && dPrevious <= selectionDistance) {
        closestToStart = closestId == 0
        closestId - 1
      } else if (dFollowing < dPrevious && dFollowing <= selectionDistance) {
        closestId + 1
      } else -1
      
      // Create the selection
      if (closestShapeId >= 0 || closestToStart) {
        Some((t : TransformationMatrix) => {
          if (closestToStart) {
            PolylineShape(startPoint.transform(t), innerShapes.updated(closestId, innerShapes(closestId).transform(t)), attributes)
          } else {
            PolylineShape(startPoint, innerShapes.updated(closestId, innerShapes(closestId).transform(t))
                                                 .updated(closestShapeId, innerShapes(closestShapeId).transform(t)), attributes)
          }
        })
      } else None // Otherwise selection is empty
    }*/
    EmptyShapePart
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
  sealed trait InnerPolylineShape extends Serializable with (Vector2D => BasicShape) {

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
    override def toString = "PolylineLineShape(" + point + ")"
    def transform(t : TransformationMatrix) = new PolylineLineShape(point.transform(t))
  }

  /**
   * An ArcShape representation used inside a PolylineShape.
   * @param middle  The center point of the arc
   * @param point The point given to create a LineShape.
   */
  sealed class PolylineArcShape(val middle : Vector2D, val point : Vector2D) extends InnerPolylineShape {
    def apply(v : Vector2D) = ArcShape(v, middle, point)
    override def toString = "PolylineArcShape(" + middle + ", " + point + ")"
    def transform(t : TransformationMatrix) = new PolylineArcShape(middle.transform(t), point.transform(t))
  }

  /**
   * Creates an empty PolylineShape.
   */
  def empty = new PolylineShape(Vector2D.empty, Seq[InnerPolylineShape](), Attributes())

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