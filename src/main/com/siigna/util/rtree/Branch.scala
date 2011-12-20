/*
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

/**
 * In a PR-tree a branch contains of 4 priority-nodes and 2 subtrees.
 *
 * @author Jens Egholm <jensep@gmail.com>
 */
trait Branch extends Node {

  /**
   * Adds a number of elements to the branch.
   */
  def add(elems : Iterator[(MBR, String)]) = {
    // If there's more elements than can be contained in a single
    // block, then add them manually.
    if (elems.size + size <= branchFactor) { 
      elems.foldLeft(this)((branch, elem) => branch.add(elem))
    } else {
      // Otherwise bulkload away!
      bulkload(elems, (elems.size > size << 1))
    }
  }
  
  /**
   * Bulk-loads a number of elements. By removing all the elements and 
   * adding both the old elements and the new elements.
   * 
   * @param elems  The elements to add.
   */
  def bulkload(elems : Iterator[(MBR, String)]) : Branch {
    Branch.empty // Erm..
  }
   
  /**
   * Rebalances the branches beneath this branch.
   */
  def rebalance : Branch

  /**
   * Replace a single element in the Node.
   */
  def update(key : MBR, value : String) : Node
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
    def add(key : MBR, value : String) = new SingleBlockBranch(Seq(key -> value), branchFactor)
    def apply(query : MBR) = Iterator.empty
    def iterable = Iterable.empty
    def mbr = MBR.empty
    def rebalance = this
    def remove(key : MBR, value : String) = this
    def remove(elems : Iterator[(MBR, String)]) = this
    val size = 0
    val toString = "Branch[ ]"
    def update(key : MBR, value : String) = this
  }

  /**
   * A branch containing <= branchFactor elements.
   * 
   * @param elems  The array containing the elements.
   */
  class SingleBlockBranch(elems : Seq[(MBR, String)], branchFactor : Int) extends Branch(branchFactor) {
    def add(key : MBR, value : String) = 
      if (elems.size < 7) {
        new SingleBlockBranch(elems :+ (key -> value), branchFactor)
      } else {
        val branch = new RBranch(Leaf(branchFactor, OrderMinX), Leaf(branchFactor, OrderMinY), 
                                 Leaf(branchFactor, OrderMaxX), Leaf(branchFactor, OrderMaxY),
                                 
      }
  }

  class RBranch(leafMinX : Leaf, leafMinY : Leaf, leafMaxX : Leaf, leafMaxY : Leaf, branchMinX : Branch, branchMaxX : Branch, branchFactor : Int) {

    /**
     * Add a single element to the branch. If the element cannot fit in the priority leaves,
     * the branch will grow in depth.
     */
    def add(key : MBR, value : String) {
      // First try all the priority leaves to see if they're not full
      if (leafXMin.size < branchFactor)      leafXMin.add(key, value)
      else if (leafYMin.size < branchFactor) leafYMin.add(key, value)
      else if (leafXMax.size < branchFactor) leafXMax.add(key, value)
      else if (leafYMax.size < branchFactor) leafYMax.add(key, value)

      // Then try all the priority leaves for a better fit
      else if (leafXMin.isBetter(key)) {
        val res = leafXMin.swap(key, value)
        add(res._1, res._2)
      } else if (leafYMin.isBetter(key)) {
        val res = leafYMin.swap(key, value)
        add(res._1, res._2)
      } else if (leafXMax.isBetter(key)) {
        val res = leafXMax.swap(key, value)
        add(res._1, res._2)
      } else if (leafYMax.isBetter(key)) {
        val res = leafYMax.swap(key, value)
        add(res._1, res._2)
      }

      // Then push into the branches

    }
  }

}


/**class Branch[T](branchFactor : Int, level : Int) extends Node[T] {

  private var map = Map[MBR, T]()
  
  def add(key : MBR, elem : T) = map = map + (key -> elem)
  
  def add(elems : Map[MBR, T]) = map = map ++ elems
  
  def apply(mbr : MBR) = map.filter(_._1 == mbr).values
  
  def mbr : MBR = 
  	if (map.isEmpty) MBR(0, 0, 0, 0) 
  	else map.keys.reduceLeft((p1, p2) => (p1.expand(p2)))
  
  def remove(elems : Map[MBR, T]) { map = map.--(elems.keys) }
  	
  def remove(key : MBR, elem : T) { map = map.filterNot(_ == (key, elem)) }
  
  def size = map.size
  
  override def toString() = "Hello!"

}*/
*/
