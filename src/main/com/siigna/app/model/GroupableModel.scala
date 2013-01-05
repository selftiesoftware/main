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

package com.siigna.app.model


/**
 * A model that can group and ungroup [[com.siigna.app.model.shape.Shape]]s.
 * 
 * @tparam Key  The type of the keys to group.
 * @tparam Value  The type of the values inside the Model.
 */
trait GroupableModel[Key, Value] extends ModelBuilder[Key, Value] {

  /**
   * Group a single shape to another group.
   * @param key  The key of the shape to group
   * @param group  The key of the group.
   */
  def group(key : Key, group : Key) : Model = throw new UnsupportedOperationException("Not implemented")

  /**
   * Group a collection of shapes.
   */
  def group(keys : Traversable[Key]) : Model = throw new UnsupportedOperationException("Not implemented")

  /**
   * Group a collection of shapes into another group.
   * @param keys  The keys of the shapes to group.
   * @param group  The key of the group.
   */
  def group(keys : Traversable[Key], group : Key) : Model = throw new UnsupportedOperationException("Not implemented")

  /**
   * Ungroup a group by destroying it and putting the shapes back into the model individually.
   * @param group  The key of the group to explode.
   */
  def ungroup(group : Key) : Model = throw new UnsupportedOperationException("Not implemented")

  /**
   * Ungroups a shape given by its key from a group given by its key.
   * @param shape  The key of the shape to ungroup.
   * @param group  The key of the group to ungroup the shape from.
   */
  def ungroup(shape : Key, group : Key) : Model = throw new UnsupportedOperationException("Not implemented")
  
}
