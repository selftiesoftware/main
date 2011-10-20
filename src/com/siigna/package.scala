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

package com

package object siigna extends com.siigna.util.Implicits {

  // Actions
  type Action = com.siigna.app.model.action.Action

  lazy val Create = com.siigna.app.model.action.Create
  type CreateShape = com.siigna.app.model.action.CreateShape
  lazy val CreateShape = com.siigna.app.model.action.CreateShape
  type CreateShapes = com.siigna.app.model.action.CreateShapes
  lazy val CreateShapes = com.siigna.app.model.action.CreateShapes

  lazy val Delete = com.siigna.app.model.action.Delete
  type DeleteShape = com.siigna.app.model.action.DeleteShape
  type DeleteShapes = com.siigna.app.model.action.DeleteShapes

  lazy val Deselect = com.siigna.app.model.action.Deselect
  lazy val Select = com.siigna.app.model.action.Select

  lazy val Transform = com.siigna.app.model.action.Transform
  type TransformShape = com.siigna.app.model.action.TransformShape
  type TransformShapes = com.siigna.app.model.action.TransformShapes

  // Commands
  /**
   * Creates a ForwardTo class and puts it in the controller event-queue.
   *
   * @param module  The module expressed by a symbol. Remember to <code>Preload</code> it!
   * @param continue  A Boolean value to express whether the forwarded module should receive the last event again. Defaults to true.
   */
  def ForwardTo(module : Symbol, continue : Boolean = true) {
    Control(new com.siigna.app.controller.command.ForwardTo(module, continue))
  }

  /**
   * Shortcut to the Goto command.
   *
   * @param state The state to forward to.
   * @param continue  A flag that signals whether the module should continue it's execution in the new state or wait
   * for a new event. Defaults to true.
   */
  def Goto(state : Symbol, continue : Boolean = true) {
    Control(new com.siigna.app.controller.command.Goto(state, continue))
  }

  /**
   * Asks the controller to preload a given module.
   *
   * @param name  The symbolic representation of the module used inside Siigna to recognize the module.
   * @param classPath  The name of the path to load the class from, including the name of the class itself. Defaults to "com.siigna.module.endogenous".
   * @param filePath  The name and the place of the given file to load. Defaults to "".
   */
  def Preload(name : Symbol, classPath : String = "com.siigna.module.endogenous", fileName : String = "") {
    Control(new com.siigna.app.controller.command.Preload(name, classPath, fileName))
  }

  // Controller
  lazy val Control = com.siigna.app.controller.Control

  // Model
  type Model = com.siigna.app.model.Model
  lazy val Model = com.siigna.app.model.Model

  // Module
  type Module = com.siigna.module.Module

  // Shapes
  type ImmutableShape = com.siigna.app.model.shape.ImmutableShape
  type Shape = com.siigna.app.model.shape.Shape

  type ArcShape = com.siigna.app.model.shape.ArcShape
  lazy val ArcShape = com.siigna.app.model.shape.ArcShape

  type CircleShape = com.siigna.app.model.shape.CircleShape
  lazy val CircleShape = com.siigna.app.model.shape.CircleShape

  type DynamicShape = com.siigna.app.model.shape.DynamicShape
  val DynamicShape = com.siigna.app.model.shape.DynamicShape

  type ImageShape = com.siigna.app.model.shape.ImageShape
  lazy val ImageShape = com.siigna.app.model.shape.ImageShape

  type LineShape = com.siigna.app.model.shape.LineShape
  lazy val LineShape = com.siigna.app.model.shape.LineShape

  type PointShape = com.siigna.app.model.shape.PointShape
  lazy val PointShape = com.siigna.app.model.shape.PointShape

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
  type Interface = com.siigna.app.view.Interface
  type Popup = com.siigna.app.view.Popup

  // ------------------ UTIL -------------------- //
  // Collection
  type Attributes = com.siigna.util.collection.Attributes
  lazy val Attributes = com.siigna.util.collection.Attributes

  type DirectedGraph[V, A] = com.siigna.util.collection.DirectedGraph[V, A]
  lazy val DirectedGraph = com.siigna.util.collection.DirectedGraph

  lazy val Preferences = com.siigna.util.collection.Preferences

  // Events
  type Event = com.siigna.util.event.Event

  type EventHandler = com.siigna.util.event.EventHandler
  lazy val EventHandler = com.siigna.util.event.EventHandler

  lazy val Key = com.siigna.util.event.Key
  type KeyDown = com.siigna.util.event.KeyDown
  val KeyDown = com.siigna.util.event.KeyDown
  type KeyUp = com.siigna.util.event.KeyUp
  val KeyUp = com.siigna.util.event.KeyUp

  type Message[T] = com.siigna.util.event.Message[T]
  lazy val Message = com.siigna.util.event.Message

  type ModifierKeys = com.siigna.util.event.ModifierKeys
  val ModifierKeys = com.siigna.util.event.ModifierKeys

  type ModuleEvent = com.siigna.util.event.ModuleEvent

  lazy val MouseButtonLeft = com.siigna.util.event.MouseButtonLeft
  lazy val MouseButtonMiddle = com.siigna.util.event.MouseButtonMiddle
  lazy val MouseButtonRight = com.siigna.util.event.MouseButtonRight

  type MouseDown = com.siigna.util.event.MouseDown
  val MouseDown = com.siigna.util.event.MouseDown
  type MouseDrag = com.siigna.util.event.MouseDrag
  val MouseDrag = com.siigna.util.event.MouseDrag
  type MouseEnter = com.siigna.util.event.MouseEnter
  val MouseEnter = com.siigna.util.event.MouseEnter
  type MouseExit = com.siigna.util.event.MouseExit
  val MouseExit = com.siigna.util.event.MouseExit
  type MouseMove = com.siigna.util.event.MouseMove
  val MouseMove = com.siigna.util.event.MouseMove
  type MouseUp = com.siigna.util.event.MouseUp
  val MouseUp = com.siigna.util.event.MouseUp
  type MouseWheel = com.siigna.util.event.MouseWheel
  val MouseWheel = com.siigna.util.event.MouseWheel

  // Geometry
  type Arc = com.siigna.util.geom.Arc
  lazy val Arc = com.siigna.util.geom.Arc

  type Circle = com.siigna.util.geom.Circle
  lazy val Circle = com.siigna.util.geom.Circle

  type Ellipse = com.siigna.util.geom.Ellipse
  lazy val Ellipse = com.siigna.util.geom.Ellipse

  type Line = com.siigna.util.geom.Line
  lazy val Line = com.siigna.util.geom.Line

  type Rectangle = com.siigna.util.geom.Rectangle
  lazy val Rectangle = com.siigna.util.geom.Rectangle

  type Segment = com.siigna.util.geom.Segment
  lazy val Segment = com.siigna.util.geom.Segment

  type TransformationMatrix = com.siigna.util.geom.TransformationMatrix
  lazy val TransformationMatrix = com.siigna.util.geom.TransformationMatrix

  type Vector = com.siigna.util.geom.Vector
  lazy val Vector = com.siigna.util.geom.Vector

  // Log
  lazy val Log = com.siigna.util.logging.Log

}