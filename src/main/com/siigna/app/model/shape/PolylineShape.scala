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

import com.siigna.util.geom._
import collection.mutable.BitSet
import com.siigna.util.collection.{Attributes}

/**
 * <p>A PolylineShape is a shape that can consist of segments or arcs. <b>Use the companion object
 * [[com.siigna.app.model.shape.PolylineShape]] to construct a Polylineshape!</b> The default
 * constructor uses a [[com.siigna.app.model.shape.PolylineShape#InnerPolylineShape]] to ensure
 * that data is not being duplicated in the long list of lines and/or arcs.</p>
 *
 * <p>PolylineShapes can by convention not contain two duplicate points. If any situation
 * should arise (transformation, addition etc.) that could result in two equal points, one
 * of them is removed.</p>
 *
 * <p>Available attributes:
 * <pre>
 *  - Color        Color   The color of the lines in the Polyline.
 *  - StrokeWidth  Double  The width of the linestroke used to draw.
 *  - Raster       Color   A color that fills out the PolylineShape. The fill is defined as the polygon given by the points
 *                         in the PolylineShape.
 * </pre></p>
 *
 * @param startPoint  The starting point of the PolylineShape.
 * @param innerShapes  The inner shapes of the PolylineShape, basically a seq of [[com.siigna.app.model.shape.PolylineShape#InnerPolylineShape]].
 * TODO: Do an apply(shapes : BasicShape*)..
 * TODO: Implement additions and subtractions
 * TODO: Implement more robust geometry for PolylineShapes
 */
sealed case class PolylineShape(startPoint : Vector2D, private val innerShapes : Seq[PolylineShape.InnerPolylineShape], attributes : Attributes) extends CollectionShape[BasicShape] {

  require(startPoint != null, "Cannot create a polyline without a starting point")
  require(!innerShapes.isEmpty, "Cannot create a polyline without shapes")

  type T = PolylineShape
  
  def apply(part : ShapeSelector) = part match {
    case FullSelector => Some(new PartialShape(transform))
    case CollectionSelector(xs) => {
      // Create a function that transforms the selected parts of the polyline
      val transformInner = (t : TransformationMatrix) => {
        val arr = new Array[PolylineShape.InnerPolylineShape](innerShapes.size)
        for (i <- 0 until innerShapes.size) {
          arr(i) = if (xs contains (i + 1)) {
            innerShapes(i).transform(t)
          } else innerShapes(i)
        }
        // Make sure there are no duplicate points
        arr.distinct
      }

      Some(new PartialShape((t : TransformationMatrix) =>
        PolylineShape(
          // Test if the start point is included (binary position 1)
          if (xs(0)) { startPoint.transform(t) } else { startPoint },
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
    case FullSelector => None
    case CollectionSelector(xs) => {
      val deleteStart = xs(0)

      if (deleteStart && xs.size == (innerShapes.size - 1)) { // Everything is selected!
        None
      } else if (xs.size == 0) { // Nothing is selected, carry on...
        Some(this)
      } else {
        // Otherwise we're somewhere between 0 and everything
        // First filter all the removed parts away - remember startPoint occupies the 0-position
        val inner = xs.-(0).map(i => innerShapes(i - 1)).toSeq

        if (deleteStart && inner.size > 1) { // Is the start point included? Then we need at least two shapes
          Some(PolylineShape(inner.head.point, inner.tail, attributes))
        } else if (inner.size > 0) { // Otherwise we need at least one inner shape
          Some(PolylineShape(startPoint, inner, attributes))
        } else Some(this) // Fair enough...
      }
    }
    case EmptySelector => Some(this)
  }

  def getPart(rect: Rectangle2D) =
    if (rect.contains(geometry.boundary)) {
      FullSelector
    } else if (rect.intersects(geometry.boundary)) {
      val set = BitSet()
      // Add the start point if it is inside the rectangle
      if (rect.contains(startPoint)) {
        set add 0
      }
      // Iterate inner shapes
      for (i <- 0 until innerShapes.size) {
        if (rect.contains(innerShapes(i).point)) {
          set add (i + 1) // Add one since we already included the startPoint (at index 0)
        }
      }
      CollectionSelector(set)
    } else EmptySelector

  def getPart(point: Vector2D) = { // TODO: Test this
    val set = BitSet()

    // Iterate the shapes to find the ones who matches
    for (i <- 0 until shapes.size) {
      shapes(i).getPart(point) match {
        case FullSelector => { // Include both numbers
          if (set.size <= 1) {
            set add i
            set add (i + 1)
          } else { // ... but only allow one segment to be selected as max
            set.find(n => shapes(n).distanceTo(point) < shapes(i).distanceTo(point)) match {
              case Some(n) => set remove n; set add i
              case None => // Don't add the new shape since it's further away
            }
          }
        }
        case LineShape.Selector(x) => {
          if (set.size > 1) { // Only allow one segment to be selected as max
            set.find(n => shapes(n).distanceTo(point) < shapes(i).distanceTo(point)) match {
              case Some(n) => set remove n; set add i; set add (i + 1)
              case None => // Don't add the new shape since it's further away
            }
          } else {
            set add (if (x) i else i + 1)
          }
        }
        case _ =>
      }
    }

    // Return
    if (set.size == shapes.size + 1) {
      FullSelector
    } else if (set.size > 0) {
      CollectionSelector(set)
    } else EmptySelector
  }

  def getVertices(selector: ShapeSelector) = selector match {
    case FullSelector => geometry.vertices
    case CollectionSelector(xs) => {
      var inner = Seq[Vector2D]()
      
      // Add startPoint
      if (xs(0)) { inner :+= startPoint }
      
      // Check all the binary positions for matches
      for (i <- 1 to innerShapes.size) {
        if (xs(i)) {
          inner :+= innerShapes(i - 1).point // Subtract one to account for the startPoint
        }
      }
      
      inner
    }
    case _ => Seq()
  }   

  def join(shape: BasicShape) = PolylineShape(startPoint, innerShapes :+ (shape match {
    case ArcShape(p, _, _, _, _) => new PolylineShape.PolylineLineShape(p) // TODO: Use PolylineArcShape!
    case LineShape(p1, p2, _) => new PolylineShape.PolylineLineShape(p2)
  }), attributes)

  def join(shapes: Traversable[BasicShape]) = null

  /**
   * The inner shapes the PolylineShape consists of in terms of regular
   * [[com.siigna.app.model.shape.Shape]]s.
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
  //def toDXF = DXFSection(List())

  override def toString = "PolylineShape[" + startPoint + "," + innerShapes + "]"

  def transform(t : TransformationMatrix) = PolylineShape(t.transform(startPoint), innerShapes.map(_.transform(t)).distinct, attributes)
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
  sealed case class PolylineLineShape(point : Vector2D) extends InnerPolylineShape {
    def apply(v : Vector2D) = LineShape(v, point)
    override def toString = "PolylineLineShape(" + point + ")"
    def transform(t : TransformationMatrix) = new PolylineLineShape(point.transform(t))
  }

  /**
   * An ArcShape representation used inside a PolylineShape.
   * @param middle  The center point of the arc
   * @param point The point given to create a LineShape.
   */
  sealed case class PolylineArcShape(middle : Vector2D, point : Vector2D) extends InnerPolylineShape {
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
  def apply(points : Vector2D*) : PolylineShape = apply(points.toIterable)

  /**
   * Creates a PolylineShape from a collection of points. If two points are equal in
   * the collection, one of them is filtered out to avoid overlapping points.
   *
   * @param points  The collection of points to use. Duplicates are removed.
   * @param closed  A flag signalling whether to close the PolylineShape by adding the first point at the end. Defaults to false.
   */
  def apply(points : Traversable[Vector2D], closed : Boolean = false) : PolylineShape = {
    val lines = points.tail.toSeq.distinct.map(p => new PolylineLineShape(p))

    PolylineShape(points.head,
                  // Close the shape, if requested
                  if (closed) lines :+ new PolylineLineShape(points.head) else lines,
                  Attributes())
  }

  /**
   * Returns a PolylineShape with four lines, representing the given Rectangle.
   */
  def apply(rect : Rectangle2D) : PolylineShape = apply(rect.vertices :+ rect.vertices.head)

}