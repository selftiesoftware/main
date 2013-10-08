/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */
package com.siigna.app.model.shape

import com.siigna.util.geom._
import com.siigna.util.collection.Attributes
import collection.immutable.BitSet
import com.siigna.app.Siigna
import com.siigna.app.model.selection.{BitSetShapeSelector, FullShapeSelector, EmptyShapeSelector, ShapeSelector}

/**
 * <p>
 * A PolylineShape is a shape that can consist of segments or arcs. The class PolylineShape is a trait that covers
 * polylines that are either closed or not closed.
 * </p>
 *
 * <p>
 * The default constructor uses a [[com.siigna.app.model.shape.PolylineShape# I n n e r P o l y l i n e S h a p e]] to ensure
 * that data is not being duplicated in the long list of lines and/or arcs. The <code>shapes</code> </p>
 * </p>
 *
 * <p>
 * PolylineShapes can by convention not contain two duplicate points. If any situation
 * should arise (transformation, addition etc.) that could result in two equal points, one
 * of them is removed.
 * </p>
 *
 * @define polylineAttributes
 *         <p>Available attributes:
 *         <pre>
 *         - Color        Color   The color of the lines in the Polyline.
 *         - StrokeWidth  Double  The width of the linestroke used to draw.
 *         - Raster       Color   A color that fills out the PolylineShape. The fill is defined as the polygon given by the points
 *         in the PolylineShape.
 *         </pre></p>
 */
trait PolylineShape extends CollectionShape[BasicShape] {

  type T = PolylineShape

  /**
   * Copies a type T < PolylineShape into another type T with the given parameter(s) changes, keeping
   * the same attributes as this object.
   * @param startPoint  The new start point
   * @param innerShapes  The new inner shapes
   * @param attributes  The new inner shapes
   * @return  A new instance of type T < PolylineShape
   */
  protected def copy(startPoint: Vector2D = startPoint, innerShapes: Seq[InnerPolylineShape] = innerShapes, attributes: Attributes = attributes): T

  def getPart(part: ShapeSelector) = part match {
    case FullShapeSelector => Some(new PartialShape(this, transform))
    case BitSetShapeSelector(xs) => {
      // The selected parts, needed for drawing
      var selected = Seq[BasicShape]()

      // Create a function that transforms the selected parts of the polyline
      val transformInner = (t: TransformationMatrix) => {
        val arr = collection.mutable.ArrayBuffer[InnerPolylineShape]()
        for (i <- 0 until innerShapes.size) {
          val shape = if (xs contains (i + 1)) {
            selected = selected :+ shapes(i)
            innerShapes(i).transform(t)
          } else innerShapes(i)
          // Make sure there are no duplicate neighbour points
          if (!arr.isDefinedAt(i - 1) || arr(i - 1) != shape) arr += shape
        }

        arr
      }

      // Create a shape used for drawing
      val drawShape = GroupShape(selected)

      Some(new PartialShape(drawShape, (t: TransformationMatrix) =>
        copy(
          // Test if the start point is included (binary position 0)
          if (xs(0)) {
            startPoint.transform(t)
          } else {
            startPoint
          },
          // Transform the inner shapes that are a part of the selection
          transformInner(t),
          // Forward the attributes as is
          attributes
        )
      ))
    }
    case _ => None
  }

  def getSelector(rect: SimpleRectangle2D) =
    if (rect.contains(geometry.boundary)) {
      FullShapeSelector
    } else if (rect.intersects(geometry)) {
      var set = BitSet()
      // Add the start point if it is inside the rectangle
      if (rect.contains(startPoint)) {
        set += 0
      }
      // Iterate inner shapes
      for (i <- 0 until innerShapes.size) {
        if (rect.contains(innerShapes(i).point)) {
          set += (i + 1) // Add one since we already included the startPoint (at index 0)
        }
      }
      BitSetShapeSelector(set)
    } else EmptyShapeSelector

  def getSelector(point: Vector2D) = {
    // Find the distance to all the points and get their index
    val points = innerShapes.map(_.point.distanceTo(point)).+:(startPoint.distanceTo(point)).zipWithIndex
    // Find the points that are within the selection distance
    val closeVertices = points.filter(t => t._1 <= Siigna.selectionDistance).map(_._2)

    // If only one point is close, then we return a single index (point)
    if (closeVertices.size == 1) {
      BitSetShapeSelector(BitSet(closeVertices.head))
    } else {
      // If there are zero or several close points, we should check for selection of segments
      val closeShapes = shapes.zipWithIndex.map(t => t._1.distanceTo(point) -> t._2).filter(_._1 <= Siigna.selectionDistance)

      // If no shapes are close, nothing is selected
      if (closeShapes.isEmpty) {
        EmptyShapeSelector
      } else {
        // Otherwise we add the vertices of the close shapes
        var closeShapeVertices = BitSet()
        val isClosed = isInstanceOf[PolylineShape.PolylineShapeClosed]
        val isCloseToBothEndPoints = points.head._1 < Siigna.selectionDistance && points.last._1 < Siigna.selectionDistance

        // Fetch the shapes
        val xs = if (closeShapes.size <= 2) {
          closeShapes
        } else {
          closeShapes.sortBy(t => t._1).take(1)
        }

        // Avoid cases where the first and last segment are selected if both endpoints in an open PL is close
        if (!isClosed && isCloseToBothEndPoints)
          closeShapeVertices +=(0, points.last._2)
        else {
          xs.foreach(t => {
            closeShapeVertices += t._2

            // Make sure not to duplicate points in a closed polylineShape
            closeShapeVertices += (
              if (isClosed && t._2 == innerShapes.size) 0
              else t._2 + 1
              )
          })
        }

        // Lastly we return
        BitSetShapeSelector(closeShapeVertices)
      }
    }
  }

  def getShape(s: ShapeSelector) = s match {
    case FullShapeSelector => Some(this)
    case BitSetShapeSelector(xs) => {
      if (xs.size < 2) None
      else {
        var firstPoint = false
        var parts = Seq[InnerPolylineShape]()
        var lastIndex = xs.head // Last known index
        var isConsistent = true // Is the selection consistent?

        // Circular selection: Selection spanning between the first and last point of the PL
        var isUsingTail = false // Are we currently adding points to the circular tail?
        var circularTail = Seq[InnerPolylineShape]() // The circular "tail" or points we need to append to the selection at the end
        // Is the selection circular? (selects first and last point)
        val isCircular = xs(0) && xs(innerShapes.size) && isInstanceOf[PolylineShape.PolylineShapeClosed]

        // Iterate the parts
        xs foreach (i => {
          // Is the current point equal to the previous + 1?
          val isSuccessor = i == xs.head || lastIndex == i - 1

          // Examine if the first and last point have been selected
          if (i == 0 && isCircular) {
            parts :+= PolylineLineShape(startPoint)
          } else if ((!isSuccessor && isCircular) || isUsingTail) {
            circularTail :+= innerShapes(i - 1)
            isUsingTail = true
            // See if two adjacent elements are selected
          } else if (xs(i + 1) || xs(i - 1)) {
            // Includes shapes if they are not already there
            if (i != 0) {
              parts :+= innerShapes(i - 1)
              // Include the start point if s is the head element
            } else if (!isCircular) {
              firstPoint = true
            }
          }

          // If two indices are not next to one another, the polyline is not consistent
          if (!isSuccessor && !isCircular) {
            isConsistent = false
          }

          // Set the isFirstSet variable depending on whether the previous part is included
          lastIndex = i
        })

        // Add the circular "tail", if any
        if (isCircular) parts = circularTail ++ parts

        // Examine whether the first point should be included and whether the result is coherent
        (firstPoint, isConsistent) match {
          case (true, true) => Some(PolylineShape.PolylineShapeOpen(startPoint, parts, attributes))
          case (true, false) => Some(GroupShape(PolylineShape.PolylineShapeOpen(startPoint, parts, attributes)))
          case (false, true) if parts.size > 1 => Some(PolylineShape.PolylineShapeOpen(parts.head.point, parts.tail, attributes))
          case (false, false) if parts.size > 1 => Some(GroupShape(shapes(parts.head.point, parts.tail), attributes))
          case _ => None
        }
      }
    }
    case _ => None
  }

  def getVertices(selector: ShapeSelector) = selector match {
    case FullShapeSelector => geometry.vertices
    case BitSetShapeSelector(xs) => {
      var inner = Seq[Vector2D]()

      // Add startPoint
      if (xs(0)) {
        inner :+= startPoint
      }

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

  /**
   * The inner shapes of the polyline. Inner shapes have been constructed especially for the PolylineShape
   * to reduce redundancy, since points in the PolylineShape does not exist in itself. Only shapes (arcs or lines)
   * do, which both have more than one point, and at least one of them is shared with the next shape in the Polyline.
   * Thw two types of InnerPolylineShapes are: Arcs and Lines.
   * @return  A sequence of [[com.siigna.app.model.shape.InnerPolylineShape]]s.
   */
  def innerShapes: Seq[InnerPolylineShape]

  def join(shape: BasicShape) = copy(startPoint, innerShapes :+ (shape match {
    case ArcShape(p, _, _, _, _) => new PolylineLineShape(p) // TODO: Use PolylineArcShape!
    case LineShape(p1, p2, _) => new PolylineLineShape(p2)
  }), attributes)

  def join(shapes: Traversable[BasicShape]) = null

  /**
   * The shapes inside the PolylineShape as regular [[com.siigna.app.model.shape.LineShape]]s and
   * [[com.siigna.app.model.shape.ArcShape]]s.
   * @return A number of [[com.siigna.app.model.shape.Shape]]s.
   */
  def shapes: Seq[BasicShape] = shapes(startPoint, innerShapes)

  /**
   * Retrieves actual BasicShape-types from a given point along with a sequence of InnerPolylineShapes.
   * @param point  The starting point, to base the collection on.
   * @param inner  A sequence of InnerPolylineShapes
   * @return A collection of BasicShapes.
   */
  protected def shapes(point: Vector2D, inner: Seq[InnerPolylineShape]): Seq[BasicShape]

  /**
   * The start point for the polyline shape.
   * @return  A Vector2D indicating the first point in the PolylineShape.
   */
  def startPoint: Vector2D

  def transform(t: TransformationMatrix) = copy(t.transform(startPoint), innerShapes.map(_.transform(t)), attributes)

  override def toString() = "PolylineShape[" + startPoint + "," + innerShapes + ", " + attributes + "]"

}

/**
 * A companion object to PolylineShape. Provides shortcuts to creations of PolylineShapes.
 */
object PolylineShape {

  /**
   * A PolylineShape that is closed, i. e. where start and end points are the same.
   *
   * $polylineAttributes
   *
   * @param startPoint  The starting point of the PolylineShape.
   * @param innerShapes  The inner shapes of the PolylineShape, basically a seq of [[com.siigna.app.model.shape.PolylineShape# I n n e r P o l y l i n e S h a p e]].
   * @param attributes  The attributes to give the PolylineShape
   */
  // TODO extend ClosedShape
  case class PolylineShapeClosed(startPoint: Vector2D, innerShapes: Seq[InnerPolylineShape], attributes: Attributes)
    extends PolylineShape {

    protected def copy(startPoint: Vector2D, innerShapes: Seq[InnerPolylineShape], attributes: Attributes) =
      PolylineShapeClosed(startPoint, innerShapes, attributes)

    def delete(part: ShapeSelector) = {
      // Deletes a number of vertices starting at index, while replacing with the given insert
      def deleteVertices(index: Int, number: Int, insert: Option[InnerPolylineShape]) = {
        val xs = innerShapes.splitAt(index)
        val ys = xs._2.drop(number)
        insert.map(ys :+ _).getOrElse(ys) ++ xs._1
      }

      part match {
        case FullShapeSelector => Nil
        case BitSetShapeSelector(xs) => {
          // Find any continuous groups
          def findCoherentVerticesFrom(bits: BitSet, i: Int): Seq[BitSet] = {
            val group = bits.tail.takeWhile(i => bits(i - 1)) + bits.head
            if (group.isEmpty || group.last >= bits.last) Seq(group)
            else Seq(group) ++ findCoherentVerticesFrom(bits.drop(group.size), group.last)
          }

          // Test if the given bitset is continuous
          def testContinuous(bits: BitSet, current: Int, previous: Int): Boolean = {
            bits.last < current || (current == previous + 1 && testContinuous(bits, current + 1, current))
          }

          // Examine if the PL is continuous
          val isContinuous = xs.size == 1 || testContinuous(xs, xs.tail.head, xs.head)
          if (isContinuous) {
            xs.head match {
              case 0 if innerShapes.size > xs.size => {
                val vertices = deleteVertices(xs.head - 1, xs.size - 1, None)
                Seq(PolylineShape(vertices.head.point, vertices.tail, attributes))
              }
              case _ if innerShapes.size > xs.size => {
                val verticesWithStartPoint = deleteVertices(xs.head - 1, xs.size, Some(PolylineLineShape(startPoint)))
                Seq(PolylineShape(verticesWithStartPoint.head.point, verticesWithStartPoint.tail, attributes))
              }
              case _ => Nil
            }
            // Include cases where the last and first points are included
          } else if (xs(0) && xs.last == size - 1) {
            val indices = innerShapes.indices.map(_ + 1)
            val coherentBits = indices.diff(findCoherentVerticesFrom(xs, 0).flatten)
            if (coherentBits.size > 1) {
              val vertices = coherentBits.map(i => innerShapes(i - 1))
              Seq(PolylineShape(vertices.head.point, vertices.tail, attributes))
            } else Seq()
            // If the PL is not continuous we need to search for subgroups
          } else {
            Nil
          }
        }
        case EmptyShapeSelector => Seq(this)
      }
    }

    protected def shapes(point: Vector2D, inner: Seq[InnerPolylineShape]): Seq[BasicShape] = {
      if (!inner.isEmpty) {
        val tmp = new Array[BasicShape](inner.size + 1) // plus one to close the shape
        tmp(0) = inner.head.apply(point)
        for (i <- 1 until inner.size) {
          tmp(i) = inner(i).apply(inner(i - 1).point)
        }
        // Adds a final connection from the first to the last point
        tmp(inner.size) = LineShape(inner.last.point, point)
        tmp
      } else Nil
    }

    def setAttributes(attr: Attributes) = copy(attributes = attr)

    override def toString() = "PolylineShapeClosed[" + startPoint + "," + innerShapes + ", " + attributes + "]"

  }

  /**
   * A PolylineShape that is not closed.
   *
   * $polylineAttributes
   *
   * @param startPoint  The starting point of the PolylineShape.
   * @param innerShapes  The inner shapes of the PolylineShape, basically a seq of
   *                     [[com.siigna.app.model.shape.PolylineShape# I n n e r P o l y l i n e S h a p e]].
   * @param attributes  The attributes to give the PolylineShape
   */
  case class PolylineShapeOpen(startPoint: Vector2D, innerShapes: Seq[InnerPolylineShape], attributes: Attributes)
    extends PolylineShape {

    protected def copy(startPoint: Vector2D, innerShapes: Seq[InnerPolylineShape], attributes: Attributes): PolylineShape =
      PolylineShapeOpen(startPoint, innerShapes, attributes)

    def delete(part: ShapeSelector) = {
      part match {
        case FullShapeSelector => Nil
        case BitSetShapeSelector(xs) => {

          if (xs(0) && xs.size == (innerShapes.size + 1)) {
            // Everything is selected!
            Nil
          } else if (xs.size == 0) {
            // Nothing is selected, carry on...
            Seq(this)
          } else {
            // Find groups in the selection and return them
            var groups = Seq[PolylineShape]() // The sequence of groups
            var low = 0 // The value for the lower bound of the current group
            xs.-(0) foreach {
              x =>
              // Subtract one since we're not dealing with the startPoint
                val i = x - 1
                // Split the polylineshape at the given index and store the remainder if non-empty
                //  - add the first point (inner shape) to the end if the PL is closed
                val group = innerShapes.slice(low, i)
                // Create the group if
                if (group.size > 0 && low == 0 && !xs(0)) {
                  // Size == 1 but start point included
                  groups = groups :+ PolylineShape(startPoint, group, attributes)
                } else if (group.size > 1) {
                  // Size > 2 and no start point
                  groups = groups :+ PolylineShape(group.head.point, group.tail, attributes)
                }

                // Update the lowest bound for the next group
                low = i + 1
            }

            // Add the remaining tail elements (if there are more than 2)
            if (low < innerShapes.size - 1) {
              val elems = innerShapes.drop(low)
              groups = groups :+ PolylineShape(elems.head.point, elems.tail, attributes)
            }

            groups
          }
        }
        case EmptyShapeSelector => Seq(this)
      }
    }

    protected def shapes(point: Vector2D, inner: Seq[InnerPolylineShape]): Seq[BasicShape] = {
      if (!inner.isEmpty) {
        val tmp = new Array[BasicShape](inner.size)
        tmp(0) = inner.head.apply(point)
        for (i <- 1 until inner.size) {
          tmp(i) = inner(i).apply(inner(i - 1).point)
        }
        tmp
      } else Nil
    }

    def setAttributes(attr: Attributes) = copy(attributes = attr)

    override def toString() = "PolylineShapeOpen[" + startPoint + "," + innerShapes + ", " + attributes + "]"

  }

  /**
   * Creates a PolylineShape connecting the given points with lines. If the first and last point are the same the
   * polyline is closed ([[com.siigna.app.model.shape.PolylineShape.PolylineShapeClosed]]).
   *
   * @param points  The points to use.
   */
  def apply(points: Vector2D*): PolylineShape = apply(points.toTraversable)

  /**
   * Creates a PolylineShape from a collection of points. If the first and last points are equal in
   * the collection, we close the polyline.
   *
   * @param points  The collection of points to use.
   * @return  A PolylineShape connecting the given points with lines
   */
  def apply(points: Traversable[Vector2D]): PolylineShape = {
    if (points.size < 2) throw new IllegalArgumentException("Cannot create polyline from less than 2 points.")
    else {
      val lines = distinctNeighbour(points.toList).map(p => new PolylineLineShape(p))

      // Close the shape, if requested. The points are rounded to avoid non-significant rounding errors.
      if (points.head == points.last)
        PolylineShapeClosed(points.head, lines.tail.take(lines.size - 2), Attributes())
      else
        PolylineShapeOpen(points.head, lines.tail, Attributes())
    }
  }

  /**
   * Returns a closed PolylineShape with four lines, representing the given Rectangle.
   * @param rect  The [[com.siigna.util.geom.SimpleRectangle2D]] to construct the Polyline from.
   */
  def apply(rect: Rectangle2D): PolylineShape = apply(rect.vertices :+ rect.vertices.head)

  /**
   * Constructs a PolylineShape from regular lines and arcs. A polyline requires that the shapes are connected, so
   * if one or more shape are found not to be connected, we create one or more PolylineShape. If the shapes are
   * closed (that is surrounds a closed space) we create an instance of
   * [[com.siigna.app.model.shape.PolylineShape.PolylineShapeClosed]],
   * if not we create an instance of [[com.siigna.app.model.shape.PolylineShape.PolylineShapeOpen]].
   * @ param shapes  The shapes to create a polyline from.
   * @return  A number of polyline-shapes from 1 to the number of shapes. If all shapes are connected (same end/start
   *          points) a single polyline is returned.
   */
  // TODO: Write this
  //def apply(shapes : BasicShape*) : Seq[PolylineShape] = {
  //  if (shapes.size > 1 && shapes.head.geometry.vertices.exists(shapes.last.geometry.vertices.contains)) {
  //
  //  }
  //}

  /**
   * Creates a PolylineShape from a start point, the inner shapes (see documentation for the PolylineShape class) and
   * a number of [[com.siigna.util.collection.Attributes]].
   * @param startPoint  The starting point of the polyline
   * @param innerShapes  The inner shapes of the polyline
   * @param attributes  The attributes of the polyline
   */
  def apply(startPoint: Vector2D, innerShapes: Seq[InnerPolylineShape], attributes: Attributes) = {
    if (!innerShapes.isEmpty) {
      // Is it a closed polyline?
      if (startPoint.equals(innerShapes.last.point)) {
        // Yes
        new PolylineShapeClosed(startPoint, innerShapes.take(innerShapes.size - 1), attributes)
      } else new PolylineShapeOpen(startPoint, innerShapes, attributes) // No
    } else throw new IllegalArgumentException("Cannot create polyline from zero points.")
  }

  /**
   * Creates a PolylineShape connecting the given points with lines. Even if the first and last point are the same, the
   * polyline is open ([[com.siigna.app.model.shape.PolylineShape.PolylineShapeOpen]]).
   *
   * @param points  The points to use.
   */
  def createOpen(points: Vector2D*): PolylineShape = PolylineShape.createOpen(points.toTraversable)

  /**
   * Creates a PolylineShape from a collection of points. Even if the first and last points are equal in
   * the collection, the polyline is kept open.
   *
   * @param points  The collection of points to use.
   * @return  A PolylineShape connecting the given points with lines
   */
  def createOpen(points: Traversable[Vector2D]): PolylineShape =


    if (points.size < 2) throw new IllegalArgumentException("Cannot create polyline from less than 2 points.")
    else {
      val lines = distinctNeighbour(points.toList).map(p => new PolylineLineShape(p))
      PolylineShape.createOpen(points.head, lines.tail, Attributes())
    }

  /**
   * Creates an PolylineShape from a start point, the inner shapes (see documentation for the PolylineShape class) and
   * a number of [[com.siigna.util.collection.Attributes]]. The shape is open, even if start and end points are the same.
   * @param startPoint  The starting point of the polyline
   * @param innerShapes  The inner shapes of the polyline
   * @param attributes  The attributes of the polyline
   */
  def createOpen(startPoint: Vector2D, innerShapes: Seq[InnerPolylineShape], attributes: Attributes) = {
    if (!innerShapes.isEmpty) {
      new PolylineShapeOpen(startPoint, innerShapes, attributes)
    }
    else throw new IllegalArgumentException("Cannot create polyline from zero points.")
  }

  /**
   * A method to filter away elements if they are the same as the previous. Useful for removing duplicate points.
   * @param seq  The sequence to filter.
   * @tparam A  The type of the elements in the sequence.
   * @return  A new sequence of elements where subsequent duplicates are removed.
   */
  private[shape] def distinctNeighbour[A](seq : List[A]) : List[A] = {
    seq match {
      case x :: y :: tail => {
        if (x == y) distinctNeighbour(y :: tail)
        else   x :: distinctNeighbour(y :: tail)
      }
      case x => x
    }
  }

  /**
   * Extractor pattern for PolylineShapes
   * @param startPoint  The start point for the polyline
   * @param innerShapes  The [[com.siigna.app.model.shape.InnerPolylineShape]]s for the polyline
   * @param attributes  Attributes describing values set in the polyline
   * @return  Some[PolylineShapeOpen].
   */
  def unapply(startPoint: Vector2D, innerShapes: Seq[InnerPolylineShape], attributes: Attributes): Option[PolylineShape] = {
    Some(
      if (startPoint == innerShapes.last.point)
        new PolylineShapeClosed(startPoint, innerShapes.tail.take(innerShapes.size - 2), attributes)
      else
        new PolylineShapeOpen(startPoint, innerShapes, attributes)
    )
  }

}

/**
 * A shape type used in the PolylineShape. This shape is instantiated by a given point,
 * so we (1) ensure that the shapes are connected and (2) avoids any duplicated points.
 */
sealed trait InnerPolylineShape {

  /**
   * Creates a BasicShape to use inside the PolylineShape.
   * @param v  The vector with which the BasicShape is instantiated.
   */
  def apply(v: Vector2D): BasicShape

  /**
   * The only point the InnerPolylineShape knows for certain.
   */
  def point: Vector2D

  /**
   * Transforms the InnerPolylineShape with the given [[com.siigna.util.geom.TransformationMatrix]].
   */
  def transform(t: TransformationMatrix): InnerPolylineShape

}

/**
 * A LineShape representation used inside a PolylineShape.
 * @param point  The point given to create a LineShape.
 */
sealed case class PolylineLineShape(point: Vector2D) extends InnerPolylineShape {
  def apply(v: Vector2D) = LineShape(v, point)

  override def toString = "PolylineLineShape(" + point + ")"

  def transform(t: TransformationMatrix) = new PolylineLineShape(point.transform(t))
}

/**
 * An ArcShape representation used inside a PolylineShape.
 * @param middle  The center point of the arc
 * @param point The point given to create a LineShape.
 */
sealed case class PolylineArcShape(middle: Vector2D, point: Vector2D) extends InnerPolylineShape {
  def apply(v: Vector2D) = ArcShape(v, middle, point)

  override def toString = "PolylineArcShape(" + middle + ", " + point + ")"

  def transform(t: TransformationMatrix) = new PolylineArcShape(middle.transform(t), point.transform(t))
}