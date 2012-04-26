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

package com

import siigna.app.model.Selection

package object siigna extends com.siigna.util.Implicits {

  // Actions
  type Action = com.siigna.app.model.action.Action

  lazy val Create = com.siigna.app.model.action.Create

  //lazy val Delete = com.siigna.app.model.action.Delete

  //lazy val Deselect = com.siigna.app.model.action.Deselect
  lazy val Select = com.siigna.app.model.action.Select

  lazy val Transform = com.siigna.app.model.action.Transform

  // Commands
  lazy val ForwardTo = com.siigna.app.controller.command.ForwardTo
  lazy val Goto = com.siigna.app.controller.command.Goto
  lazy val Preload = com.siigna.app.controller.command.Preload

  // RemoteCommands
  lazy val GetNewShapeId = com.siigna.app.controller.remote.GetNewShapeId

  // Controller
  lazy val Controller = com.siigna.app.controller.Controller
  val AppletParameters = com.siigna.app.controller.AppletParameters

  // Model
  val Model = com.siigna.app.model.Model

  type Selection = com.siigna.app.model.Selection
  val Selection = com.siigna.app.model.Selection

  // Module
  type Module = com.siigna.module.Module

  // Shapes
  type ImmutableShape = com.siigna.app.model.shape.Shape
  type Shape = com.siigna.app.model.shape.ShapeLike

  type ArcShape = com.siigna.app.model.shape.ArcShape
  lazy val ArcShape = com.siigna.app.model.shape.ArcShape

  type CircleShape = com.siigna.app.model.shape.CircleShape
  lazy val CircleShape = com.siigna.app.model.shape.CircleShape

  //type ImageShape = com.siigna.app.model.shape.ImageShape
  //lazy val ImageShape = com.siigna.app.model.shape.ImageShape

  type LineShape = com.siigna.app.model.shape.LineShape
  lazy val LineShape = com.siigna.app.model.shape.LineShape

  type PolylineShape = com.siigna.app.model.shape.PolylineShape
  lazy val PolylineShape = com.siigna.app.model.shape.PolylineShape

  type TextShape = com.siigna.app.model.shape.TextShape
  lazy val TextShape = com.siigna.app.model.shape.TextShape

  // Siigna object
  lazy val Siigna = com.siigna.app.Siigna

  // View
  lazy val Cursors = com.siigna.app.view.Interface.Cursors 
  type Display = com.siigna.app.view.Display
  type Graphics = com.siigna.app.view.Graphics
  type ModuleInterface = com.siigna.app.view.ModuleInterface
  type Popup = com.siigna.app.view.Popup
  val View = com.siigna.app.view.View

  // ------------------ UTIL -------------------- //
  // Collection
  type Attributes = com.siigna.util.collection.Attributes
  lazy val Attributes = com.siigna.util.collection.Attributes

  type DirectedGraph[V, A] = com.siigna.util.collection.DirectedGraph[V, A]
  lazy val DirectedGraph = com.siigna.util.collection.DirectedGraph

  lazy val Preferences, Pref = com.siigna.util.collection.Preferences

  // Events
  type Event = com.siigna.app.view.event.Event

  type EventHandler = com.siigna.app.view.event.EventHandler
  lazy val EventHandler = com.siigna.app.view.event.EventHandler

  lazy val Key = com.siigna.app.view.event.Key
  type KeyDown = com.siigna.app.view.event.KeyDown
  val KeyDown = com.siigna.app.view.event.KeyDown
  type KeyUp = com.siigna.app.view.event.KeyUp
  val KeyUp = com.siigna.app.view.event.KeyUp

  type Message[T] = com.siigna.app.view.event.Message[T]
  lazy val Message = com.siigna.app.view.event.Message

  type ModifierKeys = com.siigna.app.view.event.ModifierKeys
  val ModifierKeys = com.siigna.app.view.event.ModifierKeys

  type ModuleEvent = com.siigna.app.view.event.ModuleEvent

  lazy val MouseButtonLeft = com.siigna.app.view.event.MouseButtonLeft
  lazy val MouseButtonMiddle = com.siigna.app.view.event.MouseButtonMiddle
  lazy val MouseButtonNone = com.siigna.app.view.event.MouseButtonNone
  lazy val MouseButtonRight = com.siigna.app.view.event.MouseButtonRight

  type MouseDown = com.siigna.app.view.event.MouseDown
  val MouseDown = com.siigna.app.view.event.MouseDown
  type MouseDrag = com.siigna.app.view.event.MouseDrag
  val MouseDrag = com.siigna.app.view.event.MouseDrag
  type MouseEnter = com.siigna.app.view.event.MouseEnter
  val MouseEnter = com.siigna.app.view.event.MouseEnter
  type MouseExit = com.siigna.app.view.event.MouseExit
  val MouseExit = com.siigna.app.view.event.MouseExit
  type MouseMove = com.siigna.app.view.event.MouseMove
  val MouseMove = com.siigna.app.view.event.MouseMove
  type MouseUp = com.siigna.app.view.event.MouseUp
  val MouseUp = com.siigna.app.view.event.MouseUp
  type MouseWheel = com.siigna.app.view.event.MouseWheel
  val MouseWheel = com.siigna.app.view.event.MouseWheel

  // Geometry
  type Arc = com.siigna.util.geom.Arc
  type Arc2D = com.siigna.util.geom.Arc2D
  lazy val Arc2D = com.siigna.util.geom.Arc2D

  type Circle = com.siigna.util.geom.Circle
  type Circle2D = com.siigna.util.geom.Circle2D
  lazy val Circle2D = com.siigna.util.geom.Circle2D

  //type Ellipse = com.siigna.util.geom.Ellipse
  //lazy val Ellipse = com.siigna.util.geom.Ellipse

  type Line = com.siigna.util.geom.Line
  type Line2D = com.siigna.util.geom.Line2D
  lazy val Line2D = com.siigna.util.geom.Line2D

  type Rectangle = com.siigna.util.geom.Rectangle
  type Rectangle2D = com.siigna.util.geom.Rectangle2D
  lazy val Rectangle2D = com.siigna.util.geom.Rectangle2D

  type Segment = com.siigna.util.geom.Segment
  type Segment2D = com.siigna.util.geom.Segment2D
  lazy val Segment2D = com.siigna.util.geom.Segment2D

  type TransformationMatrix = com.siigna.util.geom.TransformationMatrix
  lazy val TransformationMatrix = com.siigna.util.geom.TransformationMatrix

  type Vector = com.siigna.util.geom.Vector
  type Vector2D = com.siigna.util.geom.Vector2D
  lazy val Vector2D = com.siigna.util.geom.Vector2D

  // Log
  lazy val Log = com.siigna.util.logging.Log

}