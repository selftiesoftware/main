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

package object siigna extends com.siigna.util.Implicits {

  // ------------------ APP -------------------- //
  // Actions
  type Action = com.siigna.app.model.action.Action

  val Create = com.siigna.app.model.action.Create
  val Delete = com.siigna.app.model.action.Delete
  val Select = com.siigna.app.model.action.Select
  val Transform = com.siigna.app.model.action.Transform

  // Controller
  val Controller = com.siigna.app.controller.Controller

  // Model
  val Drawing = com.siigna.app.model.Drawing

  type Selection = com.siigna.app.model.Selection
  val Selection = com.siigna.app.model.Selection

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

  // ------------------ MODULE ------------------ //
  type Module         = com.siigna.module.Module
  val  Module         = com.siigna.module.Module
  type ModuleInstance = com.siigna.module.ModuleInstance
  val  ModuleInstance = com.siigna.module.ModuleInstance
  val  ModuleLoader   = com.siigna.module.ModuleLoader
  type ModulePackage  = com.siigna.module.ModulePackage
  val  ModulePackage  = com.siigna.module.ModulePackage
  type State          = com.siigna.module.State
  type StateMap       = com.siigna.module.StateMap

  // ------------------ UTIL -------------------- //
  // Collection
  type Attributes = com.siigna.util.collection.Attributes
  val Attributes = com.siigna.util.collection.Attributes

  type DirectedGraph[V, A] = com.siigna.util.collection.DirectedGraph[V, A]
  val DirectedGraph = com.siigna.util.collection.DirectedGraph

  // Events
  type Event = com.siigna.util.event.Event

  val Key     = com.siigna.util.event.Key
  type KeyDown = com.siigna.util.event.KeyDown
  val KeyDown  = com.siigna.util.event.KeyDown
  type KeyUp   = com.siigna.util.event.KeyUp
  val KeyUp    = com.siigna.util.event.KeyUp

  type Message[T]  = com.siigna.util.event.Message[T]
  lazy val Message = com.siigna.util.event.Message

  type ModifierKeys = com.siigna.util.event.ModifierKeys
  val ModifierKeys  = com.siigna.util.event.ModifierKeys
  val Alt           = ModifierKeys.Alt
  val Control, Ctrl = ModifierKeys.Control
  val Shift         = ModifierKeys.Shift

  type ModuleEvent = com.siigna.util.event.ModuleEvent
  type End[T]      = com.siigna.util.event.End[T]
  val End          = com.siigna.util.event.End
  type Start[T]    = com.siigna.util.event.Start[T]
  val Start        = com.siigna.util.event.Start

  val MouseButtonLeft   = com.siigna.util.event.MouseButtonLeft
  val MouseButtonMiddle = com.siigna.util.event.MouseButtonMiddle
  val MouseButtonNone   = com.siigna.util.event.MouseButtonNone
  val MouseButtonRight  = com.siigna.util.event.MouseButtonRight

  type MouseDown = com.siigna.util.event.MouseDown
  val MouseDown = com.siigna.util.event.MouseDown
  type MouseDrag = com.siigna.util.event.MouseDrag
  val MouseDrag = com.siigna.util.event.MouseDrag
  type MouseEnter = com.siigna.util.event.MouseEnter
  val MouseEnter = com.siigna.util.event.MouseEnter
  type MouseDouble = com.siigna.util.event.MouseDouble
  val MouseDouble = com.siigna.util.event.MouseDouble
  type MouseExit = com.siigna.util.event.MouseExit
  val MouseExit = com.siigna.util.event.MouseExit
  type MouseMove = com.siigna.util.event.MouseMove
  val MouseMove = com.siigna.util.event.MouseMove
  type MouseUp = com.siigna.util.event.MouseUp
  val MouseUp = com.siigna.util.event.MouseUp
  type MouseWheel = com.siigna.util.event.MouseWheel
  val MouseWheel = com.siigna.util.event.MouseWheel

  val Snap = com.siigna.util.event.Snap
  val Track = com.siigna.util.event.Track

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