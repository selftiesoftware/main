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

package com.siigna.app.model

import collection.parallel.immutable.ParVector

import com.siigna.util.geom.Rectangle2D
import shape.ImmutableShape

/**
 * An immutable model containing immutable shapes.
 * 
 * @elems  The elements that the Model consists of.
 */
class ImmutableModel(elems : ParVector[ImmutableShape]) extends Model[ImmutableShape] {

  def add(elem : ImmutableShape) = new ImmutableModel(elems :+ elem)

  def add(elems : ImmutableShape*) = new ImmutableModel(this.elems ++ elems)
  
  def add(elems : Traversable[ImmutableShape]) = new ImmutableModel(this.elems ++ elems)

  def iterator = elems.toIterator

  def remove(elem : ImmutableShape) = new ImmutableModel(elems.filterNot(_ == elem))

  /**
   * TODO: Examine if this can be optimized.
   */
  def remove(elems : ImmutableShape*) = {
    var xe = this.elems
    for (e <- elems) {
      xe = xe.filterNot(_ == e)
    }
    new ImmutableModel(xe)
  }

  def remove(elems : Traversable[ImmutableShape]) = {
    var xe = this.elems
    for (e <- elems) {
      xe = xe.filterNot(_ == e)
    }
    new ImmutableModel(xe)
  }

  def updated(elem1 : (String, ImmutableShape), elem2 : (String, ImmutableShape)) = {
    val index = elems.indexOf(elem1)
    if (index >= 0) {
      new ImmutableModel(elems.updated(index, elem2))
    } else this
  }

  /**
   * TODO: Optimize this by getting the ids at the first run
   */
  def updated(elems : Map[String, ImmutableShape]) = {
    var xe = this.elems
    for (e <- elems) {
      val index = xe.indexOf(e)
      if (index >= 0) {
        xe.updated(index, e)
      }
    }
    new ImmutableModel(xe)
  }

}