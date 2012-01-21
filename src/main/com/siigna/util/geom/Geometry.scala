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

package com.siigna.util.geom

/**
 * An N-dimensional <code>Geometry</code>. Contains methods that's able to calculate different
 * geometrical properties, such as the distance to another geometry, intersection(s)
 * with another geometry, whether the geometry overlaps with another geometry etc.
 * TODO: Test everything!
 */
trait Geometry {

  /**
   * Define the type of the current geometry.
   */
  type T <: Geometry

  type V <: Vector

  /**
   * The boundary of the shape.
   */
  def boundary : Rectangle

  /**
   * The center of the geometry.
   */
  def center : V

  /**
   * Calculates the closest point on the geometry from a given vector.
   */
  def closestPoint(vector : V) : V

  /**
   * The dimension of the current geometry.
   */
  def dimension : Int

  /**
   * Transform the geometry with a given matrix.
   */
  def transform(transformation : TransformationMatrix) : T

  /**
   * A set of vertices defined by the geometry.
   */
  def vertices : Seq[V]

}

/**
 * A geometry wrapper for 2-dimensional geometries.
 */
trait Geometry2D extends Geometry {

  /**
   * The geometry type to be used in concrete implementations.
   */
  type T <: Geometry2D

  /**
   * The vector type of this dimension.
   */
  type V = Vector2D

  /**
   * The number of dimensions for this geometry.
   */
  val dimension = 2

  /**
   * The boundary of the shape.
   */
  override def boundary : Rectangle2D

  /**
   * The center of the shape.
   */
  override def center = boundary.center

  /**
   * Determines the distance from the geometry to an arc.
   */
  def distanceTo(arc : Arc2D) : Double

  /**
   * Determines the distance from the geometry to a circle.
   */
  def distanceTo(circle : Circle2D) : Double

  /**
   * Determines the distance from the geometry to an ellipse.
   */
  def distanceTo(ellipse : Ellipse2D) : Double

  /**
   * Determines the distance from the geometry to a line.
   */
  def distanceTo(line : Line2D) : Double

  /**
   * Determines the distance from the geometry to a rectangle.
   */
  def distanceTo(rectangle : Rectangle2D) : Double

  /**
   * Determines the distance from the geometry to a segment.
   */
  def distanceTo(segment : Segment2D) : Double

  /**
   * Determines the distance from the geometry to a vector.
   */
  def distanceTo(vector : Vector2D) : Double

  /**
   * Determine whether the geometry is overlapping (intersecting) the given arc.
   */
  def intersects(arc : Arc2D) : Boolean

  /**
   * Determine whether the geometry is overlapping (intersecting) the given circle.
   */
  def intersects(circle : Circle2D) : Boolean

  /**
   * Determine whether the geometry is overlapping (intersecting) the given ellipse.
   */
  def intersects(ellipse : Ellipse2D) : Boolean

  /**
   * Determine whether the geometry is overlapping (intersecting) the given line.
   */
  def intersects(line : Line2D) : Boolean

  /**
   * Determine whether the geometry is overlapping (intersecting) the given rectangle.
   */
  def intersects(rectangle : Rectangle2D) : Boolean

  /**
   * Determine whether the geometry is overlapping (intersecting) the given segment.
   */
  def intersects(segment : Segment2D) : Boolean

  /**
   * Returns the intersections between this and the given arc, if any.
   */
  def intersections(arc : Arc2D) : Set[Vector2D]

  /**
   * Returns the intersections between this and the given circle, if any.
   */
  def intersections(circle : Circle2D) : Set[Vector2D]

  /**
   * Returns the intersections between this and the given ellipse, if any.
   */
  def intersections(ellipse : Ellipse2D) : Set[Vector2D]

  /**
   * Returns the intersections between this and the given line, if any.
   */
  def intersections(line : Line2D) : Set[Vector2D]

  /**
   * Returns the intersections between this and the given rectangle, if any.
   */
  def intersections(rectangle : Rectangle2D) : Set[Vector2D]

  /**
   * Returns the intersections between this and the given segment, if any.
   */
  def intersections(segment : Segment2D) : Set[Vector2D]

}