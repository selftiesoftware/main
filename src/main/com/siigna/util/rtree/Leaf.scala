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

package com.siigna.util.rtree

import com.siigna.util.geom.SimpleRectangle2D

/**
 * An immutable leaf in a prioritized R-tree containing up to <code>branchFactor</code> elements.
 *
 * @author Jens Egholm <jensep@gmail.com>
 */
trait Leaf extends Node {

  /**
   * Adds a single element to the Node.
   */
  def add(elem : (Int, SimpleRectangle2D)) : Leaf

  /**
   * Adds a number of elements to the leaf. If the number of elements plus the size of the leaf
   * exceeds the branchFactor, an error is thrown.
   * TODO: Optimize
   */
  def add(elems : Traversable[(Int, SimpleRectangle2D)]) : Leaf = {
    if (elems.size + size > branchFactor || elems.isEmpty) {
      throw new IllegalArgumentException("Unable to add to the leaf with " + elems.size + " elements")
    } else if (elems.size + size > 2) { // If we require a LeafN node (more than 2 elements)
      val a = (traversable ++ elems).toArray
      new Leaf.LeafN(a, elems.size + size, branchFactor, ordering)
    } else if (elems.size == 2) {       // If there's 2 elements to add
      val e = elems.toSeq
      new Leaf.Leaf2(e(0)._1, e(0)._2, e(1)._1, e(1)._2, branchFactor, ordering)
    } else {                            // Else there can be only one.....
      if (size == 1) {
        val e = (traversable ++ elems).toSeq
        new Leaf.Leaf2(e(0)._1, e(0)._2, e(1)._1, e(1)._2, branchFactor, ordering)
      } else { // Size must be 0
        val e = elems.toSeq
        new Leaf.Leaf1(e(0)._1, e(0)._2, branchFactor, ordering)
      }
    }
  }
  
  /**
   * Examines whether a given mbr is "better" than the worst case of the leaf.
   */
  def isBetter(query : SimpleRectangle2D) : Boolean
  
  /**
   * The ordering of the leaf.
   */
  def ordering : MBROrdering

  /**
   * Removes a single element from the leaf.
   */
  def remove(elem : Int) : Leaf
  
  /**
   * Removes a number of elements from the leaf.
   * TODO: Optimize
   */
  def remove(elems : Traversable[Int]) : Leaf =
    elems.foldLeft(this)((leaf, elem) => leaf.remove(elem))

  /**
   * A collection that traverses the string-identifiers of the leaf.
   */
  def traversable : Traversable[(Int, SimpleRectangle2D)]
  
  /**
   * Returns the leaf where the given element is updated.
   */
  def updated(elem : (Int, SimpleRectangle2D)) : Leaf
  
  /**
   * The worst case MBR from the given ordering.
   */
  def worst : SimpleRectangle2D

}

/**
 * Creates leafs that fits the given elements in size.
 */
object Leaf {

  /**
   * Creates an empty leaf with a given branch factor and ordering.
   */
  def apply(branchFactor : Int, ordering : MBROrdering) =
     new EmptyLeaf(branchFactor, ordering)

  /**
   * An empty leaf with an empty MBR.
   */
  class EmptyLeaf(val branchFactor : Int, val ordering : MBROrdering) extends Leaf {
    def add(elem : (Int, SimpleRectangle2D)) = new Leaf1(elem._1, elem._2, branchFactor, ordering)
    def apply(query : SimpleRectangle2D) = Traversable.empty
    def isBetter(query : SimpleRectangle2D) = true
    val mbr = null // TODO: Change this
    def remove(elem : Int) = throw new UnsupportedOperationException("Unable to delete element from empty leaf.")
    val size = 0
    override val toString = "Empty Leaf"
    val traversable = Iterable.empty[(Int, SimpleRectangle2D)]
    def updated(elem : (Int, SimpleRectangle2D)) = throw new UnsupportedOperationException("Unable to update an element on an empty leaf.")
    lazy val worst = null // TODO: Change this
  }

  /**
   * A leaf with one element.
   */
  class Leaf1(key : Int, value : SimpleRectangle2D, val branchFactor : Int, val ordering : MBROrdering) extends Leaf {
    def add(elem : (Int, SimpleRectangle2D)) = new Leaf2(key, value, elem._1, elem._2, branchFactor, ordering)
    def apply(query : SimpleRectangle2D) = if (mbr.intersects(query)) Traversable(key) else Traversable.empty
    def isBetter(query : SimpleRectangle2D) = ordering.lt(query, worst)
    val mbr = value
    def remove(elem : Int) = if (key.equals(elem)) new EmptyLeaf(branchFactor, ordering) else this
    val size = 1
    override val toString = "Leaf[ " + key + " -> " + value + " ]"
    val traversable = Traversable(key -> value)
    def updated(elem : (Int, SimpleRectangle2D)) = if (value.equals(value)) new Leaf1(key, value, branchFactor, ordering) else this
    lazy val worst = value
  }

  /**
   * A leaf with two elements.
   */
  class Leaf2(key1 : Int, value1 : SimpleRectangle2D, key2 : Int, value2 : SimpleRectangle2D, val branchFactor : Int, val ordering : MBROrdering) extends Leaf {
    def add(elem : (Int, SimpleRectangle2D)) = {
      val a = new Array[(Int, SimpleRectangle2D)](branchFactor)
      a(0) = (key1 -> value1); a(1) = (key2 -> value2); a(2) = elem
      new LeafN(a, 3, branchFactor, ordering)
    }
    def apply(mbr : SimpleRectangle2D) =
      if (value1.intersects(mbr) && value2.intersects(mbr)) Traversable(key1, key2)
      else if (value1.intersects(mbr)) Traversable(key1)
      else if (value2.intersects(mbr)) Traversable(key2)
      else Traversable.empty
    def isBetter(query : SimpleRectangle2D) = ordering.lt(query, worst)
    val mbr = value1.expand(value2)
    def remove(elem : Int) =
      if (elem.equals(key1))
        new Leaf1(key2, value2, branchFactor, ordering)
      else if (elem.equals(key2))
        new Leaf1(key1, value1, branchFactor, ordering)
      else this
    val size = 2
    override val toString = "Leaf[ " + key1 + " -> " + value1 + ", " + key2 + " -> " + value2 + " ]"
    val traversable = Traversable(key1 -> value2, key2 -> value2)
    def updated(elem : (Int, SimpleRectangle2D)) =
      if (elem._2.equals(value1))
        new Leaf2(key1, elem._2, key2, value2, branchFactor, ordering)
      else if (elem._2.equals(value2))
        new Leaf2(key1, value1, key2, elem._2, branchFactor, ordering)
      else this
    lazy val worst = ordering.min(value1, value2) // todo: test
  }

  /**
   * A leaf with N elements.
   *
   * @param elems  The array to store - MUST have a length equal to the branch factor.
   */
  class LeafN(elems : Array[(Int, SimpleRectangle2D)], val size : Int, val branchFactor : Int, val ordering : MBROrdering) extends Leaf {

    /**
     * The MBR of the leaf.
     */
    val mbr = {
      var mbr = elems(0)._2
      for(n <- 1 until size) mbr = mbr.expand(elems(n)._2)
      mbr
    }

    /**
     * The "worst-case" mbr defined by the given ordering.
     */
    val worst = {
      var worst = 0
      for (n <- 0 until size) if (ordering.gt(elems(n)._2, elems(worst)._2)) worst = n
      elems(worst)._2
    }

    /**
     * Add an element to the leaf. Throws an error if the leaf is full.
     */
    def add(elem : (Int, SimpleRectangle2D)) = {
      elems(size) = elem
      new LeafN(elems, size + 1, branchFactor, ordering)
    }

    /**
     * Searches the node for any elements that interacts with the query.
     */
    def apply(query : SimpleRectangle2D) = {
      elems.view.filter(p => query.intersects(p._2)).map(_._1).toTraversable
    }

    /**
     * Examines whether a given mbr is "better" than the current worst case.
     */
    def isBetter(mbr : SimpleRectangle2D) = ordering.lt(mbr, worst)

    /**
     * Return an traversable with all the elements in the leaf.
     */
    def traversable = elems.toTraversable

    /**
     * Remove an element from the leaf.
     */
    def remove(value : Int) : Leaf = {
      for (i <- 0 until size) {
        if (value.equals(elems(i)._1)) {
          if (size > 3) {
            elems(i) = null
            return new LeafN(elems, size - 1, branchFactor, ordering)
          } else if (size == 3) {
            return new Leaf2(elems(0)._1, elems(0)._2, elems(1)._1, elems(1)._2, branchFactor, ordering)
          } else if (size == 2) {
            return new Leaf1(elems(0)._1, elems(0)._2, branchFactor, ordering)
          } else {
            return new EmptyLeaf(branchFactor, ordering)
          }
        }
      }
      // If no element was found, then return unchanged.
      this
    }

    override def toString = {
      var str = "Leaf[ "
      for (i <- 0 until size) {
        str += elems(i)._1 + " -> " + elems(i) + ", "
      }
      // Remove the last "," and return
      str.substring(0, str.length() - 2) + " ]"
    }
    
    def updated(elem : (Int, SimpleRectangle2D)) : LeafN = {
      for (i <- 0 until size) {
        if (elems(i)._2.equals(elem._2)) {
          elems(i) = elem
          return new LeafN(elems, size, branchFactor, ordering)
        }
      }
      // If no element was found, return this.
      this
    }

  }
}