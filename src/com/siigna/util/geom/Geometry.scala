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
sealed trait Geometry[D <: Dimension] {

  /**
   * The boundary of the shape.
   */
  def boundary : Rectangle[D]

  /**
   * The center of the geometry.
   */
  def center : Vector[D]

  /**
   * Calculates the closest point on the geometry from a given vector.
   */
  def closestPoint(vector : Vector[D]) : Vector[D]

  /**
   * The dimension of the current geometry.
   */
  def dimension : Int

  /**
   * Determines the distance from the geometry to an arc.
   */
  def distanceTo(arc : Arc[D]) : Double

  /**
   * Determines the distance from the geometry to a circle.
   */
  def distanceTo(circle : Circle[D]) : Double

  /**
   * Determines the distance from the geometry to an ellipse.
   */
  def distanceTo(ellipse : Ellipse[D]) : Double

  /**
   * Determines the distance from the geometry to a line.
   */
  def distanceTo(line : Line[D]) : Double

  /**
   * Determines the distance from the geometry to a rectangle.
   */
  def distanceTo(rectangle : Rectangle[D]) : Double

  /**
   * Determines the distance from the geometry to a segment.
   */
  def distanceTo(segment : Segment[D]) : Double

  /**
   * Determines the distance from the geometry to a vector.
   */
  def distanceTo(vector : Vector[D]) : Double

  /**
   * Determine whether the geometry is overlapping (intersecting) the given arc.
   */
  def intersects(arc : Arc[D]) : Boolean

  /**
   * Determine whether the geometry is overlapping (intersecting) the given circle.
   */
  def intersects(circle : Circle[D]) : Boolean

  /**
   * Determine whether the geometry is overlapping (intersecting) the given ellipse.
   */
  def intersects(ellipse : Ellipse[D]) : Boolean

  /**
   * Determine whether the geometry is overlapping (intersecting) the given line.
   */
  def intersects(line : Line[D]) : Boolean

  /**
   * Determine whether the geometry is overlapping (intersecting) the given rectangle.
   */
  def intersects(rectangle : Rectangle[D]) : Boolean

  /**
   * Determine whether the geometry is overlapping (intersecting) the given segment.
   */
  def intersects(segment : Segment[D]) : Boolean

  /**
   * Determine whether the geometry is overlapping (intersecting) the given vector.
   */
  def intersects(vector : Vector[D]) : Boolean

  /**
   * Returns the intersections between this and the given arc, if any.
   */
  def intersections(arc : Arc[D]) : Set[Vector[D]]

  /**
   * Returns the intersections between this and the given circle, if any.
   */
  def intersections(circle : Circle[D]) : Set[Vector[D]]

  /**
   * Returns the intersections between this and the given ellipse, if any.
   */
  def intersections(ellipse : Ellipse[D]) : Set[Vector[D]]

  /**
   * Returns the intersections between this and the given line, if any.
   */
  def intersections(line : Line[D]) : Set[Vector[D]]

  /**
   * Returns the intersections between this and the given rectangle, if any.
   */
  def intersections(rectangle : Rectangle[D]) : Set[Vector[D]]

  /**
   * Returns the intersections between this and the given segment, if any.
   */
  def intersections(segment : Segment[D]) : Set[Vector[D]]

  /**
   * Transform the geometry with a given matrix.
   */
  def transform(transformation : TransformationMatrix) : Geometry[D]

}

/**
 * A <code>Geometry</code> for basic geometries included by the <code>Polyline</code> class.
 */
trait BasicGeometry[D <: Dimension] extends Geometry[D]

/**
 * A <code>Geometry</code> that encloses (surrounds) a 2-dimensional space. E. g. a <code>Rectangle</code>.
 */
trait EnclosedGeometry[D <: Dimension] extends Geometry[D] {

  /**
   * Determines the enclosed area of the geometry.
   */
  def area : Double

  /**
   * Examines whether a given arc is within the four boundaries
   * of a rectangle.
   */
  def contains(arc : Arc[D]) : Boolean

  /**
   * Examines whether a circle is within the four boundaries
   * of a rectangle.
   */
  def contains(circle : Circle[D]) : Boolean

  /**
   * Examines whether an ellipse is within the four boundaries
   * of a rectangle.
   */
  def contains(ellipse : Ellipse[D]) : Boolean

  /**
   * Examines whether a line is within (or on top of) the four boundaries
   * of a rectangle.
   */
  def contains(line : Line[D]) : Boolean

  /**
   * Examines whether an ellipse is within the four boundaries
   * of a rectangle.
   */
  def contains(rectangle : Rectangle[D]) : Boolean

  /**
   * Examines whether a segment is within (or on top of) the four boundaries
   * of a rectangle.
   */
  def contains(segment : Segment[D]) : Boolean

  /**
   * Examines whether a point is within (or on top of) the four boundaries
   * of a rectangle.
   */
  def contains(point : Vector[D]) : Boolean

}