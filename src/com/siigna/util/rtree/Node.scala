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
 * A node in a PR-tree containing the MBR. This can effectively be either a leaf or a branch.
 * 
 * @author Jens Egholm <jensep@gmail.com>
 */
trait Node[T] {

  /**
   * Adds a single element to the Node.
   */
  def add(key : MBR, elem : T) : Unit
  
  /**
   * Adds several elements to the Node.
   */
  def add(elems : Map[MBR, T]) : Unit
  
  /**
   * Queries for elements in the node whose MBR is contained or intersected by a given MBR.
   */
  def apply(mbr : MBR) : Iterable[T]

  /**
   * The MinimumBoundingRectangle of the node.
   */
  def mbr : MBR
  
  /**
   * Removes a single element from the Node.
   * 
   * @returns  The element that could not be found and removed, if any.
   */
  def remove(key : MBR, elem : T) : Unit
  
  /**
   * Removes a number of elements from the Node.
   * 
   * @returns  A collection of the elements that could not be found and removed - can be empty!.
   */
  def remove(elems : Map[MBR, T]) : Unit

  /**
   * The size of the elements in the Node.
   */
  def size : Int

  /**
   * Returns the node as a string.
   */
  def toString : String

}