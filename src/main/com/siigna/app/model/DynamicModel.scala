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
/*package com.siigna.app.model

import com.siigna.util.logging.Log
import action.Action
import shape.DynamicShape

/**
 * The DynamicModel contains the <b>dynamic</b> (<code>var dynamic : Seq[DynamicShape]</code>) layer which is the part
 * of the drawing which are selected out for editing - the mutable and globally accessible canvas. The DynamicModel is
 * also responsible for propagating actions to the underlying <code>Model</code>.
 * These shapes can be altered without changes in the static layer.
 * When the changes have been made, the shapes are removed from the dynamic layer, and the actions which have
 * been applied on the dynamic layer is applied on the static layer.
 *
 * TODO: Start the thread
 */
trait DynamicModel extends Thread {

  /**
   * The actions performed on the model.
   */
  protected lazy val actions = new ActionModel(model)

  /**
   * The underlying mutable model.
   */
  protected val model = new Model

  /**
   * The mutable shapes.
   */
  protected var mutableShapes = scala.collection.immutable.HashMap[String, DynamicShape]()

  /**
   * Shortcut to execute a given Action.
   */
  def apply(action : Action) = execute(action)

  /**
   * Deselects the entire model.
   */
  def deselect = {
    mutableShapes.foreach(entry => {
      if (!entry._2.actions.isEmpty) {
        execute(entry._2.actions)
      }
      mutableShapes -= entry._1
    })
    this
  }

  /**
   * Deselects a shape.
   */
  def deselect(dynamic : DynamicShape) = {
    val entry = mutableShapes.find(_._2 == dynamic)
    if (entry.isDefined) {
      val (id, shape) = entry.get
      execute(shape.actions)
      mutableShapes - id
    } else {
      Log.error("Model: Could not deselect shape - did not exist in the model.")
    }
    this
  }

  /**
   * Deselects a shape.
   */
  def deselect(id : String) =
    try {
      val shape = mutableShapes.apply(id)
      execute(shape.actions)
      mutableShapes - id
    } catch {
      case e => Log.error("Model: Could not deselect shape - did not exist in the model.")
    }

  /**
   * Deselects multiple shapes.
   */
  def deselect(ids : Traversable[String]) = {
    ids.foreach(idx => try {
      val shape = mutableShapes(idx)
      execute(shape.actions)
      mutableShapes - idx
    } catch {
      case e : NoSuchElementException => Log.error("Model: Could not deselect shape with the id '"+idx+"' - did not exist in the model.")
      case e => Log.error("Model: Could not deselct shape with id '"+idx+"' - unknown error.")
    })
  }

  /**
   * Execute an action.
   */
  def execute(action : Action) : this.type = {
    actions.execute(action)
    this
  }

  /**
   * Executes several actions.
   */
  def execute(actions : TraversableOnce[Action]) : this.type = {
    actions.foreach(execute(_))
    this
  }

  /**
   * Determines whether a part of the model is selected (condition: <code>!selectedShapes.isEmpty</code>).
   */
  def isSelected = !selected.isEmpty

  /**
   * Redo the last action undone, unless there aren't any actions to redo.
   */
  def redo : this.type = {
    actions.redo
    this
  }

  /**
   * Selects a shape from it's id unless it's already selected.
   */
  def select(id : String) : this.type = {
    try {
      if (!mutableShapes.contains(id)) {
        val shape = model.static(id)
        val dynamic = new DynamicShape(id, shape)
        mutableShapes += (id -> dynamic)
      }
    } catch {
      case e : NoSuchElementException => Log.error("Model: Could not select shape - did not exist in the model.")
      case e => Log.error("Model : Could not select shape - unknown error occurred "+e)
    }
    this
  }

  /**
   * Selects a shape from a <code>Option[String]</code>.
   */
  def select(id : Option[String]) : this.type = {
    if (id.isDefined) select(id.get)
    this
  }

  /**
   * Selects multiple shapes.
   */
  def select(ids : Traversable[String]) : this.type = {
    ids.foreach(select)
    this
  }

  /**
   * Selects every shape in the Model.
   */
  def selectAll : this.type = {
    val dynamics = Model.immutable.filterNot(e => mutableShapes.contains(e._1)).map(e => e._1 -> new DynamicShape(e._1, e._2))
    mutableShapes ++= dynamics
    this
  }

  /**
   * Returns a <code>Iterable</code> containing every selected shape.
   */
  def selected = mutableShapes.values

  /**
   * Returns a <code>Map</code> of every selected shape paired with their id.
   */
  def selectedWithId = mutableShapes

  /**
   * Undo the last action executed, unless there aren't any actions to redo.
   */
  def undo : this.type = {
    actions.undo
    this
  }

}*/