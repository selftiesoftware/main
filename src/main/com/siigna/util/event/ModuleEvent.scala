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
package com.siigna.util.event

import com.siigna.module.{Module}

/**
 * Events the modules can use to signal to each other.
 */
sealed trait ModuleEvent extends Event

/**
 * Messages that can be sent to and from states, containing a message of a given type.
 * <b>Note:</b> Messages are treated like normal events and are <i>not</i> given to children and/or
 * parent modules like [[com.siigna.util.event.Start]] and [[com.siigna.util.event.End]].
 *
 * @param message  any object that the module wishes to pass on to other states.
 */
final case class Message[T](message : T) extends ModuleEvent { val symbol = 'Message }

/**
 * An event that signals a [[com.siigna.module.Module]] to end with a given message.
 * @param message  The message to pass to the 'parent' module.
 * @tparam T  The type of the message.
 */
final case class End[T](message: T) extends ModuleEvent { val symbol = 'ModuleEnd }

/**
 * An event that signals a [[com.siigna.module.Module]] to end.
 */
case object End extends ModuleEvent { val symbol = 'ModuleEnd }

/**
 * An event that signals that a [[com.siigna.module.Module]] wishes to initialize and forward to the given module.
 * This can be used effectively by splitting functionality into different modules and invoking them when needed.
 * <br>
 * To load the module stored in the value ''module'' with the message "Hello World":
 * {{{
 *   Start(module, "Hello World")
 * }}}
 *
 * @tparam T  The type of the message to pass on. Can be anything <: Any.
 * @param module  The [[com.siigna.module.Module]] to load.
 * @param message  A message to pass on of type T.
 * @return A Start event referring to a [[com.siigna.module.Module]] and containing a message of type T.
 */
final case class Start[T](module : Module, message : T) extends ModuleEvent { val symbol = 'ModuleStart }

/**
 * Companion object for case class [[com.siigna.util.event.Start]]
 */
object Start {

  /**
   * Creates a Start event with the given [[com.siigna.module.Module]], so the controller can load the new module.
   * <br>
   * To load the module stored in the value ''module'':
   * {{{
   *   Start(module)
   * }}}
   *
   * @param module The [[com.siigna.module.Module]] to create
   * @return A [[com.siigna.module.Module]]
   */
  def apply(module : Module) : Start[Unit] = new Start[Unit](module, Unit)

}