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
 * A geometry class for a polyline.
 * TODO: Strictly this isn't a mathematical representation but a collection. Keep it anyway?
 */
case class Polyline(geometries : Seq[BasicGeometry]) extends Geometry
{

  def boundary = geometries.map(_.boundary).reduceLeft(_ expand _)

  def center = geometries.map(_.center).reduceLeft(_ + _) / geometries.size

  def distanceTo(point : Vector) = geometries.map(_.distanceTo(point)).reduceLeft(math.min)

  def handles = geometries.map(_.handles).flatten.distinct

  def intersects(arc : Arc) =
    geometries.map(_.intersects(arc)).flatten

  def intersects(line : Segment) =
    geometries.map(_.intersects(line)).flatten

  def intersect(rectangle : Rectangle) : Boolean =
    geometries.map(_.intersect(rectangle)).reduceLeft(_ || _)

}