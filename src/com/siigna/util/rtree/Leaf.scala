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

package com.siigna.util.rtree

import com.siigna.util.rtree.Leaf.Leaf1

/**
 * An immutable leaf in a prioritized R-tree containing up to <code>branchFactor</code> elements.
 *
 * @tparam T  The type of the elements to pair with the MBR.
 *
 * @author Jens Egholm <jensep@gmail.com>
 */
trait Leaf[T] extends Node[T] {

  def add(key : MBR, elem : T) : Leaf[T]

  def remove(elem : T) : Node[T]

}

/**
 * Creates leafs that fits the given elements in size.
 */
object Leaf {

  /**
   * An empty leaf with no elements and an empty MBR.
   */
  class EmptyLeaf[T](branchFactor : Int, ordering : MBROrdering) extends Leaf[T] {
    def add(key : MBR, value : T) = new Leaf1(key, value, branchFactor, ordering)
    def apply(mbr : MBR) = Iterator.empty
    val mbr = MBR.empty
    def remove(value : MBR) = this
    val size = 0
    val toString = "Empty Leaf"
  }

  /**
   * A leaf with one element.
   */
  class Leaf1[T](key : MBR, value : T, branchFactor : Int, ordering : MBROrdering) extends Leaf[T] {
    def add(key2 : MBR, value2 : T) = new Leaf2(key, value, key2, value2, branchFactor, ordering)
    def apply(mbr : MBR) = if (this.mbr.overlap(mbr)) Iterator(value) else Iterator.empty
    val mbr = key
    def remove(key : MBR, value : MBR) = if (this.value == value) new EmptyLeaf(branchFactor, ordering) else this
    val size = 1
    val toString = "Leaf[ " + key + " -> " + value + " ]"
  }

  /**
   * A leaf with two elements.
   */
  class Leaf2[T](key1 : MBR, value1 : T, key2 : MBR, value2 : T, branchFactor : Int, ordering : MBROrdering) extends Leaf[T] {

  }

  class LeafN[T](elems : Array[(MBR, T)], branchFactor : Int, ordering : MBROrdering) extends Node[T] {

    /**
     * The elements in the node.
     */
    private var arr = new Array[(MBR, T)](branchFactor)

    /**
     * The MBR of the leaf.
     */
    var mbr = MBR(0, 0, 0, 0)

    /**
     * The number of elements currently stored in the leaf.
     */
    var size = 0

    /**
     * The index of the "worst-case" node defined by the given ordering.
     * <b>Only updated</b> if the size of the leaf eq the branchFactor.
     */
    var worst : Int = 0

    /**
     * Add an element to the leaf. Throws an error if the leaf is full.
     */
    def add(key : MBR, value : T) {
      arr(size) = (key -> value)
      size += 1
      updateWorst()

      // Set MBR
      if (size == 1) mbr = key
      else           mbr = mbr.expand(key)
    }

    /**
     * Examines whether a given mbr is "better" than the current worst case.
     */
    def isBetter(mbr : MBR) = ordering.lt(mbr, arr(worst)._1)

    /**
     * Removes a single element from the leaf. Throw an error if the leaf is empty.
     */
    def remove(value : T) {
      if (arr(0)._2.equals(value)) arr(0) = null
      else if (size > 1 && arr(1)._2.equals(value)) arr(0) = null
      else if (size > 2 && arr(2)._2.equals(value)) arr(0) = null
      else if (size > 3 && arr(3)._2.equals(value)) arr(0) = null
      else if (size > 4 && arr(4)._2.equals(value)) arr(0) = null
      else if (size > 5 && arr(5)._2.equals(value)) arr(0) = null
      else if (size > 6 && arr(6)._2.equals(value)) arr(0) = null
      else if (size > 7 && arr(7)._2.equals(value)) arr(0) = null

      // Set the mbr
      mbr = arr(0)._1.expand(
            arr(1)._1.expand(
            arr(2)._1.expand(
            arr(3)._1.expand(
            arr(4)._1.expand(
            arr(5)._1.expand(
            arr(6)._1.expand(
            arr(7)._1)))))))
    }

    /**
     * Replaces the "worst-case" element in the leaf with another element and returns the replaced element.
     */
    def swap(key : MBR, value : T) = {
      val res = arr(worst)
      arr(worst) = (key -> value)
      updateWorst()
      res
    }

    /**
     * Updates the worst value for the leaf.
     */
    private def updateWorst() {
      if (size == branchFactor) {
        if (ordering.compare(arr(0)._1, arr(worst)._1) <= 0) worst = 0
        if (ordering.compare(arr(1)._1, arr(worst)._1) <= 0) worst = 1
        if (ordering.compare(arr(2)._1, arr(worst)._1) <= 0) worst = 2
        if (ordering.compare(arr(3)._1, arr(worst)._1) <= 0) worst = 3
        if (ordering.compare(arr(4)._1, arr(worst)._1) <= 0) worst = 4
        if (ordering.compare(arr(5)._1, arr(worst)._1) <= 0) worst = 5
        if (ordering.compare(arr(6)._1, arr(worst)._1) <= 0) worst = 6
        if (ordering.compare(arr(7)._1, arr(worst)._1) <= 0) worst = 7
      }
    }

  }
}