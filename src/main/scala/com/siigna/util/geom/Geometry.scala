/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
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
 * <p>
 *   An N-dimensional <code>Geometry</code>. Contains methods that's able to calculate different
 *   geometrical properties, such as the distance to another geometry, intersection(s)
 *   with another geometry, whether the geometry overlaps with another geometry etc.
 * </p>
 *
 */
// TODO: Test everything!
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
   * A sequence of vertices defined by the geometry.
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
  def boundary : Rectangle2D

  /**
   * The center of the shape.
   */
  def center = boundary.center

  /**
   * Determines the distance from the geometry to an arc.
   */
  def distanceTo(geometry : Geometry2D) : Double

  /**
   * Determine whether the geometry is overlapping (intersecting) the given geometry.
   */
  def intersects(geometry : Geometry2D) : Boolean

  /**
   * Returns the intersections between this and the given geometry, if any.
   */
  def intersections(geometry : Geometry2D) : Set[Vector2D]

}

/**
 * A <code>Geometry</code> for basic geometries included by the <code>Polyline</code> class.
 */
sealed trait GeometryBasic

/**
 * A <code>Geometry</code> type for basic 2-dimensional geometries.
 */
trait GeometryBasic2D extends Geometry2D with GeometryBasic {

  type T <: GeometryBasic2D

}

/**
 * A <code>Geometry</code> that encloses (surrounds) a n-dimensional space. E. g. a <code>Rectangle</code>.
 */
sealed trait GeometryClosed {

  /**
   * Determines the enclosed area of the geometry.
   */
  def area : Double

}

/**
 * A geometry that encloses a 2-dimensional space.
 */
trait GeometryClosed2D extends GeometryClosed with Geometry2D {

  type T <: GeometryClosed2D

  // TODO: Introduce expand

  /**
   * Examines whether a the given geometry is completely enclosed by this geometry.
   * @return true if the given geometry is inside, false otherwise
   */
  def contains(geometry : Geometry2D) : Boolean

}