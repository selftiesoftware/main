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

import com.siigna.app.model.shape.Shape
import com.siigna.util.geom.Rectangle2D

/**
 * An immutable leaf in a prioritized R-tree containing up to <code>branchFactor</code> elements.
 *
 * @author Jens Egholm <jensep@gmail.com>
 */
trait Leaf extends Node {

  /**
   * Adds a single element to the Node.
   */
  def add(elem : (String, Shape)) : Leaf

  /**
   * Adds a number of elements to the leaf. If the number of elements plus the size of the leaf
   * exceeds the branchFactor, an error is thrown.
   * TODO: Optimize
   */
  def add(elems : Traversable[(String, Shape)]) : Leaf = {
    if (elems.size + size > branchFactor || elems.isEmpty) {
      throw new IlegalArgumentException("Unable to add to the leaf with " + elems.size + " elements")
    } else if (elems.size + size > 2) { // If we require a LeafN node (more than 2 elements)
      val a = (traversable ++ elems).toArray
      new Leaf.LeafN(a, elems.size + size, branchFactor, ordering)
    } else if (elems.size == 2) {       // If there's 2 elements to add
      val e = elems.toSeq
      new Leaf.Leaf2(e(0)._1, e(0)._2, e(1)._1, e(0)._2¸ branchFactor, ordering)
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
  def isBetter(query : Rectangle2D) : Boolean
  
  /**
   * The ordering of the leaf.
   */
  def ordering : MBROrdering

  /**
   * Removes a single element from the leaf.
   */
  def remove(elem : (String, Shape)) : Leaf
  
  /**
   * Removes a number of elements from the leaf.
   * TODO: Optimize
   */
  def remove(elems : Map[(String, Shape)]) : Leaf =
    elems.foldLeft(this)((leaf, elem) => leaf.remove(elem))
  
  /**
   * Returns the leaf where the given element is updated.
   */
  def updated(elem : (String, Shape)) : Leaf
  
  /**
   * The worst case MBR from the given ordering.
   */
  def worst : Rectangle2D

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
  class EmptyLeaf(branchFactor : Int, ordering : MBROrdering) extends Leaf {
    def add(elem : (String, Shape)) = new Leaf1(elem._1, elem._2, branchFactor, ordering)
    def apply(query : Rectangle2D) = Traversable.empty
    def isBetter(query : Rectangle2D) = true
    val mbr = Rectangle2D.empty
    def remove(elem : String) = this
    val size = 0
    override val toString = "Empty Leaf"
    val traversable = Iterable.empty[(String, Shape)]
    def updated(elem : (String, Shape)) = this
    val worst = Rectangle2D.empty
  }

  /**
   * A leaf with one element.
   */
  class Leaf1(key1 : String, value1 : Shape, branchFactor : Int, ordering : MBROrdering) extends Leaf {
    def add(elem : (String, Shape)) = new Leaf2(key1, value1, elem._1, elem._2, branchFactor, ordering)
    def apply(query : Rectangle2D) = if (mbr.intersects(query)) Iterator(value1) else Iterator.empty
    def isBetter(query : Rectangle2D) = ordering.lt(worst, value1)
    val iterable = Iterable(key1 -> value1)
    val mbr = value1.boundary
    def remove(elem : String) = if (value1.equals(elem)) new EmptyLeaf(branchFactor, ordering) else this
    val size = 1
    override val toString = "Leaf[ " + key + " -> " + value + " ]"
    def updated(elem : (MBR, String)) = if (value1.equals(value)) new Leaf1(key, value, branchFactor, ordering) else this
    val worst = key1
  }

  /**
   * A leaf with two elements.
   */
  class Leaf2(key1 : String, value1 : Shape, key2 : MBR, value2 : String, branchFactor : Int, ordering : MBROrdering) extends Leaf {
    def add(elem : (MBR, String)) = {
      val a = new Array[(MBR, String)](branchFactor)
      a(0) = (key1 -> value1); a(1) = (key2 -> value2); a(2) = elem
      new LeafN(a, 3, branchFactor, ordering)
    }
    def add(elems 
    def apply(mbr : MBR) =
      if (key1.overlap(mbr) && key2.overlap(mbr)) Iterator(value1, value2)
      else if (key1.overlap(mbr)) Iterator(value1)
      else if (key2.overlap(mbr)) Iterator(value2)
      else Iterator.empty
    def isBetter(query : MBR) = ordering.lt(query, worst)
    val iterable = Iterable(key1 -> value2, key2 -> value2)
    val mbr = key1.expand(key2)
    def remove(elem : String) =
      if (elem.equals(value1))
        new Leaf1(key2, value2, branchFactor, ordering)
      else if (elem.equals(value2))
        new Leaf1(key1, value1, branchFactor, ordering)
      else this
    val size = 2
    override val toString = "Leaf[ " + key1 + " -> " + value1 + ", " + key2 + " -> " + value2 + " ]"
    def updated(elem : (MBR, String)) = 
      if (elem._2.equals(value1))
        new Leaf2(elem._1, value, key2, value2, branchFactor, ordering)
      else if (elem._2.equals(value2))
        new Leaf2(key1, value1, elem._1, value, branchFactor, ordering)
      else this
  }

  /**
   * A leaf with N elements.
   *
   * @param elems  The array to store - MUST have a length equal to the branch factor.
   */
  class LeafN(elems : Array[(String, Shape)], val size : Int, branchFactor : Int, ordering : MBROrdering) extends Leaf {

    /**
     * The MBR of the leaf.
     */
    val mbr = {
      var mbr = MBR.empty
      for(n <- 0 until size) mbr = mbr.expand(elems(n)._1)
      mbr
    }

    /**
     * The index of the "worst-case" node defined by the given ordering.
     */
    val worst : Int = {
      var worst = 0
      for (n <- 0 until size) if (ordering.gt(elems(n)._1, elems(worst)._1)) worst = n
      worst
    }

    /**
     * Add an element to the leaf. Throws an error if the leaf is full.
     */
    def add(elem : (MBR, String)) = {
      elems(size) = elem
      new LeafN[T](elems, size + 1, branchFactor, ordering)
    }

    /**
     * Searches the node for any elements that interacts with the query.
     */
    def apply(query : MBR) = {
      elems.view.filter(p => query.overlap(p._1)).map(_._2).toIterator
    }

    /**
     * Examines whether a given mbr is "better" than the current worst case.
     */
    def isBetter(mbr : MBR) = ordering.lt(mbr, elems(worst)._1)

    /**
     * Return an iterable with all the elements in the leaf.
     */
    def iterable = elems.toIterable

    /**
     * Remove an element from the leaf.
     */
    def remove(value : String) : Leaf = {
      for (i <- 0 until size) {
        if (value.equals(elems(i)._2)) {
          if (size > 3) {
            elems(i) == null
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
    
    def updated(elem : (MBR, String)) = {
      for (i <- until size) {
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