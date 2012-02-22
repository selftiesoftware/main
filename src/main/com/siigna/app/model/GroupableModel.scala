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

import com.siigna.util.logging.Log
import shape.{ImmutableShape, GroupShape}
import collection.parallel.immutable.ParVector

/**
 * A CollectibleModel is able to group shapes into <code>Groups</code>.
 */
trait GroupableModel extends GenericModel {

  /**
   * Groups a number of shapes.
   */
  def group(shapes : Traversable[Int]) : GroupableModel = {
    val shapes = shapes.map(immutableModel(_))
    immutableModel =
    this
  }

  /**
   * Disperses a group.
   */
  def ungroup(group : GroupShape) : GroupableModel = try {
    groups -= group
    this
  } catch {
    case e => Log.error("Model: Unknown error when dispersing a group: "+e)
    this
  }

  /**
   * Ungroups a number of shapes from the given collection.
   */
  def ungroup(group : GroupShape, shapes : Traversable[String]): GroupableModel = try {
    val index = groups.indexWhere(_ == group)
    if (index >= 0) {
      val newGroup = group -- shapes
      if (!newGroup.isEmpty) {
        groups.update(index, newGroup)
      } else {
        groups.remove(index)
      }
    } else {
      Log.warning("Model: The group to be dispersed could not be found in the model. Dispersion cancelled.")
    }
    this
  } catch {
    case e => Log.error("Model: Unknown error when dispersing a shape from a group: "+e)
    this
  }

  /**
   * Replaces a given group with a new group, if the first group exists.
   */
  def update(oldGroup : GroupShape, newGroup : GroupShape) : GroupableModel = try {
    val index = groups.indexWhere(_ == oldGroup)
    if (index >= 0) {
      groups.update(index, newGroup)
    } else {
      Log.warning("Model: The group to be updates could not be found in the model. Dispersion cancelled.")
    }
    this
  } catch {
    case e => Log.error("Model: Unknown error when dispersing a shape from a group: "+e)
    this
  }

}