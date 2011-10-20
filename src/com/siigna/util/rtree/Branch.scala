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
 *
 * TODO: Make this a tree!
 */
class Branch[T](branchFactor : Int, level : Int) extends Node[T] {

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

}