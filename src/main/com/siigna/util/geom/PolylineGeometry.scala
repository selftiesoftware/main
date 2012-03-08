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

package com.siigna.util.geom

import com.siigna.app.model.shape.LineShape

/**
 * A geometry designed for polylines
 */
case class PolylineGeometry(geometries : Seq[GeometryBasic2D]) extends Geometry2D {

  assert(!geometries.isEmpty, "Cannot create empty polyline geometry")

  type T = PolylineGeometry

  def boundary = geometries.map(_.boundary).reduceLeft((a, b) => a.expand(b))

  def closestPoint(p : Vector2D) = geometries.map(_.closestPoint(p)).reduceLeft((a, b) => if ((p - a).length <= (p - b).length) a else b)

  def distanceTo(s : Arc2D) = geometries.map(_.distanceTo(s)).reduceLeft((a, b) => if (a <= b) a else b)
  def distanceTo(s : Circle2D) = geometries.map(_.distanceTo(s)).reduceLeft((a, b) => if (a <= b) a else b)
  def distanceTo(s : Ellipse2D) = geometries.map(_.distanceTo(s)).reduceLeft((a, b) => if (a <= b) a else b)
  def distanceTo(s : Line2D) = geometries.map(_.distanceTo(s)).reduceLeft((a, b) => if (a <= b) a else b)
  def distanceTo(s : Rectangle2D) = geometries.map(_.distanceTo(s)).reduceLeft((a, b) => if (a <= b) a else b)
  def distanceTo(s : Segment2D) = geometries.map(_.distanceTo(s)).reduceLeft((a, b) => if (a <= b) a else b)
  def distanceTo(s : Vector2D) = geometries.map(_.distanceTo(s)).reduceLeft((a, b) => if (a <= b) a else b)

  def intersects(s : Arc2D) = geometries.exists(_.intersects(s))
  def intersects(s : Circle2D) = geometries.exists(_.intersects(s))
  def intersects(s : Ellipse2D) = geometries.exists(_.intersects(s))
  def intersects(s : Line2D) = geometries.exists(_.intersects(s))
  def intersects(s : Rectangle2D) = geometries.exists(_.intersects(s))
  def intersects(s : Segment2D) = geometries.exists(_.intersects(s))

  def intersections(s : Arc2D) = geometries.foldLeft(Set[Vector2D]())((c, a) => c ++ a.intersections(s))
  def intersections(s : Circle2D) = geometries.foldLeft(Set[Vector2D]())((c, a) => c ++ a.intersections(s))
  def intersections(s : Ellipse2D) = geometries.foldLeft(Set[Vector2D]())((c, a) => c ++ a.intersections(s))
  def intersections(s : Line2D) = geometries.foldLeft(Set[Vector2D]())((c, a) => c ++ a.intersections(s))
  def intersections(s : Rectangle2D) = geometries.foldLeft(Set[Vector2D]())((c, a) => c ++ a.intersections(s))
  def intersections(s : Segment2D) = geometries.foldLeft(Set[Vector2D]())((c, a) => c ++ a.intersections(s))

  def transform(t : TransformationMatrix) = PolylineGeometry(geometries.map(_ transform t))

  def vertices = {
    var vertices = Seq[Vector2D]()
    for (g <- geometries) {
      if (vertices.isEmpty) {
        vertices :+= g.vertices.head
      }

      vertices :+= g.vertices.last
    }
    vertices
  }

}