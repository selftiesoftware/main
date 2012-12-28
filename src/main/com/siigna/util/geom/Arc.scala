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

/**
 * A mathematical representation of a circle-piece, that is a not-full circle.
 * 
 * <b>Important:</b> Assumes that (start != middle != end).
 */
trait Arc {

  /**
   * The circle with the same center and radius as this Arc.
   * @return  A Circle with the same center and radius.
   */
  def circle : Circle

  /**
   * A value used to signal arcs defined counter-clockwise.
   */
  val cw, CW, clockwise = 0
  
  /**
   * A value used to signal arcs defined clockwise.
   */
  val ccw, CCW, counterclockwise = 1
  
  /**
   * Calculates the end angle. The end angle is always calculated so the
   * arc is drawn counter-clockwise (ccw). Firstly this is done because there's
   * a common mathematical convention that every circle is drawn ccw and
   * secondly because java's graphical engine follows this convention.
   */
  def endAngle : Double

  /**
   * Calculates the entire angle of the circle section.
   */
  def length : Double

  /**
   * The radius of the center to the circumference of the circle representing the circle-piece.
   */
  def radius : Double

  /**
   * Calculates the start angle. The start angle is always calculated so the
   * arc is drawn counter-clockwise (ccw). Firstly this is done because there's
   * a common mathematical convention that every circle is drawn ccw and
   * secondly because java's graphical engine follows this convention.
   */
  def startAngle : Double

  /**
   * Determines whether a given angle is included in the arc's periphery.
   */
  def insideArcDegrees(angle : Double) : Boolean

}

/**
 * A companion object to the Arc trait.
 */
object Arc {

  /**
   * Creates a 2D Arc.
   */
  def apply(start : Vector2D, middle : Vector2D, end : Vector2D) : Arc2D = Arc2D(start, middle, end)

}