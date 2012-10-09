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

  val Create = com.siigna.app.model.action.Create
  val Delete = com.siigna.app.model.action.Delete
  val Select = com.siigna.app.model.action.Select
  val Transform = com.siigna.app.model.action.Transform

  // Commands
  val ForwardTo = com.siigna.app.controller.command.ForwardTo
  val Preload = com.siigna.app.controller.command.Preload

  // Controller
  val Controller = com.siigna.app.controller.Controller

  // Model
  val Drawing = com.siigna.app.model.Drawing

  type Selection = com.siigna.app.model.Selection
  val Selection = com.siigna.app.model.Selection

  // Module
  type Module = com.siigna.module.Module
  type StateMap = Map[Symbol, PartialFunction[List[Event], Any]]
  type State = (Symbol, PartialFunction[List[Event], Any])
  val State = com.siigna.util.State

  // Shapes
  type Shape = com.siigna.app.model.shape.Shape
  type ShapeSelector = com.siigna.app.model.shape.ShapeSelector

  type ArcShape = com.siigna.app.model.shape.ArcShape
  lazy val ArcShape = com.siigna.app.model.shape.ArcShape

  type CircleShape = com.siigna.app.model.shape.CircleShape
  lazy val CircleShape = com.siigna.app.model.shape.CircleShape

  //type ImageShape = com.siigna.app.model.shape.ImageShape
  //lazy val ImageShape = com.siigna.app.model.shape.ImageShape

  type LineShape = com.siigna.app.model.shape.LineShape
  lazy val LineShape = com.siigna.app.model.shape.LineShape

  type PartialShape = com.siigna.app.model.shape.PartialShape

  type PolylineShape = com.siigna.app.model.shape.PolylineShape
  val PolylineShape = com.siigna.app.model.shape.PolylineShape

  type TextShape = com.siigna.app.model.shape.TextShape
  val TextShape = com.siigna.app.model.shape.TextShape

  // Siigna object
  val Siigna = com.siigna.app.Siigna

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
  val Attributes = com.siigna.util.collection.Attributes

  type DirectedGraph[V, A] = com.siigna.util.collection.DirectedGraph[V, A]
  val DirectedGraph = com.siigna.util.collection.DirectedGraph

  // Events
  type Event = com.siigna.app.view.event.Event

  val Key = com.siigna.app.view.event.Key
  type KeyDown = com.siigna.app.view.event.KeyDown
  val KeyDown = com.siigna.app.view.event.KeyDown
  type KeyUp = com.siigna.app.view.event.KeyUp
  val KeyUp = com.siigna.app.view.event.KeyUp

  type Message[T] = com.siigna.app.view.event.Message[T]
  lazy val Message = com.siigna.app.view.event.Message

  type ModifierKeys = com.siigna.app.view.event.ModifierKeys
  val ModifierKeys = com.siigna.app.view.event.ModifierKeys

  type ModuleEvent = com.siigna.app.view.event.ModuleEvent

  val MouseButtonLeft = com.siigna.app.view.event.MouseButtonLeft
  val MouseButtonMiddle = com.siigna.app.view.event.MouseButtonMiddle
  val MouseButtonNone = com.siigna.app.view.event.MouseButtonNone
  val MouseButtonRight = com.siigna.app.view.event.MouseButtonRight

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

  val Track = com.siigna.app.view.event.Track

  // Geometry
  type Arc2D = com.siigna.util.geom.Arc2D
  val Arc2D = com.siigna.util.geom.Arc2D

  type Circle2D = com.siigna.util.geom.Circle2D
  val Circle2D = com.siigna.util.geom.Circle2D

  //type Ellipse = com.siigna.util.geom.Ellipse
  //lazy val Ellipse = com.siigna.util.geom.Ellipse

  type Line2D = com.siigna.util.geom.Line2D
  val Line2D = com.siigna.util.geom.Line2D

  type Rectangle2D = com.siigna.util.geom.Rectangle2D
  val Rectangle2D = com.siigna.util.geom.Rectangle2D

  type Segment2D = com.siigna.util.geom.Segment2D
  val Segment2D = com.siigna.util.geom.Segment2D

  type TransformationMatrix = com.siigna.util.geom.TransformationMatrix
  val TransformationMatrix = com.siigna.util.geom.TransformationMatrix
  type Vector2D = com.siigna.util.geom.Vector2D
  val Vector2D = com.siigna.util.geom.Vector2D

  // Log
  val Log = com.siigna.util.logging.Log

}