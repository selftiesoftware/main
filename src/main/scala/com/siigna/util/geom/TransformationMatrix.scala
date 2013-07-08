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

import java.awt.Graphics2D
import java.awt.geom.{AffineTransform, Point2D}

/**
 * <p>
 *   A immutable wrapper class for the AffineTransform-class from AWT. Used for geometrical manipulation of any kind.
 * </p>
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
 *   the [[com.siigna.app.view.View]] the screen you are currently looking at have a coordinate space of (0, 0) to
 *   (width, height). The coordinates of a drawing originates in (0, 0) (the center) and resizes dynamically, depending
 *   on how big the drawing is. Theoretically the drawing can resize to the maximum value of a Double. Put simply, we
 *   run into trouble when we try to translate the screen-coordinates to the drawing-coordinates. A job the
 *   [[com.siigna.util.geom.TransformationMatrix]] excels in, because it can move and scale, depending on what you as
 *   a user wants to see.
 * </p>
 * <p>
 *   So if you need to translate a screen-coordinate <i>from</i> the screen and <i>into</i> a drawing coordinate, use
 *   the <code>deviceTransformation</code> method in the [[com.siigna.app.view.View]] (for instance a mouse position
 *   into a drawing-coordinate), but if you need to take a shape <i>from</i> the drawing and draw it on the screen, use
 *   the <code>drawingTransformation</code> method in the [[com.siigna.app.view.View]].
 * </p>
 *
 * <h2>Use cases</h2>
 * <h3>Transforming a shape <i>from</i> drawing-coordinates and <i>to</i> screen-coordinates (or device-coordinates).
 * {{{
 *   // Some random shape
 *   val shape = ...
 *
 *   // Get the drawing transformation
 *   val transformation = View.drawingTransformation
 *
 *   // Transform a shape
 *   val newShape = shape.transform(transformation)
 *
 *   // The shape can now be rendered on the screen via the <code>draw</code> method in our Graphics object (see below)
 * }}}
 * <h3>Transforming a mouse-coordinate <i>from</i> screen-coordinates <i>to</i> drawing-coordinates.
 * {{{
 *   // A random mouse event
 *   val event = ...
 *
 *   // Extract the mouse position
 *   val position = event.position
 *
 *   // Get the device transformation
 *   val transformation = View.deviceTransformation
 *
 *   // Transform the coordinate
 *   val newPosition = position.transform(transformation)
 * }}}
 * <h3>Transform a shape by your own TransformationMatrix</h3>
 * {{{
 *   // A random shape
 *   val shape = ...
 *
 *   // Your own matrix, that moves everything by 100 units to the right and scales it by a factor of 2
 *   val transformation = TransformationMatrix( Vector2D(100, 0), 2)
 *
 *   // Apply the matrix to the shape
 *   val newShape = shape.transform(transformation)
 * }}}
 * <h3>Advanced use</h3>
 * <p>
 *   Of course the matrix can be used to perform
 *   other more complex geometric manipulations, many of which can probably be found in the java API for the
 *   <code>java.awt.geom.AffineTransform</code> class. The TransformationMatrix wraps the AffineTransform class, so
 *   if you wish to perform more advanced manipulations, you should define your own AffineTransform class and feed
 *   it into the TransformationMatrix.
 * </p>
 *
 * @param t  The transformation matrix derived from AWT.
 * @see [[com.siigna.app.view.View]], [[com.siigna.app.view.Graphics]], [[com.siigna.util.event.MouseEvent]]
 */
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
   * Examines if this Transformation is not empty, that is <i>i</i> an identity-transformation, as opposed to the
   * <code>isEmpty</code> method.
   * @return True if the transformation is not identity, false otherwise.
   */
  def isDefined = !t.isIdentity

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
  def translation = Vector2D(t.getTranslateX, t.getTranslateY)

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
   * Thanks to: <a href="https://groups.google.com/forum/?fromgroups#!topic/uw.cs.cs349/gpaYRPQggvc">
   *   https://groups.google.com/forum/?fromgroups#!topic/uw.cs.cs349/gpaYRPQggvc
   * </a>
   * @return  A Double from 0 to 360.
   */
  def rotation = math.toDegrees(Math.atan2(t.getShearY, t.getScaleY))

  /**
   * Scales a transformation by a factor and concatenates it with this
   * transformation.
   */
  def scale(factor : Double) = TransformationMatrix(operation(_ scale(factor, factor)))

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
   * Returns the scale (zoom) factor of <i>the x-axis</i> of this transformation. If you are looking for
   * the scale-factor of the y-axis, please refer to [[com.siigna.util.geom.TransformationMatrix#scaleY]].
   * @return  A Double representing the scaling operation on the x-axis.
   */
  def scale : Double = {
    val a = new Array[Double](4)
    t.getMatrix(a)
    Math.sqrt(a(0) * a(0) + a(1) * a(1))
  }

  /**
   * Returns the scale (zoom) factor of <i>the x-axis</i> of this transformation. If you are looking for
   * the scale-factor of the y-axis, please refer to [[com.siigna.util.geom.TransformationMatrix#scaleY]].
   * @return  A Double representing the scaling operation on the x-axis.
   */
  def scaleX = scale

  /**
   * Returns the scale (zoom) factor of <i>of the y-axis</i> of this transformation. If you are looking for the
   * scale-factor of the x-axis, please refer to [[com.siigna.util.geom.TransformationMatrix#scaleX]].
   * @return  A Double representing the scaling operation on the y-axis.
   */
  def scaleY = {
    val a = new Array[Double](4)
    t.getMatrix(a)
    Math.sqrt(a(2) * a(2) + a(3) * a(3))
  }

  /**
   * Prints the affine transform.
   */
  override def toString = s"TransformationMatrix($translation, Scale: $scale, Rotation: $rotation)"

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
 * A companion object to the TransformationMatrix. Used to simplify construction of the
 * [[com.siigna.util.geom.TransformationMatrix]] case class, and cache an empty matrix for repeated use.
 */
object TransformationMatrix {

  /**
   * Creates a transformation matrix that doesn't move, scale or rotate anything.
   * @return  An empty [[com.siigna.util.geom.TransformationMatrix]] which, when applied, does nothing. Also known
   *          as a unit matrix.
   */
  def apply() : TransformationMatrix = empty

  /**
   * Creates a transformation matrix which moves and scales by the given params.
   * @param pan  The value by which the matrix should pan.
   * @param zoom  The value by which the matrix should zoom.
   * @return  A [[com.siigna.util.geom.TransformationMatrix]] which scales by the given zoom and moves by the given pan.
   */
  def apply(pan : Vector2D, zoom : Double) : TransformationMatrix =
    new TransformationMatrix() translate(pan) scale(zoom)

  /**
   * Creates a transformation matrix which moves by the given pan vector.
   * @param pan  The value by which the matrix should pan.
   * @return A [[com.siigna.util.geom.TransformationMatrix]] that, when applied, moves elements by the given pan
   *         [[com.siigna.util.geom.Vector]].
   */
  def apply(pan : Vector2D) : TransformationMatrix =
    new TransformationMatrix() translate(pan)

  /**
   * Retrieves a transformation matrix that doesn't move, scale or rotate anything.
   * @return  An empty [[com.siigna.util.geom.TransformationMatrix]] which, when applied, does nothing. Also known
   *          as a unit matrix.
   */
  val empty = new TransformationMatrix()

}
