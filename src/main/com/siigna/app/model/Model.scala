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


import action.CreateShape
import com.siigna.app.Siigna
import com.siigna.util.geom.{Vector2D, Rectangle2D}
import shape.{Shape}
import collection.immutable.MapProxy
import com.siigna.app.controller.{Controller}
import com.siigna.util.collection.Attributes
import com.siigna.app.controller.remote.{Get, RemoteAction}

/**
 * An immutable model with two layers: an static and dynamic.
 * <br />
 * The static part is basically a long list of all the
 * [[com.siigna.app.model.shape.Shape]]s and their keys in the Model.
 * <br />
 * The dynamic part allows selecting parts of the global immutable layer. These shapes can be altered
 * without changes in the static layer which allows for significant performance benefits. When the
 * changes have been made (and the shapes are deselected), the shapes are removed from the dynamic
 * layer, and the actions which have been applied on the dynamic layer is applied on the static layer.
 *
 * @param shapes  The shapes and their identifiers (keys) stored in the model.
 *
 * TODO: Examine possibility to implement an actor. Thread-less please.
 */
sealed class Model(val shapes : Map[Int, Shape]) extends ImmutableModel[Int, Shape]
                                                            with MutableModel
                                                            with SpatialModel[Int, Shape]
                                                            with ModelBuilder[Int, Shape] {

  def build(coll : Map[Int, Shape]) = new Model(coll)

}