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
  def Preload(name : Symbol, classPath : String = "com.siigna.module.endogenous", filePath : String = "") {
    Control(new com.siigna.app.controller.command.Preload(name, classPath, if (filePath.eq("")) name.name else filePath ))
  }

  /**
   * Asks the controller to send a given event into the event stream.
   *
   * @tparam T  The type of the event.
   * @param event  The event to send.
   */
  def Send[T <: Event](event : T) {
    Control(new com.siigna.app.controller.command.Send[T](event))
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

  lazy val Preferences = com.siigna.util.collection.Preferences

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