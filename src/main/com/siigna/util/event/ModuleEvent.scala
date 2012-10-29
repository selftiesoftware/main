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
package com.siigna.app.view.event

import com.siigna.module.ModuleInstance

/**
 * Events the modules can use to signal to each other.
 */
trait ModuleEvent extends Event

/**
 * Messages that can be sent to and from modules containing any object.
 *
 * @param message  any object that the module wishes to forward.
 */
case class Message[T](message : T) extends ModuleEvent { val symbol = 'Message }

/**
 * An event that signals a [[com.siigna.module.Module]] to end with a given message.
 * @param message  The message to pass to the 'parent' module.
 * @tparam T  The type of the message.
 */
case class End[T](message: T) extends ModuleEvent { val symbol = 'ModuleEnd }

/**
 * An event that signals a [[com.siigna.module.Module]] to end.
 */
case object End extends ModuleEvent { val symbol = 'ModuleEnd}

/**
 * An event that signals that a [[com.siigna.module.Module]] wishes to initialize and forward to the given module.
 * This can be used effectively by splitting functionality into different modules and invoking them when needed.
 * @param module  The module to initialize and forward to.
 */
case class Start(module : ModuleInstance) extends ModuleEvent { val symbol = 'ModuleStart }