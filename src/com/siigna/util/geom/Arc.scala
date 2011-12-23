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

import java.lang.NoSuchMethodError

/**
 * A mathematical representation of an arc.
 */
trait Arc[D <: Dimension] extends BasicGeometry[D] {

  /**
   * The number of degrees the arc is spanning.
   */
  def angle : Double

  /**
   * The ending angle of the arc. Remember that angles are always calculated counter-clockwise AND that zero
   * lies to the east.
   */
  def endAngle : Double

  /**
   * Determines whether a given angle is included in the arc's periphery.
   */
  def insideArcDegrees(angle : Double) : Boolean

  /**
   * Calculates the entire angle of the circle section.
   */
  def length : Double

  /**
   * Calculates the radius.
   */
  def radius : Double

  /**
   * The starting angle of the arc. The start angle is always calculated so the
   * arc is drawn counter-clockwise (ccw). Firstly this is done because there's
   * a common mathematical convention that every circle is drawn ccw and
   * secondly because java's graphical engine follows this convention.
   */
  def startAngle : Double

}

/**
 * A companion object to the Arc trait.
 */
object Arc {

  /**
   * Creates a 2D Arc.
   * Todo: Write this.
   */
  def apply(start : Vector2D, middle : Vector2D, end : Vector2D) = throw new NoSuchMethodError("Not yet implemented.")

  /**
   * Creates a 2D arc.
   */
  def apply(center : Vector2D, radius : Double, startAngle : Double, angle : Double) = new Arc2D(center, radius, startAngle, angle)

}