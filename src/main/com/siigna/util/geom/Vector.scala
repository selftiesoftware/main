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
 *
 * @tparam T  The type of the vector given so we can assure the functions returns the desired type of vector.
 */
<<<<<<< HEAD:src/com/siigna/util/geom/Vector.scala
trait Vector[D <: Dimension] extends Geometry
{

=======
trait Vector {

  type T <: Vector
  type R <: Rectangle

>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Vector.scala
  /**
   * The center of the geometry.
   */
  val center = this

  /**
   * Returns the sum of this vector with another vector.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Vector.scala
  def +(other : Vector[D]) : Vector[D]
=======
  def +(other : T) : T
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Vector.scala

  /**
   * Subtracts this vector with another vector.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Vector.scala
  def -(other : Vector[D]) : Vector[D]
=======
  def -(other : T) : T
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Vector.scala

  /**
   * The product of this vector with another vector and retrieve the dot-product.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Vector.scala
  def *(other : Vector[D]) : Vector[D]
=======
  def *(other : T) : Double
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Vector.scala

  /**
   * Multiplies a vector with a scalar.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Vector.scala
  def *(scale : Double) : Vector[D]
=======
  def *(scalar : Double) : T
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Vector.scala

  /**
   * Divides a vector with a number.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Vector.scala
  def /(scale : Double) : Vector[D]
=======
  def /(scale : Double) : T
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Vector.scala

  /**
   * Rotates the vector 180 degrees by multiplying with (-1). This gives a vector
   * pointing in the opposite direction.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Vector.scala
  def unary_- : Vector[D]
=======
  def unary_- : T
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Vector.scala

  /**
   * Returns the absolute value of the vector.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Vector.scala
  def abs : Vector[D]
=======
  def abs : T
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Vector.scala

  /**
   * Gives an angle (in degrees) for the vector relative to the x-axis CCW. Zero degrees indicate
   * that the vector is on the positive x-axis.
   *
   * @return  a number between 0 and 360, calculated counter-clockwise from the (positive) x-axis.
   */
  def angle : Double
<<<<<<< HEAD:src/com/siigna/util/geom/Vector.scala

  /**
   * Defines the boundary of the Vector as an empty rectangle.
   */
  def boundary : Rectangle[D]
=======
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Vector.scala

  /**
   * Confines this vector into the bounds of the given rectangle.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Vector.scala
  def confine(rectangle : Rectangle[D]) : Vector[D]
=======
  def confine(rectangle : R) : T
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Vector.scala

  /**
   * Calculates the length of the vector.
   */
  def length : Double

  /**
   * Calculates the vector rotated 90 degrees counter-clockwise.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Vector.scala
  def normal : Vector[D]
=======
  def normal : T
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Vector.scala

  /**
   * Rounds the vector to whole numbers.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Vector.scala
  def round : Vector[D]
=======
  def round : T
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Vector.scala

  /**
   * Transform the vector with the given TransformationMatrix.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Vector.scala
  def transform(transformation : TransformationMatrix) : Vector[D]
=======
  def transform(transformation : TransformationMatrix) : T
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Vector.scala

  /**
   * Calculates the unit-vector of this.
   */
<<<<<<< HEAD:src/com/siigna/util/geom/Vector.scala
  def unit : Vector[D]
=======
  def unit : T
>>>>>>> 8896d427524cee7181dfbcf806c82c2213a383f7:src/main/com/siigna/util/geom/Vector.scala

}

/**
 * Utility functions for vectors.
 *
 * @see Vector
 */
object Vector {

  /**
   * Creates a new 2-dimensional vector.
   */
  def apply(x : Double, y : Double) = new Vector2D(x, y)

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
      Vector2D(coordinates(0), coordinates(1))
    } else {
      throw new IllegalArgumentException("Expected a vector, got: " + value)
    }
  }

}
