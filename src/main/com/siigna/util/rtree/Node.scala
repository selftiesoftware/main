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
 * A node in a PR-tree. This can effectively be either a leaf or a branch.
 * 
 * @author Jens Egholm <jensep@gmail.com>
 */
trait Node {

  /**
   * Adds a single element to the Node.
   */
  def add(elem : (String, Shape)) : Node
  
  /**
   * Adds a number of elements to the Node.
   */
  def add(elems : Traversable[(String, Shape)]) : Node

  /**
   * Queries for elements in the node whose MBR is contained or intersected by a given MBR.
   */
  def apply(query : Rectangle2D) : Traversable[Shape]

  /**
   * The branch factor for the node.
   */
  def branchFactor : Int
  
  /**
   * Retrieve the traversable for all elements in the node.
   */
  def traversable : Traversable[(String, Shape)]

  /**
   * The MinimumBoundingRectangle of the node.
   */
  def mbr : Rectangle2D

  /**
   * Removes a single element from the Node.
   */
  def remove(elem : (String, Shape)) : Node
  
  /**
   * Removes a number of elements from the Node.
   */
  def remove(elems : Traversable[(String, Shape)]) : Node

  /**
   * The number of elements in the Node.
   */
  def size : Int

  /**
   * Returns the node as a string.
   */
  def toString : String
  
  /**
   * Updates the shape associated with the given string.
   */
  def updated(elem : (String, Shape)) : Node

}
