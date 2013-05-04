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
 *   A Selection is a wrapper for one or more [[com.siigna.app.model.shape.Shape]](s) and
 *   [[com.siigna.app.model.selection.ShapeSelector]](s). It is used to describe which shapes have been selected and
 *   how they have been selected. The shapes are represented by their id and the shape itself (used to initialize
 *   [[com.siigna.app.model.action.Action]]s for latter application to the [[com.siigna.app.model.Model]]), while the
 *   selection is represented by a [[com.siigna.app.model.selection.ShapeSelector]], which describes how the shape
 *   was selected.
 * </p>
 * <p>
 *   The Selection is also used to dynamically manipulate the shapes via [[com.siigna.util.collection.Attributes]] or
 *   transform them via a [[com.siigna.util.geom.TransformationMatrix]] for later application to the corresponding
 *   shapes in the [[com.siigna.app.model.Model]]. This is particularly useful if a user only wants to manipulate parts
 *   of one or more shapes and not the entire shapes, or if a user wishes to do several changes before deciding on the
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
 * @see [[com.siigna.app.model.selection.ShapeSelector]]
 */
trait Selection extends HasAttributes with MapProxy[Int, (Shape, ShapeSelector)] with Externalizable {

  type T = Selection

  /**
   * Adds a single shape along with its part to the selection.
   * @param id  The id of the shape in the [[com.siigna.app.model.Model]].
   * @param selector  The part to select from the shape.
   * @return  The resulting selection from the merge.
   */
  def add(id : Int, selector : (Shape, ShapeSelector)) : Selection

  /**
   * Adds a number of shapes to the selection.
   * @param parts  The parts to add consisting of the shape ids paired with the shape to select and the part to select
   *               from that given shape.
   * @return  The new selection after the merge.
   */
  def add(parts : Map[Int, (Shape, ShapeSelector)]) : Selection

  /**
   * The boundary of the underlying ImmutableShapes.
   * @return A Rectangle2D describing the smallest rectangle possible, that includes all the shapes in the selection.
   */
  def boundary : Rectangle2D

  /**
   * Calculates the distance from the vector and to the underlying [[com.siigna.app.model.shape.Shape]]s in
   * scale 1. This is equivalent to calling <code>distanceTo(point, 1)</code>.
   * @param point  The point to calculate the distance to.
   * @return  The length from the closest point of this shape to the point, in the given scale.
   */
  def distanceTo(point : Vector2D) : Double = distanceTo(point, 1)

  /**
   * Calculates the distance from the vector and to the underlying [[com.siigna.app.model.shape.Shape]]s in the
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
   * Retrieves the active parts of the selected shapes in the current [[com.siigna.app.model.selection.Selection]].
   * Useful if you need to draw the active parts only, for instance.
   * @return  A collection of sub-parts of active shapes. The shapes that does not have a meaningful selection are
   *          left out.
   */
  def parts : Traversable[Shape]

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
  def transform(transformation: TransformationMatrix) : Selection

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
 *               the [[com.siigna.app.model.selection.ShapeSelector]] that describes the selection.
 */
case class NonEmptySelection(selection : Map[Int, (Shape, ShapeSelector)]) extends Selection {

  /**
   * The attributes to be applied on the selection when deselected. Initially empty.
   * @return  An [[com.siigna.util.collection.Attributes]] object.
   */
  var attributes : Attributes = Attributes.empty

  def add(id: Int, part: (Shape, ShapeSelector)) : Selection = {
    NonEmptySelection( selection +
      (id ->
        (selection.get(id) match {
          case Some(p : (Shape, ShapeSelector)) => p._1 -> (p._2 ++ part._2)
          case _ => part
        })
      )
    )
  }

  def add(parts: Map[Int, (Shape, ShapeSelector)]) : Selection = {
    def mergePart(t : (Int, (Shape, ShapeSelector))) : (Int, (Shape, ShapeSelector)) = {
      (t._1 -> (selection.get(t._1) match {
        case Some(s) => s._1 -> (s._2 ++ t._2._2)
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

  def parts = {
    selection.map(t => t._2._1.getShape(t._2._2)).flatten.map(_.setAttributes(attributes).transform(transformation))
  }

  def shapes : Map[Int, Shape] = selection.map(t => t._1 -> t._2._1.addAttributes(attributes).transform(transformation)).toMap

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

  def vertices = selection.values.map(t => t._1.geometry.vertices).flatten

}

/**
 * A [[com.siigna.app.model.selection.Selection]] that does not contain any shapes. Used for optimization purposes
 * to re-use references to (the empty) inner members.
 */
case object EmptySelection extends Selection {

  val attributes = Attributes.empty

  def add(id: Int, part : (Shape, ShapeSelector)) : Selection = NonEmptySelection(Map(id -> part))

  def add(parts: Map[Int, (Shape, ShapeSelector)]): Selection = NonEmptySelection(parts)

  def boundary = SimpleRectangle2D(0, 0, 0, 0)

  def parts = Nil

  def selectedShapes = Nil

  def self = Map.empty[Int, (Shape, ShapeSelector)]

  def shapes = Map.empty[Int, Shape]

  def vertices = Traversable.empty

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
   *               paired with the shape itself and the [[com.siigna.app.model.selection.ShapeSelector]] to select from
   *               that given shape.
   * @return  A [[com.siigna.app.model.selection.Selection]] that can either be full or empty.
   */
  def apply(parts : Map[Int, (Shape, ShapeSelector)]) = if (parts.isEmpty) EmptySelection else new NonEmptySelection(parts)

  /**
   * Creates a [[com.siigna.app.model.selection.Selection]] containing the given part.
   * @param part  The single part to select, describing the id of the shape to select, the shape itself and the
   *              part of the shape to select via a [[com.siigna.app.model.selection.ShapeSelector]].
   * @return A [[com.siigna.app.model.selection.Selection]] containing the selection.
   */
  def apply(part : (Int, (Shape, ShapeSelector))) = NonEmptySelection(Map(part))

}