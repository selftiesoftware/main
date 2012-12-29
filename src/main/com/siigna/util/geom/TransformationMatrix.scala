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

import java.awt.Graphics2D
import java.awt.geom.{AffineTransform, Point2D}

/**
 * A wrapper class for the AffineTransform-class from AWT. Used for geometrical manipulation of any kind.
 * <h2>Geometric transformation</h2>
 * <p>
 *   First and foremost the TransformationMatrix is used to transform geometries: translation (moving),
 *   scaling (zooming) and rotation. The mathematics behind can be found at
 *   <a href="http://en.wikipedia.org/wiki/Transformation_matrix">Wikipedia</a>. What that means for us is that
 *   we can keep track of all the geometric manipulations one place. And that's handy!
 * </p>
 *
 * <h2>Using the TransformationMatrix for drawing</h2>
 * <p>
 *   The TransformationMatrix is also handy when it comes to translating between coordinate-systems. As described in
 *   the [[com.siigna.app.view.View]] the screen you are currently looking on have a coordinate space of (0, 0) to
 *   (width, height). A drawing has one that originates in the (0, 0) (the center) and resizes dynamically. This causes
 *   some issues when we try to translate one to the other. So if you need to translate a screen-coordinate into a
 *   drawing coordinate use the <code>deviceTransformation</code> method in the [[com.siigna.app.view.View]] (for
 *   instance a mouse position into a drawing-coordinate), but if you need to take a shape from the drawing and draw
 *   it upon the screen, use the <code>drawingTransformation</code> method in the [[com.siigna.app.view.View]].
 * </p>
 *
 * @param t  The transformation matrix derived from AWT.
 */
@SerialVersionUID(-1812748115)
case class TransformationMatrix(t : AffineTransform) {

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
  def inverse = if (t.getDeterminant != 0) TransformationMatrix(t createInverse) else this

  /**
   * Examines if this TransformationMatrix is "empty" that is an identity-transformation.
   * @return True if no transformations are made by this transformation or False if any scale, rotation or translation has been set.
   */
  def isEmpty = t.isIdentity

  /**
   * Determines whether this transformation y-axis has been flipped.
   */
  def isFlippedY = (t.getScaleY < 0)

  /**
   * Returns the origin of the TransformationMatrix as a Vector.
   */
  def getTranslate = Vector2D(t.getTranslateX, t.getTranslateY)

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
    TransformationMatrix(operation(_ rotate(degrees / 180 * scala.math.Pi, point.x, point.y)))

  /**
   * Calculates the rotation for this transformation-matrix from the translated origin. Given in degrees,
   * counter clockwise.
   * @return  A Double from 0 to 360.
   */
  def rotation = {
    val v = Vector2D(1, 0)             // The vector
    val t = translate(-getTranslate) // Extract translation
    val p = v.transform(t)           // Transform the vector
    math.atan2(p.y, p.x)
  }

  /**
   * Scales a transformation by a factor and concatenates it with this
   * transformation.
   */
  def scale(factor : Double) =
    TransformationMatrix(operation(_ scale(factor, factor)))

  /**
   * Scales a transformation by a factor on the first dimension and a factor on the second dimension.
   * Before and after applying the transformation we translate the matrix to the given point so the
   * transformation is done with the point as its center. This allows for funky mirror- or
   * 1d-transformations.
   * @param xFactor  The scale for the first dimension
   * @param yFactor  The scala for the second dimension
   * @param point  The base point for the scale transformation
   * @return  A new scaled TransformationMatrix
   */
  def scale(xFactor : Double, yFactor : Double, point : Vector2D) =
    TransformationMatrix(operation(_ scale(xFactor, yFactor), point))

  /**
   * Scales a transformation by a factor, but translates it before and after so the scale operation
   * is based in the given coordinates.
   * @param factor  The factor with which to scale
   * @param point  The base point of the scale
   * @return  A new scale TransformationMatrix
   */
  def scale(factor : Double, point : Vector2D) =
    TransformationMatrix(operation(a => {
      a.translate(point.x, point.y)
      a.scale(factor, factor)
      a.translate(-point.x, -point.y)
    }))

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
   *
   * @param delta  The translation to do describes as a [[com.siigna.util.geom.Vector2D]]
   */
  def translate(delta : Vector2D) = TransformationMatrix(operation(_ translate(delta.x, delta.y)))

  /**
   * Creates a translation (panning) on the x-axis and concatenates it with this
   * transformation.
   *
   * @param delta  The translation of the x-axis
   */
  def translateX(delta : Double) = TransformationMatrix(operation(_ translate(delta, 0)))

  /**
   * Creates a translation (panning) on the y-axis and concatenates it with this
   * transformation.
   *
   * @param delta  The translation of the y-axis
   */
  def translateY(delta : Double) = TransformationMatrix(operation(_ translate(0, delta)))
  
  /**
   * Performs an operation on the affine transformation which is wrapped by this
   * transformation.
   * @param op  The operation to perform
   */
  protected def operation(op : AffineTransform => Unit) = {
    val newAffineTransform = t.clone.asInstanceOf[AffineTransform]
    op(newAffineTransform)
    newAffineTransform
  }

  /**
   * Performs an operation on the affine transform beneath this TransformationMatrix but translates the
   * matrix before and after the operation so the given point becomes the base for the operation.
   * @param op  The operation to perform
   * @param point  The base point for the operation
   */
  protected def operation(op : AffineTransform => Unit, point : Vector2D) = {
    val newAffineTransform = t.clone.asInstanceOf[AffineTransform]
    newAffineTransform.translate(point.x, point.y)
    op(newAffineTransform)
    newAffineTransform.translate(-point.x, -point.y)
    newAffineTransform
  }

}

/**
 * A companion object to the TransformationMatrix.
 */
object TransformationMatrix {

  /**
   * Creates a transformation matrix that doesn't move, scale or rotate anything.
   */
  def apply() : TransformationMatrix = empty

  /**
   * Creates a transformation matrix which moves and rotates by the given params.
   * @param pan  The value by which the matrix should pan.
   * @param zoom  The value by which the matrix should zoom.
   */
  def apply(pan : Vector2D, zoom : Double) : TransformationMatrix =
    new TransformationMatrix() translate(pan) scale(zoom)

  /**
   * Creates a transformation matrix that doesn't move, scale or rotate anything.
   */
  val empty = new TransformationMatrix()

}
