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
 * A vector class utility.
 */
case class Vector2D(x : Double, y : Double) extends Vector {

  type T = Vector2D
  type R = Rectangle2D

  /**
   * The dimension of the vector.
   */
  val dimension = 2

  /**
   * Returns the sum of this vector with another vector.
   */
  def +(other : Vector2D) = Vector2D(x + other.x, y + other.y)

  /**
   * Subtracts this vector with another vector.
   */
  def -(other : Vector2D) = Vector2D(x - other.x, y - other.y)

  /**
   * The scalar product of this vector with another vector.
   */
  def *(that : Vector2D) : Double = x * that.x + y * that.y

  /**
   * Multiplies a vector with a number.
   */
  def *(scale : Double) = Vector2D(x * scale, y * scale)

  /**
   * Divides a vector with a number.
   */
  def /(scale : Double) = Vector2D(x / scale, y / scale)

  /**
   * Rotates the vector 180 degrees by multiplying with (-1). This gives a vector
   * pointing in the opposite direction.
   */
  def unary_- = Vector2D(-x, -y)

  /**
   * Returns the absolute value of the vector.
   */
  def abs = Vector2D(scala.math.abs(this.x), scala.math.abs(this.y))

  /**
   * Gives an angle (in degrees) for the vector relative to the x-axis CCW. Zero degrees indicate
   * that the vector is on the positive x-axis.
   *
   * @return  a number between 0 and 360, calculated counter-clockwise from the (positive) x-axis.
   */
  def angle = {
    val degrees = scala.math.atan2(y, x) * 180 / scala.math.Pi
    if (degrees < 0)
      degrees + 360
    else
      degrees
  }

  /**
   * Defines the boundary of the Vector as an empty rectangle.
   */
  def boundary = Rectangle2D(this.x, this.y, this.x, this.y)

  /**
   * Confines the vector to the bounds of the given rectangle.
   */
  def confine(rectangle : Rectangle2D) = Vector2D(
    if (x < rectangle.xMin) rectangle.xMin else if (x > rectangle.xMax) rectangle.xMax else x,
    if (y < rectangle.yMin) rectangle.yMin else if (y > rectangle.yMax) rectangle.yMax else y)

  /**
   * Changes x by the given value.
   */
  def changeX(delta : Double) = new Vector2D(x + delta, y)

  /**
   * Changes y by the given value.
   */
  def changeY(delta : Double) = new Vector2D(x, y + delta)
  
  /**
   * Gets the distance to another vector.
   */
  def distanceTo(point : Vector2D) = (point - this).length

  /**
   * Examines whether a given rectangle intersects with the point.
   */
  def intersect(rectangle : Rectangle2D) = rectangle.distanceTo(this) == 0

  /**
   * Calculates the length of the vector.
   */
  def length = java.lang.Math.hypot(x, y)

  /**
   * Calculates the vector rotated 90 degrees counter-clockwise.
   */
  def normal = Vector2D(-y, x)

  /**
   * Rounds the vector to whole numbers.
   */
  def round = Vector2D(scala.math.round(x), scala.math.round(y))

  /**
   * Transforms a vector with a given transformation matrix.
   */
  def transform(transformation : TransformationMatrix) = transformation.transform(this)

  /**
   * Calculates the unit-vector of this.
   */
  def unit = Vector2D(this.x / this.length, this.y / this.length)

}

/**
 * Companion object to Vector2D.
 */
object Vector2D {
  
  import java.lang.Double.NaN

  /**
   * Calculates the determinant of the 2x2 matrix described by two vectors.
   *
   * <p>
   * The matrix is defined like this:
   * <pre>
   *     | a.x  b.x |             or    | a.x  a.y |
   *     | a.y  b.y | (columns)         | b.x  b.y | (rows)
   * </pre>
   * </p>
   *
   * @param  a  the first column (or row) of the determinant matrix.
   * @param  b  the second column (or row) of the determinant matrix.
   * @return  the determinant value.
   */
  def determinant(a : Vector2D, b : Vector2D) = a.x * b.y - a.y * b.x

  /**
   * Creates an empty vector with two NaN.
   */
  def empty = Vector(NaN, NaN)
  
  /**
   * Calculates the shortest angle between two vectors.
   *
   * @param  v1  the first vector.
   * @param  v2  the second vector.
   * @return  the angle between the two vectors in degrees.
   */
  def shortestAngleBetweenVectors(v1 : Vector2D, v2 : Vector2D) = {
    val scalar = v1.length * v2.length
    if (scalar != 0) {
      val inner = v1 * v2 / scalar
      scala.math.acos(inner) * 180 / scala.math.Pi
    } else 0.0
  }

}