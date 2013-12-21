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

package com.siigna.util.geom

/**
 * A geometry designed for polylines
 */
case class CollectionGeometry2D(geometries : Seq[Geometry2D]) extends Geometry2D {

  assert(!geometries.isEmpty, "Cannot create empty polyline geometry")

  type T = CollectionGeometry2D

  def boundary = geometries.map(_.boundary).reduceLeft((a, b) => a.expand(b))

  def closestPoint(p : Vector2D) = geometries.map(_.closestPoint(p)).reduceLeft((a, b) => if ((p - a).length <= (p - b).length) a else b)

  def distanceTo(s : Geometry2D) = geometries.map(_.distanceTo(s)).reduceLeft((a, b) => if (a <= b) a else b)

  //def intersects(s : Geometry2D) = geometries.exists(_.intersects(s))
  def intersects(geom : Geometry2D) = geom match {

    case collection : CollectionGeometry2D => {
      //if the collectionShapes coinside no ints should exist
      if(this != geom) this.geometries.exists(_.intersects(collection))
      else false
    }

    case segment : Segment2D => {
      this.geometries.exists(_.intersects(segment))
    }

    case v : Vector2D => {
      var int = false
      this.geometries.foreach(s => if(s.intersects(v)) int = true) //int = true
      int
    }


    case g => {
      geometries.exists(_.intersects(g))
    }
  }

  //def intersections(s : Geometry2D) = geometries.foldLeft(Set[Vector2D]())((c, a) => c ++ a.intersections(s))
  def intersections(geom : Geometry2D) : Set[Vector2D] = geom match {

    case arc : Arc2D => {
      this.geometries.flatMap(s => s.intersections(arc)).toSet
    }

    //Polyline / Circle2D - intersections
    case circle : Circle2D =>  this.geometries.flatMap(s => s.intersections(circle)).toSet

    //TODO: misses the first int if two ints exist on the same segment.
    case collection : CollectionGeometry2D => {

      def filterEnds(ints : Set[Vector2D], end1 : Vector2D, end2 : Vector2D) = {
        val t = ints.filterNot(i => i == end1)
        t.filterNot(i => i == end2)
      }
      def segmentPLeval(segment : Geometry2D) = {
        val plSegments = this.geometries.toList
        val ints = plSegments.flatMap(s => segment.intersections(s)).toSet
        filterEnds(ints,segment.vertices.head,segment.vertices.last)
      }
      val r = collection.geometries.flatMap(g => segmentPLeval(g)).toSet
      r
    }

    //Polyline / Line2D - intersections
    case line : Line2D =>  this.geometries.flatMap(s => s.intersections(line)).toSet

    //Polyline / ComplexRectangle - intersections
    case rectangle : ComplexRectangle2D => rectangle.segments.toList.flatMap(s => s.intersections(this)).toSet

    //Polyline / Segment2D - intersections
    case segment : Segment2D =>  this.geometries.flatMap(s => s.intersections(segment)).toSet

    case v : Vector2D => this.geometries.flatMap(s => s.intersections(v)).toSet

    //rectangle / ? - intersections not implemented are caught here.
    case g => {
      println("intersections eval for CollectionGeometry2D and "+g+ " not supported")
      Set()
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