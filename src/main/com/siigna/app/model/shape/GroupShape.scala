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

package com.siigna.app.model.shape

import com.siigna.app.model.Model
import com.siigna.util.collection.Attributes
import com.siigna.util.geom.{TransformationMatrix, Vector2D}

/**
 * A Group that contains <b>references</b> to other shape and is thus only used as a container.
 *
 * @param ids  a set of ID's that refer to shapes in the model.
 * @param attributes  Attributes to be applied on the shapes in the collection.
 */
case class GroupShape(ids : Seq[String], attributes : Attributes) extends Shape with collection.generic.SeqForwarder[ImmutableShape]
{

  type T = GroupShape

  /**
   * The boundary of the group.
   */
  lazy val boundary = shapes.map(_.boundary).reduceLeft((a, b) => a.expand(b))

  /**
   * An instance of the shapes referred to by the <code>ids</code> sequence.
   */
  private lazy val shapes : Seq[ImmutableShape] = ids.map(Model.immutable.apply)

  /**
   * Adds a shape to the group.
   */
  def + (id : String) : GroupShape  = copy(ids :+ id)

  /**
   * Adds several shapes to the group.
   */
  def + (id1 : String, id2 : String, idx : String*) : GroupShape = this.+(id1).+(id2).++(idx)

  /**
   * Adds a number of shapes to the group.
   */
  def ++ (xs : TraversableOnce[String]) : GroupShape  = copy(ids ++ xs)

  /**
   * Removes a shape from the group.
   */
  def - (id : String) : GroupShape  = copy(ids.filterNot(_ == id))

  /**
   * Removes several shapes from the group.
   */
  def - (id1 : String, id2 : String, idx : String*) : GroupShape  = this.-(id1).-(id2).--(idx)

  /**
   * Removes a number of shapes from the group.
   */
  def -- (xs : TraversableOnce[String]) : GroupShape  = copy(ids.diff(xs.toSeq))

  /**
   * Calculates the closest distance to the group in the given scale.
   */
  def distanceTo(point : Vector2D, scale : Double) : Double = shapes.map(_.distanceTo(point, scale)).reduceLeft(math.min)

  /**
   * The underlying shapes as required by the SeqForwarder trait.
   */
  def underlying = shapes

  /**
   * Returns a new collection with a new set of attributes. In other words return a collection with a new id,
   * but otherwise the same attributes.
   */
  def setAttributes(attributes : Attributes) : GroupShape = copy(attributes = attributes)

  /**
   * Applies a transformation to the shape.
   * TODO: Make a pivotal point for the groupshape?
   */
  def transform(transformation : TransformationMatrix) = this

}

/**
 * Object used for quick access to and constructor overloading for the <code>GroupShape</code> class.
 */
object GroupShape {

  def apply(ids : Traversable[String]) = new GroupShape(ids.toSeq, Attributes())

}