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

import com.siigna.util.dxf.DXFSection
import com.siigna.util.collection.{Attributes}
import com.siigna.util.geom._

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
 * TODO: Implement more robust geometry for PolylineShapes
 */
case class PolylineShape(startPoint : Vector2D, private val innerShapes : Seq[PolylineShape.InnerPolylineShape], attributes : Attributes) extends Shape {

  type T = PolylineShape
  
  def apply(part : ShapeSelector) = part match {
    case FullShapeSelector => Some(new PartialShape(transform))
    case SmallShapeSelector(x) => {
      // Create a function that transforms the selected parts of the polyline
      val transformInner = (t : TransformationMatrix) => {
        val arr = new Array[PolylineShape.InnerPolylineShape](innerShapes.size)
        for (i <- 0 until innerShapes.size) { // Check all the binary positions
          arr(i) = (if (((2 << i) & x) == (2 << i)) { // Remember to start from 2 since the startPoint occupies the first position
            innerShapes(i).transform(t)
          } else innerShapes(i))
        }
        arr
      }

      Some(new PartialShape((t : TransformationMatrix) =>
        PolylineShape(
          // Test if the start point is included (binary position 1)
          if ((x & 1) == 1) { startPoint.transform(t) } else { startPoint },
          // Transform the inner shapes that are a part of the selection
          transformInner(t),
          // Forward the attributes as is
          attributes
        )
      ))
    }
    case _ => None
  }
  
  def delete(part : ShapeSelector) = part match {
    case FullShapeSelector => None
    case SmallShapeSelector(x) => {
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
      } else { // Otherwise we're somewhere between 0 and everything
        // Filter all the removed parts away
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
    case LargeShapeSelector(xs) => {
      // TODO: Write this
      None
    }
    case EmptyShapeSelector => Some(this)
  }

  def geometry = if (shapes.isEmpty) Rectangle2D.empty else CollectionGeometry(shapes.map(_.geometry))

  def getPart(rect: Rectangle2D) =
    if (rect.contains(geometry.boundary)) {
      FullShapeSelector
    } else if (rect.intersects(geometry.boundary)) {
      if (innerShapes.size < 30) {
        var x = if (rect.contains(startPoint)) 1 else 0
        for (i <- 0 until innerShapes.size) {
          if (rect.contains(innerShapes(i).point)) {
            x = x | (1 << (i + 2)) // Add two since we already included the startPoint (1) and innerShapes begins with index 1
          }
        }
        SmallShapeSelector(x)
      } else {
        EmptyShapeSelector // TODO: Write this
      }
    } else EmptyShapeSelector


  def getPart(point: Vector2D) = {
    if (innerShapes.size < 30) {
      // Set the x-value and test the first shape (startPoint included)
      var x = shapes(0).getPart(point) match {
        case FullShapeSelector => 3 // Point one and two = three
        case SmallShapeSelector(p) => p
        case _ => 0
      }

      // Iterate the shapes to find the ones who matches
      for (i <- 1 until shapes.size) {
        shapes(i).getPart(point) match {
          case FullShapeSelector => x = x | ((1 << i) + (2 << i)) // Include both numbers
          case SmallShapeSelector(p) => x = x | (p << i)
          case _ =>
        }
      }

      // Return
      if (x > 0) {
        SmallShapeSelector(x)
      } else EmptyShapeSelector
    } else EmptyShapeSelector // TODO: Write this
  }

  def getVertices(selector: ShapeSelector) = selector match {
    case FullShapeSelector => geometry.vertices
    case SmallShapeSelector(x) => {
      var inner = Seq[Vector2D]()
      
      // Add startPoint
      if ((x & 1) == 1) { inner :+= startPoint }
      
      // Check all the binary positions for matches
      for (i <- 0 until innerShapes.size) { 
        if (((2 << i) & x) == (2 << i)) { // Remember to start from 2 since the startPoint occupies the first position
          inner :+= innerShapes(i).point
        }
      }
      
      inner
    }
    case _ => Seq()
  }

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

  def setAttributes(attr : Attributes) = copy(attributes = attr)

  // TODO: export polylines.
  def toDXF = DXFSection(List())

  override def toString = "PolylineShape[" + startPoint + "," + innerShapes + "]"

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