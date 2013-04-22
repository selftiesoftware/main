/*
 *
 *  * Copyright (c) 2008-2013. Siigna is released under the creative common license by-nc-sa. You are free
 *  * to Share — to copy, distribute and transmit the work,
 *  * to Remix — to adapt the work
 *  *
 *  * Under the following conditions:
 *  * Attribution —  You must attribute the work to http://siigna.com in the manner specified by the author or licensor (but not in any way that suggests that they endorse you or your use of the work).
 *  * Noncommercial — You may not use this work for commercial purposes.
 *  * Share Alike — If you alter, transform, or build upon this work, you may distribute the resulting work only under the same or similar license to this one.
 *
 */

package com.siigna.app.model.selection

import com.siigna.util.collection.{Attributes, HasAttributes}
import scala.collection.immutable.MapProxy
import com.siigna.app.model.shape.Shape
import java.io.{ObjectInput, NotSerializableException, ObjectOutput, Externalizable}
import com.siigna.util.geom.{SimpleRectangle2D, TransformationMatrix, Vector2D, Rectangle2D}

/**
 * <p>
 *   A Selection is a wrapper for one or more [[com.siigna.app.model.selection.ShapePart]](s). It is used to
 *   describe which shapes have been selected (id) and how they have been selected
 *   (via [[com.siigna.app.model.selection.ShapePart]]).
 *   It is also used to dynamically manipulate the shapes via [[com.siigna.util.collection.Attributes]] or transform
 *   them via a [[com.siigna.util.geom.TransformationMatrix]] for later application to the corresponding shape(parts)
 *   in the [[com.siigna.app.model.Model]]. This is particularly useful if a user only wants to manipulate parts of
 *   one or more shapes and not the entire shapes, or if a user wishes to do several changes before deciding on the
 *   final result to save into the [[com.siigna.app.model.Drawing]].
 * </p>
 *
 * <h2>Use case</h2>
 * <p>
 *   The parts in the Selection are immutable, but the [[com.siigna.util.collection.Attributes]] and
 *   [[com.siigna.util.geom.TransformationMatrix]] for the selected parts are mutable. Any manipulations are stored
 *   continuously each time the selection is transformed or the attributes changed. However, changes are not stored
 *   in the [[com.siigna.app.model.Drawing]] until the user removes the selection and applies the effect to the
 *   [[com.siigna.app.model.Model]]. This happens by selection/deselection in the [[com.siigna.app.model.Drawing]],
 *   via the [[com.siigna.app.model.Drawing#select]] and [[com.siigna.app.model.Drawing#deselect]] methods. When that
 *   happens, the transformations and attributes accumulated in the selection, will be executed as
 *   [[com.siigna.app.model.action.Action]]s and stored in the [[com.siigna.app.model.Drawing]].
 * </p>
 * <p>
 *   Please refer to the [[com.siigna.app.model.SelectableModel]] for examples on uses.
 * </p>
 *
 * @see [[com.siigna.app.model.SelectableModel]]
 * @see [[com.siigna.app.model.selection.ShapePart]]
 * @see [[com.siigna.app.model.selection.ShapePart]]
 */
trait Selection extends HasAttributes with MapProxy[Int, ShapePart[Shape]] with Externalizable {

  type T = Selection

  /**
   * Adds a single shape along with its part to the selection.
   * @param id  The id of the shape in the [[com.siigna.app.model.Model]].
   * @param part  The part to select from the shape.
   * @tparam  T  The type of the shape to add.
   * @return  The resulting selection from the merge.
   */
  def add[T <: Shape](id : Int, part : ShapePart[T]) : Selection

  /**
   * Adds a number of shapes to the selection.
   * @param parts  The parts to add consisting of the shape ids paired with the shape to select and the part to select
   *               from that given shape.
   * @tparam  T  The type of the shapes in the collection.
   * @return  The new selection after the merge.
   */
  def add[T <: Shape](parts : Map[Int, ShapePart[T]]) : Selection

  /**
   * The boundary of the underlying ImmutableShapes.
   * @return A Rectangle2D describing the smallest rectangle possible, that includes all the shapes in the selection.
   */
  def boundary : Rectangle2D

  /**
   * Calculates the distance from the vector and to the underlying [[com.siigna.app.model.selection.ShapePart]]s in
   * scale 1. This is equivalent to calling <code>distanceTo(point, 1)</code>.
   * @param point  The point to calculate the distance to.
   * @return  The length from the closest point of this shape to the point, in the given scale.
   */
  def distanceTo(point : Vector2D) : Double = distanceTo(point, 1)

  /**
   * Calculates the distance from the vector and to the underlying [[com.siigna.app.model.selection.ShapePart]]s in the
   * given scale.
   * @param point  The point to calculate the distance to.
   * @param scale  The scale in which we are calculating.
   * @return  The length from the closest point of this shape to the point, in the given scale.
   */
  def distanceTo(point: Vector2D, scale: Double) : Double

  /**
   * Examines if there are any shapes in the selection. Synonym to <code>!isEmpty</code>.
   * @return  True if any shapes are selected, false if no shapes are selected (the selection is empty).
   */
  def isDefined = size != 0

  /**
   * Retrieves the whole shapes included in the selection, and not just the selected parts of them.
   * @return A map containing the ids and the shapes used in the current selection.
   */
  def shapes : Map[Int, Shape]

  /**
   * Transforms the selected shape-parts by replacing the previously stored transformation with the new.
   * @param transformation  The TransformationMatrix to apply to the parts.
   * @return  The Selection with the transformation applied.
   */
  def transform(transformation: TransformationMatrix) : T

  /**
   * The vertices of the selection, i.e. the selected points, described by a number of
   * [[com.siigna.util.geom.Vector2D]]s.
   * @return A number of vertices.
   */
  def vertices : Traversable[Vector2D]

  def writeExternal(out: ObjectOutput) { throw new NotSerializableException("com.siigna.app.model.Selection") }

  def readExternal(in: ObjectInput) { throw new NotSerializableException("com.siigna.app.model.Selection") }
}

/**
 * A [[com.siigna.app.model.selection.Selection]] which is not empty, that is, contains more than 0 shapes.
 * @param selection  The ids of the wrapped shape(s) paired with the [[com.siigna.app.model.shape.Shape]] itself and
 *               the [[com.siigna.app.model.selection.ShapePart]] to select.
 */
case class NonEmptySelection(selection : Map[Int, ShapePart[Shape]]) extends Selection {

  /**
   * The attributes to be applied on the selection when deselected. Initially empty.
   * @return  An [[com.siigna.util.collection.Attributes]] object.
   */
  var attributes : Attributes = Attributes.empty

  def add[A <: Shape](id: Int, part: ShapePart[A]) : Selection = {
    NonEmptySelection( selection +
      (id ->
        (selection.get(id) match {
          case Some(p : ShapePart[A]) => p.add(part.selector)
          case _ => part
        })
      )
    )
  }

  def add[A <: Shape](parts: Map[Int, ShapePart[A]]) : Selection = {
    def mergePart(t : (Int, ShapePart[A])) : (Int, ShapePart[A]) = {
      (t._1 -> (selection.get(t._1) match {
        case Some((s : A, p : ShapePart[A])) => p.add(t._2.selector)
        case _ => t._2
      }))
    }
    NonEmptySelection( selection ++ parts.map(t => mergePart(t)))
  }

  def boundary = shapes.values.foldLeft(shapes.head._2.boundary)((a, b) => a.expand(b.boundary))

  def distanceTo(point: Vector2D, scale: Double) = {
    shapes.values.map(_.distanceTo(point)).reduceLeft((a, b) => if (a < b) a else b) * scale
  }

  def self = selection

  def selectedShapes = {
    shapes.values.map(_.setAttributes(attributes).transform(transformation))
  }

  def shapes : Map[Int, Shape] = selection.map(t => t._1 -> t._2.shape.addAttributes(attributes).transform(transformation)).toMap

  def setAttributes(attributes: Attributes) = { this.attributes ++= attributes; this}

  override def toString() = s"Selection($transformation, $attributes)[$self]"

  /**
   * The transformation applied to the selection so far, that will be applied on the [[com.siigna.app.model.Drawing]]
   * when deselected. Initially empty.
   * @return  The [[com.siigna.util.geom.TransformationMatrix]] of the selection.
   */
  var transformation : TransformationMatrix = TransformationMatrix.empty

  /**
   * Transforms the selected shape-parts by replacing the previously stored transformation with the new.
   * @param transformation  The TransformationMatrix to apply to the parts.
   * @return  The Selection with the transformation applied.
   */
  def transform(transformation: TransformationMatrix) = {
    this.transformation = transformation
    this
  }

  def vertices = selection.values.map(t => t.vertices).flatten

}

/**
 * A [[com.siigna.app.model.selection.Selection]] that does not contain any shapes. Used for optimization purposes
 * to re-use references to (the empty) inner members.
 */
case object EmptySelection extends Selection {

  val attributes = Attributes.empty

  def add[T <: Shape](id: Int, part : ShapePart[T]) : Selection = NonEmptySelection(Map(id -> part))

  def add[T <: Shape](parts: Map[Int, ShapePart[T]]): Selection = NonEmptySelection(parts)

  def boundary = SimpleRectangle2D(0, 0, 0, 0)

  val selectedShapes = Nil

  val self = Map.empty[Int, ShapePart[Shape]]

  val shapes = Map.empty[Int, Shape]

  val vertices = Traversable.empty

  def distanceTo(point: Vector2D, scale: Double) : Double = Double.MaxValue

  def setAttributes(attributes: Attributes) = this

  override def toString() = "Empty Selection"

  def transform(transformation : TransformationMatrix) = this
}

/**
 * A companion object to a [[com.siigna.app.model.selection.Selection]].
 */
object Selection {

  /**
   * An empty selection containing no shapes or parts.
   */
  val empty = EmptySelection

  /**
   * Creates a [[com.siigna.app.model.selection.Selection]] containing the given parts. If no parts exist, we return an
   * [[com.siigna.app.model.selection.EmptySelection]] otherwise a [[com.siigna.app.model.selection.NonEmptySelection]].
   * @param parts  The parts to insert into the selection, that is the id of the [[com.siigna.app.model.shape.Shape]]s
   *               paired with the shape itself and the [[com.siigna.app.model.selection.ShapePart]] to select from that
   *               given shape.
   * @return  A [[com.siigna.app.model.selection.Selection]] that can either be full or empty.
   */
  def apply(parts : Map[Int, ShapePart[Shape]]) = if (parts.isEmpty) EmptySelection else new NonEmptySelection(parts)

}