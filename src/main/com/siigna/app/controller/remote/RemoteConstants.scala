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
   * Used to recognize requests for setting or getting [[com.siigna.app.model.action.Action]] ids.
   */
  val ActionId = Value

  /**
   * Used to recognize requests for setting or getting [[com.siigna.app.controller.Session]]s.
   */
  val Client = Value
}