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

import com.siigna.util.geom.Rectangle2D

/**
 * A node in a PR-tree. This can effectively be either a leaf or a branch.
 * 
 * @author Jens Egholm <jensep@gmail.com>
 */
trait Node {

  type T = Node

  /**
   * Adds a single element to the Node.
   */
  def add(elem : (Int, Rectangle2D)) : T
  
  /**
   * Adds a number of elements to the Node.
   */
  def add(elems : Traversable[(Int, Rectangle2D)]) : T

  /**
   * Queries for elements in the node whose MBR is contained or intersected by a given MBR.
   */
  def apply(query : Rectangle2D) : Traversable[Int]

  /**
   * The branch factor for the node.
   */
  def branchFactor : Int

  /**
   * The MinimumBoundingRectangle of the node.
   */
  def mbr : Rectangle2D

  /**
   * Retrieve the traversable for all elements in the node.
   */
  def traversable : Traversable[(Int, Rectangle2D)]

  /**
   * The number of elements in the Node.
   */
  def size : Int

  /**
   * Returns the node as a string.
   */
  def toString : String

}
