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
 * A 2-dimensional <code>Geometry</code>. Contains methods that's able to calculate different geometrical properties,
 * such as the distance to another geometry, the intersection(s) with another geometry etc.
 * TODO: Expand this.. You know what to do.
 * TODO: Test everything!
 */
trait Geometry {

  /**
   * The boundary of the shape.
   */
  def boundary : Rectangle

  /**
   * The 2D center of the geometry.
   */
  def center : Vector

  /**
   * Determines the distance from the geometry to the vector in scale 1:1.
   */
  def distanceTo(point : Vector) : Double

  /**
   * Determine whether the geometry intersects with the given rectangle.
   */
  def intersect(rectangle : Rectangle) : Boolean

  /**
   * TODO: Move this to shapes! This isn't a mathematical property!
   */
  def handles : Seq[Vector]

  // TODO: Implement this? Great for snap! But what else?
  //lazy val centerPoint : Vector
  //lazy val endPoint    : Vector
  //lazy val midPoint    : Vector

  // def transform(t : TransformationMatrix) : Geometry

}

/**
 * A <code>Geometry</code> that encloses (surrounds) a 2-dimensional space. E. g. a <code>Rectangle</code>.
 */
trait EnclosedGeometry extends Geometry {

  /**
   * Determines the enclosed area of the geometry.
   */
  def area : Double

  /**
   * Determine whether a given point is inside the geometry.
   */
  def contains(point : Vector) : Boolean

}

/**
 * A <code>Geometry</code> for basic geometries included by the <code>Polyline</code> class.
 */
trait BasicGeometry extends Geometry {

  /**
   * Determine the points where the geometry intersects with an arc.
   */
  def intersects(arc : Arc) : Seq[Vector]

  /**
   * Determine the points where the geometry intersects with a segment.
   */
  def intersects(arc : Segment) : Seq[Vector]

}
