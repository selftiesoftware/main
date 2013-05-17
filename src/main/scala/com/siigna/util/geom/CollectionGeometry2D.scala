/*
 * Copyright (c) 2013. Siigna is released under the creative common license by-nc-sa. You are free
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
 * A geometry designed for polylines
 */
@SerialVersionUID(-209654418)
case class  CollectionGeometry2D(geometries : Seq[Geometry2D]) extends Geometry2D {

  assert(!geometries.isEmpty, "Cannot create empty polyline geometry")

  type T = CollectionGeometry2D

  def boundary = geometries.map(_.boundary).reduceLeft((a, b) => a.expand(b))

  def closestPoint(p : Vector2D) = geometries.map(_.closestPoint(p)).reduceLeft((a, b) => if ((p - a).length <= (p - b).length) a else b)

  def distanceTo(s : Geometry2D) = geometries.map(_.distanceTo(s)).reduceLeft((a, b) => if (a <= b) a else b)

  //def intersects(s : Geometry2D) = geometries.exists(_.intersects(s))
  def intersects(geom : Geometry2D) = geom match {

    case collection : CollectionGeometry2D => {
      var hasIntersection = false
      collection.geometries.foreach(s => if(s.intersects(this)) hasIntersection = true )
      if(collection == this) hasIntersection = false //coinciding collections should are not considered to intersect.
      hasIntersection
    }

    case segment : Segment2D => {
      var hasIntersection = false
      val segments = this.geometries
      segments.foreach(s => if(segment.intersects(s) == true) hasIntersection = true)
      hasIntersection
    }

    case g => geometries.exists(_.intersects(g))
    //case g => throw new UnsupportedOperationException("Segment: intersects not yet implemented with " + g)
  }

  //def intersections(s : Geometry2D) = geometries.foldLeft(Set[Vector2D]())((c, a) => c ++ a.intersections(s))
  def intersections(geom : Geometry2D) : Set[Vector2D] = geom match {
    //TODO make this!!
    case collection : CollectionGeometry2D => {
      Set()
    }

    case segment : Segment2D => {
      var intersections : Set[Vector2D] = Set()
      val segments = this.geometries
      segments.foreach(s => if(!segment.intersections(s).isEmpty) {
        intersections = segment.intersections(s)
      })
      intersections
    }
  }

  def transform(t : TransformationMatrix) = CollectionGeometry2D(geometries.map(_ transform t))

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