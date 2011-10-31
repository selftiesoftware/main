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
case class Vector(x : Double, y : Double) extends Geometry
{

  val center = this

  /**
   * Returns the sum of this vector with another vector.
   */
  def +(other : Vector) = Vector(x + other.x, y + other.y)

  /**
   * Subtracts this vector with another vector.
   */
  def -(other : Vector) = Vector(x - other.x, y - other.y)

  /**
   * The scalar product of this vector with another vector.
   */
  def *(other : Vector) = x * other.x + y * other.y

  /**
   * Multiplies a vector with a number.
   */
  def *(scale : Double) = Vector(x * scale, y * scale)

  /**
   * Divides a vector with a number.
   */
  def /(scale : Double) = Vector(x / scale, y / scale)

  /**
   * Rotates the vector 180 degrees by multiplying with (-1). This gives a vector
   * pointing in the opposite direction.
   */
  def unary_- = new Vector(-x, -y)

  /**
   * Returns the absolute value of the vector.
   */
  def abs = Vector(scala.math.abs(this.x), scala.math.abs(this.y))

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
  def boundary = Rectangle(this, this)

  /**
   * Confines this vector into the bounds of the given rectangle.
   */
  def confine(rectangle : Rectangle) : Vector = Vector(
    if      (x < rectangle.topLeft.x)  rectangle.topLeft.x
    else if (x > rectangle.topRight.x) rectangle.topRight.x
    else     x,
    if      (y > rectangle.topLeft.y)    rectangle.topLeft.y
    else if (y < rectangle.bottomLeft.y) rectangle.bottomLeft.y
    else     y)

  /**
   * Gets the distance to another vector.
   */
  def distanceTo(point : Vector) = (point - this).length

  def handles = List(this)

  def intersect(rectangle : Rectangle) = rectangle.distanceTo(this) == 0

  /**
   * Calculates the length of the vector.
   */
  def length = java.lang.Math.hypot(x, y)

  // TODO: Should probably have a better name.
  /**
   * Calculates the vector rotated 90 degrees counter-clockwise.
   */
  def normal = new Vector(-y, x)

  /**
   * Rounds the vector to whole numbers.
   */
  def round = new Vector(scala.math.round(x), scala.math.round(y))

  /**
   * Transforms a vector with a given transformation matrix.
   */
  def transform(transformation : TransformationMatrix) : Vector = transformation.transform(this)

  /**
   * Calculates the unit-vector of this.
   */
  def unit = new Vector(this.x / this.length, this.y / this.length)

}

/**
 * Utility functions for vectors.
 *
 * @see Vector
 */
object Vector {

  /**
   * Parses a string as a 2D vector. Here is an example of the expected format:
   * <code>(3.14, 7)</code>.
   *
   * <p>
   * The string must start and end with parentheses and
   * include exactly two double values (integers are also doubles) which is
   * seperated by comma. Extra white-space inside the parentheses is discarded.
   * White-space outside the parentheses is NOT expected, so if you are unsure
   * whether there is white-space in your string or not, please
   * <code>trim</code> your string before invoking this function.
   * </p>
   *
   * @param  string  containing a representation of a vector.
   * @return  a new vector parsed from the string content.
   */
  def parseVector(value : String) = {
    if (value.startsWith("(") && value.endsWith(")")) {
      val coordinates =
        try {
          value substring(1, value.size - 1) split("\\,") map(
            coordinate => java.lang.Double parseDouble(coordinate trim)
          )
        } catch {
          case ex => throw new IllegalArgumentException("Expected a numeric 2D vector, got: " + value, ex)
        }
      if (coordinates.size != 2)
        throw new IllegalArgumentException("Expected a 2-dimensional vector, got: " + value)
      Vector(coordinates(0), coordinates(1))
    } else {
      throw new IllegalArgumentException("Expected a vector, got: " + value)
    }
  }

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
  def determinant(a : Vector, b : Vector) = a.x * b.y - a.y * b.x

  /**
   * Calculates the shortest angle between two vectors.
   *
   * @param  v1  the first vector.
   * @param  v2  the second vector.
   * @return  the angle between the two vectors in degrees.
   */
  def shortestAngleBetweenVectors(v1 : Vector, v2 : Vector) =
  {
    val scalar = v1.length * v2.length
    if (scalar != 0) {
      val inner = v1 * v2 / scalar
      scala.math.acos(inner) * 180 / scala.math.Pi
    } else 0.0
  }

}
