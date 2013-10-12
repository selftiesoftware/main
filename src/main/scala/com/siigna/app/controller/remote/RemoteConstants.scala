/*
 * Copyright (c) 2008-2013, Selftie Software. Siigna is released under the
 * creative common license by-nc-sa. You are free
 *   to Share — to copy, distribute and transmit the work,
 *   to Remix — to adapt the work
 *
 * Under the following conditions:
 *   Attribution —   You must attribute the work to http://siigna.com in
 *                    the manner specified by the author or licensor (but
 *                    not in any way that suggests that they endorse you
 *                    or your use of the work).
 *   Noncommercial — You may not use this work for commercial purposes.
 *   Share Alike   — If you alter, transform, or build upon this work, you
 *                    may distribute the resulting work only under the
 *                    same or similar license to this one.
 *
 * Read more at http://siigna.com and https://github.com/siigna/main
 */

package com.siigna.app.controller.remote

/**
 * The remote package contains remote information relevant for the client and server.
 */
object RemoteConstants extends Enumeration {

  type RemoteConstant = Value

  /**
   * Used to recognize requests for getting or setting ShapeIdentifiers (ids).
   */
  val ShapeId = Value

  /**
   * Used to recognize requests for getting or setting [[com.siigna.app.model.Drawing]]s.
   */
  val Drawing = Value

  /**
   * Used to recognize requests for setting or getting drawing names.
   */
  val DrawingName = Value

  /**
   * Used to recognize requests for setting or getting drawing ids.
   */
  val DrawingId = Value

  /**
   * Used to recognize requests for setting or getting [[com.siigna.app.model.action.Action]]s.
   */
  val Action = Value

  /**
   * Used to recognize requests for setting or getting one or more [[com.siigna.app.model.action.Action]]s.
   */
  val Actions = Value

  /**
   * Used to recognize requests for setting or getting [[com.siigna.app.model.action.Action]] ids.
   */
  val ActionId = Value

  /**
   * Used to recognize requests for setting or getting one or more [[com.siigna.app.model.action.Action]] ids.
   */
  val ActionIds = Value

  /**
   * Used to recognize requests for setting or getting [[com.siigna.app.controller.remote.Session]]s.
   */
  val Client = Value

  /**
   * Used to change read write of a user or the public. Value should be a tuple3 representing recipient of the access level
   * and access modification (recipient:Long,drawing:Long,(read:Boolean,write:Boolean)).
   */
  val Access = Value

  /**
   * Change openness (public visibility of a drawing)
   * Value must be a tuple2 representing (drawing,[[com.siigna.app.model.Drawing.Openness]])
   */
  val Openness = Value

  /**
   * Force clean a drawing
   */
  val Cleaning = Value
}