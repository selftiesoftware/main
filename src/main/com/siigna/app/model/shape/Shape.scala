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

import com.siigna.util.collection.Attributes
import com.siigna.util.dxf.DXFSection
import com.siigna.util.geom._

/**
 * A Shape that (basically) can be drawn and transformed.
 * <br />
 * Shapes are what Siigna is made of. Every action performed by the user on the drawing is executed upon shapes,
 * and the "database of Siigna" - the [[com.siigna.app.model.Model]] - consists of Shapes, that are taken and used
 * by the [[com.siigna.app.view.View]] the create the visible parts of Siigna, to let the users see what they're editing.
 * <br />
 * Every Shape has a set of [[com.siigna.util.collection.Attributes]] that describes their behaviour. Most shapes have
 * a Color-attribute that modifies the color of the shape when drawn, and a [[com.siigna.app.model.shape.LineShape]]
 * for instance can have a LineWidth attribute which determines how thick the line are.
 * <br />
 * Beneath every shape lies a geometric representation of the shape in a 2-dimensional euclidean space. The
 * [[com.siigna.util.geom.Geometry2D]] is used to determine various geometric properties such as intersections,
 * length, center-point(s) and so forth.
 *
 * <br />
 * The shape hierarchy looks like this:
 * <pre>
 *
 *             ShapeLike
 *                 |
 *                 +--------+ Selection
 *                 |
 *               Shape
 *                 |
 *      +----------+-----------+
 *      |          |           |
 *      |          |        CollectionShape (contains several shapes)
 *      |          |             |
 *      |          |             +--- GroupShape
 *      |          |             |
 *      |          |             +--- PolylineShape
 *      |          |
 *      |       BasicShape (used in PolylineShape)
 *      |            |
 *      |            +----- ArcShape
 *      |            |
 *      |            +----- LineShape
 *      |
 *    EnclosedShape (encapsulates a given area)
 *          |
 *          +--- CircleShape
 *          |
 *          +--- RectangleShape (TODO: Here and below)
 *                 |
 *                 +--- ImageShape
 *                 |
 *                 +--- TextShape
 * </pre>
 */
trait Shape extends ShapeLike with (ShapeSelector => Option[PartialShape]) {

  type T <: Shape

  /**
   * Merge the new attributes in with the existing ones, eventually overwriting
   * attributes with new values.
   *
   * @param  attribute  the new attributes to merge in.
   * @return  a shape with the updated attributes.
   *
   * TODO: Refactor to addAttribute
   */
  override def addAttribute(attribute : (String, Any)) = setAttributes(attributes + attribute)

  /**
   * Merge the new attributes in with the existing ones, eventually overwriting
   * attributes with new values.
   *
   * @param  attributes  the new attributes to merge in.
   * @return  a shape with the updated attributes.
   *
   * TODO: Refactor to addAttributes
   */
  override def addAttributes(attributes : (String, Any)*) = setAttributes(this.attributes ++ attributes)

  /**
   * Calculates the closest distance to the shape in the given scale.
   */
  def distanceTo(point : Vector2D, scale : Double) = geometry.distanceTo(point) * scale

  /**
   * Returns a rectangle that includes the entire shape.
   */
  def boundary : Rectangle2D = geometry.boundary

  /**
   * Deletes a part of the shape. If removing the part means that the shape looses its meaning the method returns None.
   */
  def delete(part : ShapeSelector) : Option[Shape]

  /**
   * The basic geometric object for the shape.
   */
  def geometry : Geometry2D

  /**
   * Returns the entire shape, so it can be manipulated dynamically.
   * @return  A [[com.siigna.app.model.shape.FullShapeSelector]].
   */
  def getPart = FullShapeSelector

  /**
   * Gets part of the shape by a rectangle. If the rectangle encloses the entire shape then return everything, but if
   * only a single point is enclosed (for example) then return that point and that point only. If nothing is
   * enclosed, then return None. This comes in handy when a selection-box sweeps across the model.
   * @param rect  The rectangle to base the selection on.
   * @return  The shape (or parts of it - or nothing at all) wrapped in a [[com.siigna.app.model.shape.ShapeSelector]].
   */
  def getPart(rect : Rectangle2D) : ShapeSelector

  /**
   * Gets part of the shape by a single point. The part of the shape that is closest to that point will be selected.
   * @param point  The point to base the selection on.
   * @return  The shape (or a part of it - or nothing at all) wrapped in a [[com.siigna.app.model.shape.ShapeSelector]].
   */
  def getPart(point : Vector2D) : ShapeSelector // getPart shape close to the point

  /**
   * Retrives the affected points from the given ShapeSelector
   * @param selector  The selector, i. e. the combination of the shape to be retrieved in points.
   * @return  A sequence of points, can be empty.
   */
  def getVertices(selector : ShapeSelector) : Seq[Vector2D]

  /**
   * Returns a setAttributes of the shape. In other words return a shape with a new id,
   * but otherwise the same attributes.
   */
  def setAttributes(attributes : Attributes) : T

  /**
   * Returns a DXFSection with the given shape represented.
   */
  def toDXF : DXFSection

  /**
   * Applies a transformation to the shape.
   */
  def transform(transformation : TransformationMatrix) : T

}

/**
 * A trait used for constructing Polylines when we need to match Lines and Arcs.
 * BasicShape is extended by two shapes: ArcShape and LineShape.
 */
trait BasicShape extends Shape {

  type T <: BasicShape

  /**
   * The basic geometric object for the shape.
   */
  override def geometry : GeometryBasic2D

  /**
   * Returns a setAttributes of the shape. In other words return a shape with a new id,
   * but otherwise the same attributes.
   */
  def setAttributes(attributes : Attributes) : T

  /**
   * Applies a transformation to the BasicShape.
   */
  def transform(transformation : TransformationMatrix) : T

}

/**
 * A trait for immutable shapes containing other immutable shapes.
 * @tparam G  The type of shapes inside the collection.
 */
trait CollectionShape[G <: Shape] extends Shape with Iterable[G] {

  type T = CollectionShape[G]
  
  // TODO: Fix this - how?
  def geometry = if (shapes.isEmpty) Rectangle2D.empty else CollectionGeometry(shapes.map(_.geometry))

  /**
   * Joins this CollectionShape with a single new shape and forms a new CollectionShape.
   * @param shape  The shape to insert into the CollectionShape.
   * @return  A new CollectionShape with the new shape included.
   */
  def join(shape : G) : T

  /**
   * Joins this CollectionShape with the given shapes and forms a new CollectionShape.
   * @param shapes  The shapes to insert into the CollectionShape.
   * @return  A new CollectionShape with the new shapes included.
   */
  def join(shapes : Traversable[G]) : T
  
  def iterator = shapes.toIterator

  /**
   * The inner shapes of the collection.
   */
  def shapes : Traversable[G]

}


/**
 * A shape that's closed, that is to say a shape that encases a closed space.
 */
trait EnclosedShape extends Shape {

  type T <: EnclosedShape

  override def geometry : GeometryEnclosed2D

  /**
   * Returns a setAttributes of the shape. In other words return a shape with a new id,
   * but otherwise the same attributes.
   */
  def setAttributes(attributes : Attributes) : T

  /**
   * Applies a transformation to the BasicShape.
   */
  def transform(transformation : TransformationMatrix) : T

}