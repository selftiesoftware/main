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

import java.awt.Graphics2D
import java.awt.geom.{AffineTransform, Point2D}

/**
 * A wrapper class for the AffineTransform-class from AWT.
 * Used for geometrical manipulation of any kind.
 * TODO: Something is horribly wrong with concatenation!
 * Test-case: Vector(0, 0).flipY = Vector(0, 184) !!
 * Later..: Can't find the problem...
 *
 * @param t  The transformation matrix derived from AWT.
 *
 * TODO: Create a 3D representation as well.
 */
case class TransformationMatrix(t : AffineTransform)
{

  def this() = this(new AffineTransform)

  /**
   * Concatenates this transformation with another TransformationMatrix.
   */
  def concatenate(that : TransformationMatrix) =
    TransformationMatrix(operation(_ concatenate(that t)))

  /**
   * Flips the X-axis. It is concatenated with this transformation.
   */
  def flipX : TransformationMatrix =
    TransformationMatrix(operation(_ scale(-1, 1)))

  /**
   * Flips the X-axis around the point.
   */
  def flipX(point : Vector2D) : TransformationMatrix =
    translate(point).flipX.translate(-point)

  /**
   * Flips the Y-axis. It is concatenated with this transformation.
   */
  def flipY : TransformationMatrix =
    TransformationMatrix(operation(_ scale(1, -1)))

  /**
   * Flips the Y-axis around a point.
   */
  def flipY(point : Vector2D) : TransformationMatrix =
    translate(point).flipY.translate(-point)

  /**
   * Creates a TransformationMatrix that performs the inverse operation of
   * this transformation.
   */
  def inverse =
    TransformationMatrix(t createInverse)

  /**
   * Determines whether this transformation y-axis has been flipped.
   */
  def isFlippedY = (t.getScaleY < 0)

  /**
   * Returns the origin of the TransformationMatrix as a Vector.
   */
  def getTranslate = Vector(t.getTranslateX, t.getTranslateY)

  /**
   * Creates a rotation in degrees around (0, 0) and concatenates it with
   * this transformation.
   */
  def rotate(degrees : Double) =
    TransformationMatrix(operation(_ rotate(degrees / 180 * scala.math.Pi)))

  /**
   * Creates a rotation in degrees around a point and concatenates it with
   * this transformation.
   */
  def rotate(degrees : Double, point : Vector2D) =
    TransformationMatrix(operation(_ rotate(degrees / 180 * scala.math.Pi, point x, point y)))

  /**
   * Scales a transformation by a factor and concatenates it with this
   * transformation.
   */
  def scale(factor : Double) =
    TransformationMatrix(operation(_ scale(factor, factor)))

  /**
   * Returns the scale (zoom) factor of this transformation.
   */
  def scaleFactor = t.getScaleX

  /**
   * Prints the affine transform.
   */
  override def toString = {
    t.toString
  }

  /**
   * Transforms a vector with this transformation.
   */
  def transform(point : Vector2D) : Vector2D = {
    val src = new Point2D.Double(point x, point y)
    val dst = new Point2D.Double(0, 0)
    t transform(src, dst)
    Vector2D(dst getX, dst getY)
  }

  /**
   * Transforms a graphics2D-object with this transformation.
   */
  def transform(graphics : Graphics2D) {
    graphics transform(t)
  }

  /**
   * Creates a translation (panning) and concatenates it with this
   * transformation.
   */
  def translate(delta : Vector2D) = TransformationMatrix(operation(_ translate(delta.x, delta.y)))
  
  /**
   * Performs a operation on the affine transformation which is wrapped by this
   * transformation.
   */
  protected def operation(op : AffineTransform => Unit) = {
    val newAffineTransform = t.clone.asInstanceOf[AffineTransform]
    op(newAffineTransform)
    newAffineTransform
  }

}

/**
 * A shortcut to instantiate a TransformationMatrix.
 */
object TransformationMatrix
{

  def apply() : TransformationMatrix = new TransformationMatrix
  def apply(pan : Vector2D, zoom : Double) : TransformationMatrix =
    new TransformationMatrix() translate(pan) scale(zoom)

}
