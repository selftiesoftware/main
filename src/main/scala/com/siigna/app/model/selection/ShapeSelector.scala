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

package com.siigna.app.model.selection

import scala.collection.immutable.{SetProxy, BitSet}

/**
 * A ShapeSelector describes how a shape can be selected. This class exists so we can
 * describe all the different combinations a [[com.siigna.app.model.shape.Shape]] can be selected, and so we can
 * access sub-sets (or parts) of a shape many times without creating new instances of the shape part.
 * The ShapeSelector itself does not contain any logic, which makes it suitable for storage and remote I/O via
 * [[com.siigna.app.model.action.Action]]s.x
 */
trait ShapeSelector extends ((Int) => Boolean) {

  /**
   * Concatenates the given selection with the current selector.
   * @param that  The other selector to concatenate with this.
   * @return  A new ShapeSelector that is the union of the two selectors.
   */
  def ++(that : ShapeSelector) : ShapeSelector

  /**
   * Subtracts a given selection from this current selection.
   * @param that  The other selection to subtract from this.
   * @return  A new ShapeSelector with the given selector subtracted.
   */
  def --(that : ShapeSelector) : ShapeSelector

}

/**
 * Companion object to the [[com.siigna.app.model.selection.ShapeSelector]].
 */
object ShapeSelector {

  /**
   * Returns an empty [[com.siigna.app.model.selection.ShapeSelector]] that represents a selection that does not
   * contain any part of a shape.
   * @return  An [[com.siigna.app.model.selection.EmptyShapeSelector]].
   */
  def apply() = EmptyShapeSelector

  /**
   * Creates a ShapeSelector selecting the given points, represented by their indexes as defined in the
   * [[com.siigna.app.model.shape.Shape]].
   * @param xs  The points to add to the selector.
   * @return  A [[com.siigna.app.model.selection.BitSetShapeSelector]] that describes the selection of the given points.
   */
  def apply(xs : Int*) = BitSetShapeSelector(BitSet(xs:_*))

  /**
   * Creates a ShapeSelector selecting all the vertices given in the set ´<code>set</code>´.
   * @param set  The list of indexes to include in the selector.
   * @return A [[com.siigna.app.model.selection.BitSetShapeSelector]] that describes the selected points from the given
   *         BitSet.
   */
  def apply(set : BitSet) = BitSetShapeSelector(set)

  /**
   * Gives a selector that describes an empty selection of a [[com.siigna.app.model.shape.Shape]]. I. e. a selection
   * that contains nothing of a shape.
   * @return  An [[com.siigna.app.model.selection.EmptyShapeSelector]].
   */
  def empty = EmptyShapeSelector

  /**
   * Gives a selector that describes a fully selected [[com.siigna.app.model.shape.Shape]].
   * @return  A [[com.siigna.app.model.selection.FullShapeSelector]].
   */
  def full = FullShapeSelector

  /**
   * A extractor to match the contents on [[com.siigna.app.model.selection.ShapeSelector]]s, in particular
   * [[com.siigna.app.model.selection.BitSetShapeSelector]] (since it comprises of a BitSet => Set of Ints).
   * Useful for examining the content of a selector.
   *
   * === Example ===
   * {{{
   *   ShapeSelector(1, 2) match {
   *     case ShapeSelector(2, 1)    => // Not a match
   *     case ShapeSelector(1, 2, 3) => // Not a match
   *     case ShapeSelector(1, 2)    => // Match!
   *     case _ => // Not a match
   *   }
   * }}}
   *
   * @param selector  The selector to extract the indices from.
   * @return  None if the selector is an instance of [[com.siigna.app.model.selection.FullShapeSelector]] or
   *          [[com.siigna.app.model.selection.EmptyShapeSelector]], Some(elements) otherwise.
   */
  def unapplySeq(selector : ShapeSelector) : Option[Seq[Int]] = selector match {
    case BitSetShapeSelector(xs) if (!xs.isEmpty) => Some(xs.toSeq)
    case _ => None
  }

}

/**
 * A [[com.siigna.app.model.selection.ShapeSelector]] that describe a selection of a
 * [[com.siigna.app.model.shape.Shape]]s via a BitSet. The selection is represented by the BitSet bit-mask, where
 * a point of the selector is included if the index of the point exists in the bitset.
 * @param bits  The BitSet representing the bitmask of the selection.
 */
case class BitSetShapeSelector(bits : BitSet) extends ShapeSelector with SetProxy[Int] {
  def self = bits
  def ++(that: ShapeSelector): ShapeSelector = that match {
    case FullShapeSelector => FullShapeSelector
    case BitSetShapeSelector(xs) => BitSetShapeSelector(bits ++ xs)
    case _ => this
  }
  def --(that: ShapeSelector): ShapeSelector = that match {
    case EmptyShapeSelector => this
    case BitSetShapeSelector(xs) => {
      val ys = bits -- xs
      if (ys.isEmpty) {
        EmptyShapeSelector
      } else {
        BitSetShapeSelector(ys)
      }
    }
  }
}

/**
 * An EmptyShapeSelector is a [[com.siigna.app.model.selection.ShapeSelector]] with no information and thus represents
 * an empty Shape subset.
 */
case object EmptyShapeSelector extends ShapeSelector {
  def apply(i : Int) = false
  def ++(that: ShapeSelector): ShapeSelector = that
  def --(that: ShapeSelector): ShapeSelector = this
}

/**
 * A FullShapeSelector is a [[com.siigna.app.model.selection.ShapeSelector]] containing the entire shape. No
 * sub-selection magic is needed.
 */
case object FullShapeSelector extends ShapeSelector {
  def apply(i : Int) = true
  def ++(that: ShapeSelector): ShapeSelector = this
  def --(that: ShapeSelector): ShapeSelector = that match {
    case FullShapeSelector | BitSetShapeSelector(_) => EmptyShapeSelector
    case _ => this
  }
}
