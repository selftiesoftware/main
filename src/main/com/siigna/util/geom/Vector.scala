/*
 * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 * to Share — to copy, distribute and transmit the work,
 * to Remix — to adapt the work
 *
 * Under the following conditions:
 * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 * Noncommercial — You may not use this work for commercial purposes.
 * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 */

package com.siigna.util.geom

import java.awt.Point

/**
 * A vector class utility.
 */
trait Vector extends Geometry {

  type T <: Vector
  type R <: Rectangle

  /**
   * Returns the sum of this vector with another vector.
   */
  def +(other : T) : T

  /**
   * Subtracts this vector with another vector.
   */
  def -(other : T) : T

  /**
   * The product of this vector with another vector and retrieve the dot-product.
   */
  def *(other : T) : Double

  /**
   * Multiplies a vector with a scalar.
   */
  def *(scalar : Double) : T

  /**
   * Divides a vector with a number.
   */
  def /(scale : Double) : T

  /**
   * Rotates the vector 180 degrees by multiplying with (-1). This gives a vector
   * pointing in the opposite direction.
   */
  def unary_- : T

  /**
   * Returns the absolute value of the vector.
   */
  def abs : T

  /**
   * Gives an angle (in degrees) for the vector relative to the x-axis CCW. Zero degrees indicate
   * that the vector is on the positive x-axis.
   *
   * @return  a number between 0 and 360, calculated counter-clockwise from the (positive) x-axis.
   */
  def angle : Double

  /**
   * Defines the boundary of the Vector as an empty rectangle.
   */
  def boundary : R

  /**
   * Confines this vector into the bounds of the given rectangle.
   */
  def confine(rectangle : R) : T

  /**
   * Calculates the length of the vector.
   */
  def length : Double

  /**
   * Calculates the vector rotated 90 degrees counter-clockwise.
   */
  def normal : T

  /**
   * Rounds the vector to whole numbers.
   */
  def round : T

  /**
   * Transform the vector with the given TransformationMatrix.
   */
  def transform(transformation : TransformationMatrix) : T

  /**
   * Calculates the unit-vector of this vector. If the length of the vector is <= [[scala.Double#MinPositiveValue]] we
   * return a zero vector instead.
   */
  def unit : T

}

/**
 * Utility functions for vectors.
 *
 * @see Vector
 */
object Vector {

  /**
   * Parses a string as a ND vector. Here is an example of the expected format:
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
   * @param  value  containing a representation of a vector.
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
          case ex : Exception => throw new IllegalArgumentException("Expected a numeric 2D vector, got: " + value, ex)
        }
      if (coordinates.size != 2)
        throw new IllegalArgumentException("Expected a 2-dimensional vector, got: " + value)
      Vector2D(coordinates(0), coordinates(1))
    } else {
      throw new IllegalArgumentException("Expected a vector, got: " + value)
    }
  }

}

/**
 * A vector class utility.
 */
@SerialVersionUID(1206628808)
case class Vector2D(x : Double, y : Double) extends Vector with Geometry2D {

  type T = Vector2D
  type R = SimpleRectangle2D

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
  def boundary = SimpleRectangle2D(this.x, this.y, this.x, this.y)

  /**
   * Confines the vector to the bounds of the given rectangle.
   */
  def confine(rectangle : SimpleRectangle2D) = Vector2D(
    if (x < rectangle.xMin) rectangle.xMin else if (x > rectangle.xMax) rectangle.xMax else x,
    if (y < rectangle.yMin) rectangle.yMin else if (y > rectangle.yMax) rectangle.yMax else y)

  /**
   * Gets the distance to another vector.
   */
  def distanceTo(point : Vector2D) = (point - this).length

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
   * Returns the [[java.awt.Point]]-representation of the Vector.
   */
  def toPoint = new Point(x.toInt, y.toInt)

  /**
   * Transforms a vector with a given transformation matrix.
   */
  def transform(transformation : TransformationMatrix) = transformation.transform(this)

  def unit = if (length <= Double.MinPositiveValue) Vector2D(0, 0) else Vector2D(this.x / this.length, this.y / this.length)

  def distanceTo(geometry : Geometry2D) = geometry.distanceTo(this)

  def intersects(geometry: Geometry2D) = geometry.intersects(this)

  def intersections(geometry: Geometry2D) = geometry.intersections(this)

  def closestPoint(vector: Vector2D) = this

  def vertices = Seq(this)
}

/**
 * Companion object to Vector2D.
 */
object Vector2D {

  /**
   * Creates a Vector2D from a given [[java.awt.Point]].
   * @param p  The [[java.awt.Point]] to convert to a Vector2D
   * @return  A Vector2D with the same x and y-coordinates as the given point
   */
  def apply(p : Point) = new Vector2D(p.getX, p.getY)

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