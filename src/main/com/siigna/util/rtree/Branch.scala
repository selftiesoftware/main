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

package com.siigna.util.rtree

import com.siigna.util.geom.SimpleRectangle2D

/**
 * In a PR-tree a branch contains of 4 priority-nodes and 2 subtrees.
 *
 * @author Jens Egholm <jensep@gmail.com>
 */
/*trait Branch extends Node {

  type T = Branch

  /**
   * Adds a number of elements to the branch.
   */
  def add(elems : Traversable[(String, SimpleRectangle2D)]) = {
    // If there's more elements than can be contained in a single
    // block, then add them manually.
    if (elems.size + size <= branchFactor) { 
      elems.foldLeft[Branch](this)((branch, elem) => branch.add(elem))
    } else {
      // Otherwise bulkload away!
      bulkload(elems, (elems.size > (size << 1)))
    }
  }
  
  /**
   * Bulk-loads a number of elements. By removing all the elements and 
   * adding both the old elements and the new elements.
   * 
   * @param elems  The elements to add.
   */
  def bulkload(elems : Traversable[(String, SimpleRectangle2D)]) : Branch = {
    Branch.empty(branchFactor) // Erm..
  }
   
  /**
   * Rebalances the branches beneath this branch.
   */
  def rebalance : Branch

  /**
   * Replace a single element in the Node.
   */
  def update(key : String, value : SimpleRectangle2D) : Node
}

/**
 * This object creates branches based on the number of elements to add.
 */
object Branch {

  /**
   * Creates an empty branch.
   */
  def empty(branchFactor : Int) = new EmptyBranch(branchFactor)
  
  /**
   * An empty branch.
   */
  class EmptyBranch(branchFactor : Int) extends Branch {
    def add(key : String, value : SimpleRectangle2D) = new SingleBlockBranch(Traversable(key -> value), branchFactor)
    def apply(query : SimpleRectangle2D) = Iterator.empty
    def traversable = Traversable.empty
    def mbr = SimpleRectangle2D.empty
    def rebalance = this
    def remove(key : String, value : SimpleRectangle2D) = throw new UnsupportedOperationException("Unable to delete element from empty branch.")
    def remove(elems : Iterator[(String, SimpleRectangle2D)]) = this
    val size = 0
    val toString = "Branch[ ]"
    def update(key : String, value : SimpleRectangle2D) = this
  }

  /**
   * A branch containing <= branchFactor elements.
   * 
   * @param elems  The array containing the elements.
   */
  class SingleBlockBranch(elems : Traversable[(String, SimpleRectangle2D)], branchFactor : Int) extends Branch(branchFactor) {
    def add(key : String, value : SimpleRectangle2D) = {
      if (elems.size < branchFactor) {
        new SingleBlockBranch(elems :+ (key -> value), branchFactor)
      } else {
        val branch = new RBranch(Leaf(branchFactor, OrderMinX), Leaf(branchFactor, OrderMinY), 
                                 Leaf(branchFactor, OrderMaxX), Leaf(branchFactor, OrderMaxY),
                                 Branch.empty(branchFactor), Branch.empty(branchFactor), branchFactor)
        branch.add(elems ++ Traversable(key -> value))
      }
    }

  }

  class RBranch(leafMinX : Leaf, leafMinY : Leaf, leafMaxX : Leaf, leafMaxY : Leaf, branchMinX : Branch, branchMaxX : Branch, branchFactor : Int) {

    /**
     * Add a single element to the branch. If the element cannot fit in the priority leaves,
     * the branch will grow in depth.
     */
    def add(key : String, value : SimpleRectangle2D) = {
      // First try all the priority leaves to see if they're not full
      if (leafMinX.size < branchFactor)      leafMinX.add(key, value)
      else if (leafMinY.size < branchFactor) leafMinY.add(key, value)
      else if (leafMaxX.size < branchFactor) leafMaxX.add(key, value)
      else if (leafMaxY.size < branchFactor) leafMaxY.add(key, value)

      // Then try all the priority leaves for a better fit
      else if (leafMinX.isBetter(value)) {
        val res = leafMinX.swap(key, value)
        add(res._1, res._2)
      } else if (leafMinY.isBetter(key)) {
        val res = leafMinY.swap(key, value)
        add(res._1, res._2)
      } else if (leafMaxX.isBetter(key)) {
        val res = leafMaxX.swap(key, value)
        add(res._1, res._2)
      } else if (leafMaxY.isBetter(key)) {
        val res = leafMaxY.swap(key, value)
        add(res._1, res._2)
      }

      // Then push into the branches

    }
  }

}*/

class Branch(val branchFactor : Int) extends Node {

  private var map = Map[Int, SimpleRectangle2D]()

  def add(key : Int, value : SimpleRectangle2D) = map = map + (key -> value)
  
  def add(elem : (Int, SimpleRectangle2D)) = {
    map = map + elem
    this
  }

  def add(elems : Map[Int, SimpleRectangle2D]) = map = map ++ elems

  def add(elems : Traversable[(Int, SimpleRectangle2D)]) = {
    map = map ++ elems
    this
  }

  def apply(mbr : SimpleRectangle2D) = map.filter(_._2.intersects(mbr)).keys

  def mbr : SimpleRectangle2D =
  	map.values.reduceLeft((p1, p2) => (p1.expand(p2)))

  def remove(elems : Map[Int, SimpleRectangle2D]) { map = map.--(elems.keys) }

  def remove(key : Int, elem : SimpleRectangle2D) { map = map.filterNot(_ == (key, elem)) }

  def size = map.size

  def traversable = map.toTraversable

  override def toString() = "Hello!"

}